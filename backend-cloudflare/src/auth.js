/**
 * Admin authentication.
 *
 * The admin key MUST be configured as a Worker secret:
 *   npx wrangler secret put ADMIN_API_KEY
 * There is intentionally no fallback value. If the secret is missing, every
 * admin route fails closed with 503 instead of accepting a well-known default.
 *
 * The key is accepted only via headers (never via query string, which would
 * leak it into logs, browser history and Referer headers):
 *   Authorization: Bearer <key>
 *   X-Admin-Key: <key>
 */
import { HttpError } from "./http.js";

const MIN_KEY_LENGTH = 32;

export async function requireAdmin(request, env) {
    const expected = env.ADMIN_API_KEY;
    if (!expected || expected.length < MIN_KEY_LENGTH) {
        console.error("ADMIN_API_KEY secret is missing or shorter than 32 characters; admin API disabled.");
        throw new HttpError(503, "Admin API is not configured on this server");
    }

    const provided = extractKey(request);
    if (!provided || !(await timingSafeEqual(provided, expected))) {
        throw new HttpError(401, "Invalid or missing admin credentials");
    }
}

function extractKey(request) {
    const auth = request.headers.get("Authorization");
    if (auth && auth.startsWith("Bearer ")) {
        return auth.slice(7).trim();
    }
    const header = request.headers.get("X-Admin-Key");
    return header ? header.trim() : "";
}

/**
 * Constant-time comparison: hash both values so lengths are equal, then
 * compare byte-by-byte without early exit.
 */
async function timingSafeEqual(a, b) {
    const enc = new TextEncoder();
    const [ha, hb] = await Promise.all([
        crypto.subtle.digest("SHA-256", enc.encode(a)),
        crypto.subtle.digest("SHA-256", enc.encode(b))
    ]);
    const va = new Uint8Array(ha);
    const vb = new Uint8Array(hb);
    let diff = 0;
    for (let i = 0; i < va.length; i++) {
        diff |= va[i] ^ vb[i];
    }
    return diff === 0;
}
