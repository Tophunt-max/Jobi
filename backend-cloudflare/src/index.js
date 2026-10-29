/**
 * Cloudflare Worker backend for Sarkari Job Portal.
 *
 * - Public read API for the Android app (ACTIVE notices only).
 * - Admin API (moderation, importer, ads, uploads) protected by ADMIN_API_KEY secret.
 * - Scheduled importer that scrapes official portals into a review queue.
 *
 * Bindings (see wrangler.toml): DB (D1), FILES_BUCKET (R2), optional PUBLIC_RATE_LIMITER
 * ADMIN_RATE_LIMITER and AI_RATE_LIMITER (Workers Rate Limiting).
 * Secrets: ADMIN_API_KEY (required for admin API), GEMINI_API_KEY (AI mentor), REPORT_SALT (optional).
 */
import { corsHeaders, errorResponse, json, HttpError } from "./http.js";
import { handlePublic } from "./routes/public.js";
import { handleAdmin } from "./routes/admin.js";
import { handleClient } from "./routes/client.js";
import { executeJobImport, expireOldJobs } from "./importer/pipeline.js";
import { purgeExpiredSessions } from "./auth.js";

export default {
    async fetch(request, env, ctx) {
        const requestId = request.headers.get("cf-ray") || crypto.randomUUID();
        const url = new URL(request.url);
        const path = url.pathname;
        const method = request.method;
        const isAdmin = path.startsWith("/api/admin/");
        const cors = corsHeaders(request, env, { admin: isAdmin });

        if (method === "OPTIONS") {
            return new Response(null, { status: 204, headers: cors });
        }

        try {
            if (!env.DB) throw new HttpError(503, "Database binding is not configured");

            await enforceRateLimit(request, env, isAdmin);

            if (isAdmin) {
                return await handleAdmin(request, env, url, cors);
            }

            if (path.startsWith("/api/files/") && (method === "GET" || method === "HEAD")) {
                return await serveFile(env, path, cors);
            }

            const clientRes = await handleClient(request, env, url, cors);
            if (clientRes) return clientRes;

            if (method !== "GET" && method !== "HEAD") {
                throw new HttpError(405, "Method not allowed");
            }

            const res = await handlePublic(request, env, url, cors);
            if (res) return res;

            throw new HttpError(404, "Route not found");
        } catch (err) {
            return errorResponse(err, requestId, cors);
        }
    },

    async scheduled(event, env, ctx) {
        ctx.waitUntil((async () => {
            try {
                await purgeExpiredSessions(env).catch((e) => console.warn("[CRON] session purge failed:", e.message));
                const expired = await expireOldJobs(env);
                if (expired) console.log(`[CRON] Marked ${expired} jobs as EXPIRED`);
                const summary = await executeJobImport(env, { triggeredBy: "CRON_TRIGGER" });
                console.log(`[CRON] Import ${summary.status}: ${summary.totalInserted} new, ${summary.totalErrors} errors`);
            } catch (err) {
                // 409 (a run is already in progress) is expected occasionally.
                console.warn("[CRON] Import skipped/failed:", err && err.message ? err.message : err);
            }
        })());
    }
};

/**
 * Per-client rate limiting using the Workers Rate Limiting binding, if configured.
 * Admin calls are keyed by IP with a stricter limit to slow down key guessing.
 */
async function enforceRateLimit(request, env, isAdmin) {
    const limiter = isAdmin ? env.ADMIN_RATE_LIMITER : env.PUBLIC_RATE_LIMITER;
    if (!limiter || typeof limiter.limit !== "function") return;
    const ip = request.headers.get("CF-Connecting-IP") || "unknown";
    const { success } = await limiter.limit({ key: `${isAdmin ? "admin" : "public"}:${ip}` });
    if (!success) throw new HttpError(429, "Too many requests, please slow down");
}

/**
 * Serves uploaded notice PDFs from R2. Only keys under notices/ are reachable and
 * they are always served as PDF, so the bucket cannot be used to host HTML/JS.
 */
async function serveFile(env, path, cors) {
    if (!env.FILES_BUCKET) throw new HttpError(503, "File storage is not configured");
    const key = decodeURIComponent(path.slice("/api/files/".length));
    if (!/^notices\/\d{4}-\d{2}\/[0-9a-f-]{36}\.pdf$/.test(key)) {
        throw new HttpError(404, "File not found");
    }
    const object = await env.FILES_BUCKET.get(key);
    if (!object) throw new HttpError(404, "File not found");

    return new Response(object.body, {
        headers: {
            ...cors,
            "Content-Type": "application/pdf",
            "Content-Disposition": "inline",
            "X-Content-Type-Options": "nosniff",
            "Content-Security-Policy": "default-src 'none'; sandbox",
            "Cache-Control": "public, max-age=86400, immutable",
            "ETag": object.httpEtag
        }
    });
}
