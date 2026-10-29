# Cloudflare Worker backend

Production: https://sarkari-jobs-worker.bubunmohanta14.workers.dev (D1 `sarkari_jobs_db`).
R2 is not enabled on the account yet, so PDF upload returns 503 until you enable R2 in the
dashboard, run `npx wrangler r2 bucket create sarkari-job-docs`, and uncomment
`[[r2_buckets]]` in `wrangler.toml`.

The Worker (`src/index.js`) serves the Android app's read API, the admin API used by
`admin-panel/`, an AI-mentor proxy, and a cron job that scrapes official recruitment
portals into a review queue.

## Layout

| Path | Purpose |
|---|---|
| `src/index.js` | Router, rate limiting, R2 file serving, cron handler |
| `src/routes/public.js` | `GET /api/*` for the app (only `ACTIVE`/`EXPIRED` notices) |
| `src/routes/client.js` | `POST /api/reports`, `POST /api/ai/generate` |
| `src/routes/admin.js` | `/api/admin/*` (moderation, importer, ads, reports, PDF upload) |
| `src/importer/` | Scraper (HTMLRewriter) and import pipeline |
| `migrations/` | D1 schema, applied with `wrangler d1 migrations apply` |
| `seed/dev_sample_jobs.sql` | Sample rows for **local development only** |
| `test/smoke.sh` | End-to-end API checks against `wrangler dev` |

## First-time setup

```bash
cd backend-cloudflare
npm ci

npx wrangler login
npx wrangler d1 create sarkari_jobs_db      # copy the database_id into wrangler.toml
npx wrangler r2 bucket create sarkari-job-docs

# Secrets (never put these in wrangler.toml)
openssl rand -base64 48 | npx wrangler secret put ADMIN_API_KEY   # required, >= 32 chars
npx wrangler secret put GEMINI_API_KEY                             # AI mentor
openssl rand -hex 16 | npx wrangler secret put REPORT_SALT         # optional

npm run db:migrate:remote
npm run deploy
```

Then set `ADMIN_ALLOWED_ORIGINS` in `wrangler.toml` to your admin panel's URL and redeploy.

### Upgrading a database created with the old `schema.sql`

Migrations are idempotent on such a database: `0001` creates nothing that already exists, and
`0002` adds columns and unique indexes. `0002` also:

- deletes `job-imp-*` rows still in `pending_review` (they came from the old placeholder importer, not from real portals);
- moves the 16 sample jobs that the old `schema.sql` seeded as `ACTIVE` back to `pending_review`, so an admin verifies them before they are public.

Back up first: `npx wrangler d1 export sarkari_jobs_db --remote --output backup.sql`.

## Security model

- **Admin auth:** two ways.
  - People sign in with email and password (`POST /api/admin/auth/login`). This creates a session in D1 (`admin_sessions`, token stored only as SHA-256) and sets an `HttpOnly; Secure; SameSite=Strict` cookie. Writes made with the cookie must come from an allowed `Origin`.
  - Automation uses `Authorization: Bearer <ADMIN_API_KEY>` (header only, constant-time compare). It acts as an owner, so use it to create the first account.
  - Passwords are PBKDF2-SHA256 with 100k iterations. Accounts lock for 15 minutes after 5 failures. See `src/auth.js` and `src/routes/adminAuth.js`.
- **CORS:** public routes allow any origin. Admin routes reflect only the origins in `ADMIN_ALLOWED_ORIGINS`.
- **Rate limits** (per IP, per Cloudflare location): public 120/min, admin 120/min, sign-in 10/min, AI 6/min, reports 20/day.
- **Uploads:** admin-only, PDF magic bytes required, max 10 MB, keys generated server-side. Files are always served as `application/pdf` with a sandboxing CSP.
- **Errors:** internal details are logged and never returned to clients; responses carry a `requestId`.

## Importer

The cron runs every 3 hours; each source is fetched only when its `polling_interval_hours` has passed. For each enabled source the scraper:

1. fetches `listing_url` (15 s timeout, 3 MB cap, HTML only);
2. extracts links whose text looks like a notice (recruitment, admit card, result, answer key, syllabus, admission) and drops navigation, tender and RTI links;
3. keeps only links on the source's own domain or on `.gov.in` / `.nic.in`;
4. inserts them as `pending_review` with `verified = 0`. Unique indexes on the notice URL and content hash make duplicates impossible, even with concurrent runs.

Nothing is published until an admin approves it. Some portals block automated requests (for example HTTP 403). These failures appear per source in `last_error` and in the Runs page; add those notices manually from the admin panel.

## Local development

```bash
echo 'ADMIN_API_KEY="local_dev_key_at_least_32_characters_long"' > .dev.vars
npm run db:migrate:local
npm run db:seed:local       # optional sample data
npm run dev                 # http://localhost:8787
./test/run-local.sh         # starts wrangler dev, runs test/smoke.sh, stops it
```

`RUN_IMPORT=1 IMPORT_SOURCE=src-lic ./test/run-local.sh` also runs a real scrape of one source.
