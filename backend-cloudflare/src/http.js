/**
 * HTTP helpers: JSON responses, CORS, typed errors and safe body parsing.
 */

export class HttpError extends Error {
    constructor(status, message, code = undefined) {
        super(message);
        this.status = status;
        this.code = code || httpCode(status);
    }
}

function httpCode(status) {
    switch (status) {
        case 400: return "BAD_REQUEST";
        case 401: return "UNAUTHORIZED";
        case 403: return "FORBIDDEN";
        case 404: return "NOT_FOUND";
        case 409: return "CONFLICT";
        case 413: return "PAYLOAD_TOO_LARGE";
        case 415: return "UNSUPPORTED_MEDIA_TYPE";
        case 429: return "RATE_LIMITED";
        default: return status >= 500 ? "INTERNAL_ERROR" : "ERROR";
    }
}

const SECURITY_HEADERS = {
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "no-referrer",
    "X-Frame-Options": "DENY"
};

/**
 * Builds CORS headers.
 * - Public read endpoints are open to any origin (no credentials are involved).
 * - Admin endpoints only reflect origins listed in env.ADMIN_ALLOWED_ORIGINS.
 */
export function corsHeaders(request, env, { admin = false } = {}) {
    const origin = request.headers.get("Origin");
    const base = {
        "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
        "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Admin-Key",
        "Access-Control-Max-Age": "86400",
        "Vary": "Origin"
    };

    if (!admin) {
        return { ...base, "Access-Control-Allow-Origin": "*" };
    }

    const allowed = parseList(env.ADMIN_ALLOWED_ORIGINS);
    if (origin && allowed.includes(origin)) {
        return { ...base, "Access-Control-Allow-Origin": origin };
    }
    // No Allow-Origin header: browsers on other origins are blocked.
    return base;
}

export function parseList(value) {
    return String(value || "")
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean);
}

export function json(data, { status = 200, headers = {} } = {}) {
    return new Response(JSON.stringify(data), {
        status,
        headers: {
            "Content-Type": "application/json;charset=UTF-8",
            ...SECURITY_HEADERS,
            ...headers
        }
    });
}

export function errorResponse(err, requestId, headers) {
    if (err instanceof HttpError) {
        return json(
            { success: false, error: err.code, message: err.message, requestId },
            { status: err.status, headers }
        );
    }
    // Never leak internal error details (SQL, stack traces) to clients.
    console.error(`[${requestId}] Unhandled error:`, err && err.stack ? err.stack : err);
    return json(
        { success: false, error: "INTERNAL_ERROR", message: "Internal server error", requestId },
        { status: 500, headers }
    );
}

const MAX_JSON_BYTES = 256 * 1024;

/**
 * Reads a JSON object body with a size cap. Throws HttpError(400) on malformed input.
 */
export async function readJson(request, { maxBytes = MAX_JSON_BYTES, optional = false } = {}) {
    const declared = Number(request.headers.get("content-length") || "0");
    if (declared > maxBytes) {
        throw new HttpError(413, `Request body exceeds ${maxBytes} bytes`);
    }
    const text = await request.text();
    if (text.length > maxBytes) {
        throw new HttpError(413, `Request body exceeds ${maxBytes} bytes`);
    }
    if (!text.trim()) {
        if (optional) return {};
        throw new HttpError(400, "Request body is required");
    }
    let parsed;
    try {
        parsed = JSON.parse(text);
    } catch {
        throw new HttpError(400, "Malformed JSON body");
    }
    if (parsed === null || typeof parsed !== "object" || Array.isArray(parsed)) {
        throw new HttpError(400, "JSON body must be an object");
    }
    return parsed;
}
