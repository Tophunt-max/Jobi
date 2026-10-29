/**
 * Admin panel edge worker.
 *
 * - /api/*  -> forwarded to the backend Worker through a service binding, so the
 *              panel and its API share one origin. The session cookie can then be
 *              HttpOnly + SameSite=Strict and is never exposed to other sites.
 * - else    -> static Next.js export (Workers Static Assets).
 */
export default {
    async fetch(request, env) {
        const url = new URL(request.url);
        if (url.pathname.startsWith("/api/")) {
            if (!env.BACKEND) {
                return new Response(JSON.stringify({ success: false, message: "Backend binding missing" }), {
                    status: 503,
                    headers: { "Content-Type": "application/json" }
                });
            }
            return env.BACKEND.fetch(request);
        }
        return env.ASSETS.fetch(request);
    }
};
