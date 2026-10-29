# Sarkari Job Portal admin panel

A static Next.js app for reviewing scraped notices, publishing jobs, managing sources, ads, user reports and the admin team.

Production: https://sarkari-jobs-admin.bubunmohanta14.workers.dev

## How it is served

`wrangler.toml` deploys one Worker that:

- serves the static export in `out/` (Workers Static Assets);
- forwards `/api/*` to the backend Worker `sarkari-jobs-worker` over a **service binding** (`worker/index.js`).

So the panel and its API share one origin. No CORS is involved and the session cookie can be strict.

## Sign-in

- Email + password accounts are stored in D1 (`admin_users`). Passwords are hashed with PBKDF2-SHA256 (100k iterations, random salt).
- Signing in creates a server-side session. The browser holds only an opaque token in a `__Host-` cookie that is `HttpOnly`, `Secure` and `SameSite=Strict`. JavaScript cannot read it, and nothing is kept in localStorage or sessionStorage.
- Sessions end after 12 hours, after 2 hours of inactivity, on sign-out, when the password changes, or when an owner disables the account.
- After 5 wrong passwords the account locks for 15 minutes. Sign-in is also rate limited to 10 attempts per minute per IP.
- Cookie-authenticated write requests must carry the panel's `Origin` (CSRF protection).
- Roles: **owner** can add, disable and enable members and reset their passwords (Settings > Team). **Editor** can do everything else.

### First owner / lost access

An owner account can always be created with the backend's `ADMIN_API_KEY` (kept for automation and recovery):

```bash
curl -X POST https://sarkari-jobs-worker.bubunmohanta14.workers.dev/api/admin/users \
  -H "Authorization: Bearer $ADMIN_API_KEY" -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"at-least-12-characters","role":"owner"}'
```

To reset an owner's password without the panel, use `POST /api/admin/users/<id>/reset-password` with the same header (`GET /api/admin/users` lists ids).

## Deploy

Deploy the backend first (the service binding needs it), then:

```bash
cd admin-panel
npm ci
npm run build
npx wrangler deploy
```

## Local development

Run the backend with `wrangler dev` (port 8787), then start the Next.js dev server with
`NEXT_PUBLIC_API_URL=http://localhost:8787`. Add `ADMIN_ALLOWED_ORIGINS="http://localhost:3000"`
to `backend-cloudflare/.dev.vars` so the backend accepts the panel's cookie requests in development.
