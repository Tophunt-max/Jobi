/**
 * Admin panel accounts:
 *   POST /api/admin/auth/login            {email, password}  -> sets session cookie
 *   POST /api/admin/auth/logout
 *   GET  /api/admin/auth/me
 *   POST /api/admin/auth/change-password  {current_password, new_password}
 *   GET  /api/admin/users                 (owner)
 *   POST /api/admin/users                 {email, password, role}  (owner, or API key for bootstrap)
 *   POST /api/admin/users/:id/disable     (owner)
 *   POST /api/admin/users/:id/enable      (owner)
 *   POST /api/admin/users/:id/reset-password {new_password} (owner)
 */
import { json, readJson, HttpError } from "../http.js";
import {
    clearSessionCookie, createSession, destroyAllSessionsForUser, destroySession,
    hashPassword, requireAdmin, requireOwner, sessionCookie, validatePassword, verifyLogin
} from "../auth.js";
import { oneOf } from "../validation.js";

const EMAIL_RE = /^[^\s@]{1,64}@[^\s@]{1,190}\.[^\s@]{2,}$/;

function normalizeEmail(value) {
    if (typeof value !== "string") throw new HttpError(400, "email is required");
    const email = value.trim().toLowerCase();
    if (!EMAIL_RE.test(email) || email.length > 254) throw new HttpError(400, "Enter a valid email address");
    return email;
}

function publicUser(u) {
    return {
        id: u.id,
        email: u.email,
        role: u.role,
        disabled: !!u.disabled,
        last_login_at: u.last_login_at || null,
        created_at: u.created_at,
        locked: !!(u.locked_until && Date.parse(u.locked_until) > Date.now())
    };
}

function idFromPath(path, prefix, suffix) {
    const id = decodeURIComponent(path.slice(prefix.length, path.length - suffix.length));
    if (!/^usr-[0-9a-f-]{36}$/.test(id)) throw new HttpError(404, "User not found");
    return id;
}

/** Handles the login endpoint, which is the only admin route that does not need auth. */
export async function handleLogin(request, env, cors) {
    if (env.LOGIN_RATE_LIMITER && typeof env.LOGIN_RATE_LIMITER.limit === "function") {
        const ip = request.headers.get("CF-Connecting-IP") || "unknown";
        const { success } = await env.LOGIN_RATE_LIMITER.limit({ key: `login:${ip}` });
        if (!success) throw new HttpError(429, "Too many sign-in attempts. Wait a minute and try again.");
    }
    const body = await readJson(request, { maxBytes: 4 * 1024 });
    const email = normalizeEmail(body.email);
    if (typeof body.password !== "string" || !body.password || body.password.length > 128) {
        throw new HttpError(401, "Incorrect email or password");
    }
    const user = await verifyLogin(env, email, body.password);
    const token = await createSession(env, user, request);
    return json(
        { success: true, user: publicUser(user) },
        { headers: { ...cors, "Set-Cookie": sessionCookie(token), "Cache-Control": "no-store" } }
    );
}

/**
 * Account routes for an already-authenticated principal. Returns null if the path
 * is not an auth/user route.
 */
export async function handleAccount(request, env, url, cors, principal) {
    const path = url.pathname;
    const method = request.method;
    const ok = (data, status = 200, extra = {}) =>
        json({ success: true, ...data }, { status, headers: { ...cors, "Cache-Control": "no-store", ...extra } });

    if (path === "/api/admin/auth/me" && method === "GET") {
        return ok({ user: { email: principal.email, role: principal.role, kind: principal.kind } });
    }

    if (path === "/api/admin/auth/logout" && method === "POST") {
        await destroySession(env, principal.sessionId);
        return ok({ message: "Signed out" }, 200, { "Set-Cookie": clearSessionCookie() });
    }

    if (path === "/api/admin/auth/change-password" && method === "POST") {
        if (principal.kind !== "session") throw new HttpError(400, "Sign in with email and password to change it");
        const body = await readJson(request, { maxBytes: 4 * 1024 });
        const user = await env.DB.prepare("SELECT * FROM admin_users WHERE id = ?").bind(principal.userId).first();
        if (!user) throw new HttpError(401, "Not signed in");
        await verifyLogin(env, user.email, String(body.current_password || "")).catch(() => {
            throw new HttpError(400, "Current password is incorrect");
        });
        validatePassword(body.new_password);
        if (body.new_password === body.current_password) throw new HttpError(400, "New password must be different");
        const h = await hashPassword(body.new_password);
        await env.DB.prepare(`
            UPDATE admin_users SET password_hash = ?, password_salt = ?, password_iterations = ?, password_changed_at = ?
            WHERE id = ?
        `).bind(h.hash, h.salt, h.iterations, new Date().toISOString(), user.id).run();
        // Sign out every other device.
        await destroyAllSessionsForUser(env, user.id, principal.sessionId);
        return ok({ message: "Password changed. Other sessions were signed out." });
    }

    if (path === "/api/admin/users" && method === "GET") {
        requireOwner(principal);
        const { results } = await env.DB.prepare("SELECT * FROM admin_users ORDER BY created_at ASC").all();
        return ok({ users: (results || []).map(publicUser) });
    }

    if (path === "/api/admin/users" && method === "POST") {
        requireOwner(principal);
        const body = await readJson(request, { maxBytes: 4 * 1024 });
        const email = normalizeEmail(body.email);
        const role = oneOf(body.role, "role", ["owner", "editor"], "editor");
        validatePassword(body.password);
        const h = await hashPassword(body.password);
        const now = new Date().toISOString();
        const id = `usr-${crypto.randomUUID()}`;
        const res = await env.DB.prepare(`
            INSERT INTO admin_users (id, email, password_hash, password_salt, password_iterations, role,
                                     password_changed_at, created_at, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(email) DO NOTHING
        `).bind(id, email, h.hash, h.salt, h.iterations, role, now, now, principal.email).run();
        if (!res.meta || res.meta.changes === 0) throw new HttpError(409, "An account with this email already exists");
        const user = await env.DB.prepare("SELECT * FROM admin_users WHERE id = ?").bind(id).first();
        return ok({ user: publicUser(user) }, 201);
    }

    const userAction = path.match(/^\/api\/admin\/users\/[^/]+\/(disable|enable|reset-password)$/);
    if (userAction && method === "POST") {
        requireOwner(principal);
        const action = userAction[1];
        const id = idFromPath(path, "/api/admin/users/", `/${action}`);
        const target = await env.DB.prepare("SELECT * FROM admin_users WHERE id = ?").bind(id).first();
        if (!target) throw new HttpError(404, "User not found");

        if (action === "disable") {
            if (target.id === principal.userId) throw new HttpError(400, "You cannot disable your own account");
            if (target.role === "owner") {
                const owners = await env.DB.prepare(
                    "SELECT COUNT(*) AS c FROM admin_users WHERE role = 'owner' AND disabled = 0"
                ).first();
                if ((owners?.c || 0) <= 1) throw new HttpError(400, "At least one active owner is required");
            }
            await env.DB.prepare("UPDATE admin_users SET disabled = 1 WHERE id = ?").bind(id).run();
            await destroyAllSessionsForUser(env, id);
            return ok({ message: `${target.email} disabled` });
        }

        if (action === "enable") {
            await env.DB.prepare("UPDATE admin_users SET disabled = 0, failed_attempts = 0, locked_until = NULL WHERE id = ?")
                .bind(id).run();
            return ok({ message: `${target.email} enabled` });
        }

        // reset-password
        const body = await readJson(request, { maxBytes: 4 * 1024 });
        validatePassword(body.new_password);
        const h = await hashPassword(body.new_password);
        await env.DB.prepare(`
            UPDATE admin_users SET password_hash = ?, password_salt = ?, password_iterations = ?,
                password_changed_at = ?, failed_attempts = 0, locked_until = NULL
            WHERE id = ?
        `).bind(h.hash, h.salt, h.iterations, new Date().toISOString(), id).run();
        await destroyAllSessionsForUser(env, id);
        return ok({ message: `Password reset for ${target.email}` });
    }

    return null;
}

export { requireAdmin };
