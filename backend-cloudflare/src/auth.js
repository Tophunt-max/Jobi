/**
 * Admin authentication.
 *
 * Two ways to authenticate an /api/admin/* request:
 *
 * 1. Session cookie (admin panel users). Email + password login creates a server-side
 *    session; the browser holds only an opaque random token in an HttpOnly, Secure,
 *    SameSite=Strict cookie. The database stores SHA-256(token), never the token.
 *    State-changing requests made with a cookie must also carry an allowed Origin (CSRF).
 *
 * 2. ADMIN_API_KEY bearer token (automation, CLI, first-owner bootstrap). Header only.
 *
 * Passwords: PBKDF2-SHA256, 100,000 iterations (the Workers runtime maximum), 16-byte salt.
 * Accounts lock for 15 minutes after 5 consecutive failures.
 */
import { HttpError, parseList } from "./http.js";
import { sha256Hex } from "./validation.js";

const MIN_KEY_LENGTH = 32;
export const SESSION_COOKIE = "__Host-sjp_admin";
const SESSION_TTL_MS = 12 * 3600_000;        // absolute lifetime
const SESSION_IDLE_MS = 2 * 3600_000;        // inactivity timeout
const SESSION_TOUCH_MS = 5 * 60_000;         // throttle last_seen_at writes
const PBKDF2_ITERATIONS = 100_000;
const MAX_FAILED_ATTEMPTS = 5;
const LOCK_MS = 15 * 60_000;
export const MIN_PASSWORD_LENGTH = 12;
export const MAX_PASSWORD_LENGTH = 128;

// ---------------------------------------------------------------------------
// Principal resolution
// ---------------------------------------------------------------------------

/**
 * Returns the authenticated principal or throws 401.
 * Principal: { kind: "session" | "api_key", userId, email, role, sessionId? }
 */
export async function requireAdmin(request, env) {
    const bearer = extractBearer(request);
    if (bearer) {
        await verifyApiKey(bearer, env);
        return { kind: "api_key", userId: null, email: "api-key", role: "owner" };
    }

    const token = readCookie(request, SESSION_COOKIE);
    if (token) {
        const principal = await resolveSession(env, token, request);
        if (principal) {
            enforceSameOrigin(request, env);
            return principal;
        }
    }
    throw new HttpError(401, "Not signed in");
}

export function requireOwner(principal) {
    if (principal.role !== "owner") throw new HttpError(403, "Only an owner can do this");
}

async function verifyApiKey(provided, env) {
    const expected = env.ADMIN_API_KEY;
    if (!expected || expected.length < MIN_KEY_LENGTH) {
        throw new HttpError(401, "Invalid or missing admin credentials");
    }
    if (!(await timingSafeEqual(provided, expected))) {
        throw new HttpError(401, "Invalid or missing admin credentials");
    }
}

function extractBearer(request) {
    const auth = request.headers.get("Authorization");
    if (auth && auth.startsWith("Bearer ")) return auth.slice(7).trim();
    const header = request.headers.get("X-Admin-Key");
    return header ? header.trim() : "";
}

/** Cookie-authenticated writes must come from an allowed origin (defence against CSRF). */
function enforceSameOrigin(request, env) {
    const method = request.method.toUpperCase();
    if (method === "GET" || method === "HEAD" || method === "OPTIONS") return;
    const origin = request.headers.get("Origin");
    const allowed = parseList(env.ADMIN_ALLOWED_ORIGINS);
    const self = new URL(request.url).origin;
    if (!origin || (origin !== self && !allowed.includes(origin))) {
        throw new HttpError(403, "Cross-origin request rejected");
    }
}

// ---------------------------------------------------------------------------
// Sessions
// ---------------------------------------------------------------------------

async function resolveSession(env, token, request) {
    if (!/^[A-Za-z0-9_-]{43}$/.test(token)) return null;
    const id = await sha256Hex(token);
    const row = await env.DB.prepare(`
        SELECT s.id, s.user_id, s.expires_at, s.last_seen_at, u.email, u.role, u.disabled
        FROM admin_sessions s JOIN admin_users u ON u.id = s.user_id
        WHERE s.id = ?
    `).bind(id).first();
    if (!row) return null;

    const now = Date.now();
    const expired = Date.parse(row.expires_at) <= now || Date.parse(row.last_seen_at) + SESSION_IDLE_MS <= now;
    if (expired || row.disabled) {
        await env.DB.prepare("DELETE FROM admin_sessions WHERE id = ?").bind(id).run();
        return null;
    }
    if (Date.parse(row.last_seen_at) + SESSION_TOUCH_MS <= now) {
        await env.DB.prepare("UPDATE admin_sessions SET last_seen_at = ? WHERE id = ?")
            .bind(new Date(now).toISOString(), id).run();
    }
    return { kind: "session", userId: row.user_id, email: row.email, role: row.role, sessionId: id };
}

export async function createSession(env, user, request) {
    const token = randomToken(32); // 256 bits -> 43 base64url chars
    const id = await sha256Hex(token);
    const now = new Date();
    const ip = request.headers.get("CF-Connecting-IP") || "";
    await env.DB.prepare(`
        INSERT INTO admin_sessions (id, user_id, created_at, expires_at, last_seen_at, user_agent, ip_hash)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    `).bind(
        id, user.id, now.toISOString(), new Date(now.getTime() + SESSION_TTL_MS).toISOString(), now.toISOString(),
        (request.headers.get("User-Agent") || "").slice(0, 200), ip ? await sha256Hex(`${env.REPORT_SALT || ""}|${ip}`) : null
    ).run();
    return token;
}

export async function destroySession(env, sessionId) {
    if (sessionId) await env.DB.prepare("DELETE FROM admin_sessions WHERE id = ?").bind(sessionId).run();
}

export async function destroyAllSessionsForUser(env, userId, exceptSessionId = null) {
    if (exceptSessionId) {
        await env.DB.prepare("DELETE FROM admin_sessions WHERE user_id = ? AND id != ?").bind(userId, exceptSessionId).run();
    } else {
        await env.DB.prepare("DELETE FROM admin_sessions WHERE user_id = ?").bind(userId).run();
    }
}

export async function purgeExpiredSessions(env) {
    const now = new Date();
    await env.DB.prepare("DELETE FROM admin_sessions WHERE expires_at <= ? OR last_seen_at <= ?")
        .bind(now.toISOString(), new Date(now.getTime() - SESSION_IDLE_MS).toISOString()).run();
}

export function sessionCookie(token) {
    return `${SESSION_COOKIE}=${token}; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=${SESSION_TTL_MS / 1000}`;
}

export function clearSessionCookie() {
    return `${SESSION_COOKIE}=; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=0`;
}

function readCookie(request, name) {
    const header = request.headers.get("Cookie") || "";
    for (const part of header.split(";")) {
        const i = part.indexOf("=");
        if (i > 0 && part.slice(0, i).trim() === name) return part.slice(i + 1).trim();
    }
    return null;
}

// ---------------------------------------------------------------------------
// Passwords & login
// ---------------------------------------------------------------------------

export function validatePassword(password) {
    if (typeof password !== "string") throw new HttpError(400, "password is required");
    if (password.length < MIN_PASSWORD_LENGTH) {
        throw new HttpError(400, `Password must be at least ${MIN_PASSWORD_LENGTH} characters`);
    }
    if (password.length > MAX_PASSWORD_LENGTH) {
        throw new HttpError(400, `Password must be at most ${MAX_PASSWORD_LENGTH} characters`);
    }
    if (/^(.)\1+$/.test(password)) throw new HttpError(400, "Password is too simple");
}

export async function hashPassword(password, saltB64 = null, iterations = PBKDF2_ITERATIONS) {
    const salt = saltB64 ? b64decode(saltB64) : crypto.getRandomValues(new Uint8Array(16));
    const key = await crypto.subtle.importKey("raw", new TextEncoder().encode(password), "PBKDF2", false, ["deriveBits"]);
    const bits = await crypto.subtle.deriveBits({ name: "PBKDF2", hash: "SHA-256", salt, iterations }, key, 256);
    return { hash: b64encode(new Uint8Array(bits)), salt: b64encode(salt), iterations };
}

// Used when the email is unknown so response time doesn't reveal which accounts exist.
const DUMMY_SALT = "AAAAAAAAAAAAAAAAAAAAAA==";

/**
 * Verifies credentials. Returns the user row or throws 401/423.
 * Error messages are identical for unknown email and wrong password.
 */
export async function verifyLogin(env, email, password) {
    const user = await env.DB.prepare("SELECT * FROM admin_users WHERE email = ?").bind(email).first();
    const now = Date.now();

    if (!user) {
        await hashPassword(password, DUMMY_SALT);
        throw new HttpError(401, "Incorrect email or password");
    }
    if (user.locked_until && Date.parse(user.locked_until) > now) {
        await hashPassword(password, DUMMY_SALT);
        const mins = Math.ceil((Date.parse(user.locked_until) - now) / 60_000);
        throw new HttpError(423, `Too many failed attempts. Try again in ${mins} minute${mins === 1 ? "" : "s"}.`);
    }

    const { hash } = await hashPassword(password, user.password_salt, user.password_iterations);
    const ok = await timingSafeEqual(hash, user.password_hash);

    if (!ok || user.disabled) {
        if (!ok) {
            const attempts = (user.failed_attempts || 0) + 1;
            const lock = attempts >= MAX_FAILED_ATTEMPTS ? new Date(now + LOCK_MS).toISOString() : null;
            await env.DB.prepare("UPDATE admin_users SET failed_attempts = ?, locked_until = ? WHERE id = ?")
                .bind(lock ? 0 : attempts, lock, user.id).run();
        }
        throw new HttpError(401, "Incorrect email or password");
    }

    await env.DB.prepare("UPDATE admin_users SET failed_attempts = 0, locked_until = NULL, last_login_at = ? WHERE id = ?")
        .bind(new Date(now).toISOString(), user.id).run();
    return user;
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function randomToken(bytes) {
    const buf = crypto.getRandomValues(new Uint8Array(bytes));
    return b64encode(buf).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function b64encode(bytes) {
    let s = "";
    for (const b of bytes) s += String.fromCharCode(b);
    return btoa(s);
}

function b64decode(str) {
    const bin = atob(str);
    const out = new Uint8Array(bin.length);
    for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
    return out;
}

/** Constant-time comparison via fixed-length digests. */
export async function timingSafeEqual(a, b) {
    const enc = new TextEncoder();
    const [ha, hb] = await Promise.all([
        crypto.subtle.digest("SHA-256", enc.encode(a)),
        crypto.subtle.digest("SHA-256", enc.encode(b))
    ]);
    const va = new Uint8Array(ha);
    const vb = new Uint8Array(hb);
    let diff = 0;
    for (let i = 0; i < va.length; i++) diff |= va[i] ^ vb[i];
    return diff === 0;
}
