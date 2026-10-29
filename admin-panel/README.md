# Sarkari Job Portal admin panel

A static Next.js app (exported to `out/`) for reviewing scraped notices, publishing jobs manually, managing sources, ads and user reports. All data comes from the Cloudflare Worker in `backend-cloudflare/`.

## Security

- The panel opens on a sign-in screen. The admin types the Worker's `ADMIN_API_KEY`, which is checked against the Worker and kept only in `sessionStorage` for that tab.
- No key is ever bundled. Only `NEXT_PUBLIC_API_URL` (the public Worker URL) is read at build time.
- `public/_headers` sets CSP, `X-Frame-Options: DENY` and `noindex` on Cloudflare Pages.
- **Put the Pages project behind Cloudflare Access** (Zero Trust > Access > Applications > Self-hosted, your `*.pages.dev` domain, allow only your team's emails). That adds a real login in front of the panel.
- Add the panel's URL to `ADMIN_ALLOWED_ORIGINS` in `backend-cloudflare/wrangler.toml`, otherwise the browser blocks admin requests (CORS).

## Deploy to Cloudflare Pages

```bash
cd admin-panel
npm ci
NEXT_PUBLIC_API_URL=https://sarkari-jobs-worker.<account>.workers.dev npm run build
npx wrangler pages deploy out --project-name=sarkari-jobs-admin
```

With Git integration, use root directory `admin-panel`, build command `npm run build`, output directory `out`, and set `NEXT_PUBLIC_API_URL` as a build variable.

## Local development

```bash
npm ci
npm run dev        # http://localhost:3000
npm run typecheck
```

For local work against `wrangler dev`, add `http://localhost:3000` to `ADMIN_ALLOWED_ORIGINS` in `backend-cloudflare/.dev.vars`:

```
ADMIN_ALLOWED_ORIGINS="http://localhost:3000"
```
