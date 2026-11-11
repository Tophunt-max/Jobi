# Deploying the Cloudflare Workers with Workers Builds

The three Workers are built and deployed by Cloudflare itself
([Workers Builds](https://developers.cloudflare.com/workers/ci-cd/builds/)). GitHub Actions does not deploy them.
GitHub Actions only builds the Android AAB/APK (and runs checks on pull requests).

| Worker | Folder | What it serves |
|---|---|---|
| `rozgarone-worker` | `backend-cloudflare/` | API, D1 database `rozgarone_db`, import cron |
| `rozgarone-admin` | `admin-panel/` | Admin panel (static Next.js export + `/api/*` proxy to the backend) |
| `rozgarone-web` | `mobile/` | Web build of the Expo app |

When a commit lands on `main`, each Worker rebuilds only if files in its own folder changed.

## One-time setup (Cloudflare dashboard)

### 1. API token for the builds

The token that Workers Builds creates automatically cannot run D1 migrations. Create your own:

1. **My Profile → API Tokens → Create Token → Create Custom Token.**
2. Name it `workers-builds-rozgarone` and add these permissions:

   | Scope | Permission | Access |
   |---|---|---|
   | Account | Workers Scripts | Edit |
   | Account | D1 | Edit |
   | Account | Account Settings | Read |
   | Account | Workers R2 Storage | Edit (only once R2 is enabled) |
   | User | User Details | Read |
   | User | Memberships | Read |

3. Under **Account Resources**, include only your account. Create the token.

### 2. Connect each Worker to the repository

For each of the three Workers:

1. **Workers & Pages →** select the Worker **→ Settings → Build → Connect.**
2. Choose **GitHub**, authorise the Cloudflare app for `Tophunt-max/RozgarOne` only, and pick branch `main`.
3. Enter the settings from the table below and select the API token from step 1.
4. Save.

| Setting | `rozgarone-worker` | `rozgarone-admin` | `rozgarone-web` |
|---|---|---|---|
| Root directory | `backend-cloudflare` | `admin-panel` | `mobile` |
| Build command | *(empty)* | `npm run build` | `npm run build:web` |
| Deploy command | `npm run deploy:ci` | `npm run deploy` | `npm run deploy:web` |
| Build watch paths: include | `backend-cloudflare/*` | `admin-panel/*` | `mobile/*` |
| Build watch paths: exclude | *(empty)* | *(empty)* | `mobile/scripts/*` |
| Non-production branch builds | **Off** | Off | Off |

Notes on these settings:

- **Dependencies:** Workers Builds runs `npm ci` itself, based on each folder's `package-lock.json`.
- **Node version:** Node 22 comes from the `.nvmrc` file in each folder.
- **Migrations:** `deploy:ci` applies pending D1 migrations before deploying the backend. Take a backup before merging a migration, using `npx wrangler d1 export rozgarone_db --remote --output backup.sql`.
- **Preview builds:** keep non-production branch builds **off** for the backend, because a preview would run against the production database.
- **Worker names:** each Worker name must match `name` in that folder's `wrangler.toml`. The three names already match.

### 3. Runtime secrets (unchanged)

Secrets live on the Worker, not in the build:
**Settings → Variables and Secrets** on `rozgarone-worker`: `ADMIN_API_KEY`, `REPORT_SALT`, `GEMINI_API_KEY`.
The other two Workers need none. Build variables are not required. The web app's API URL defaults to the
production Worker and can be overridden with an `EXPO_PUBLIC_API_BASE_URL` build variable on `rozgarone-web`.

### 4. First deploy and check

1. Push any commit to `main` that touches each folder, or open the Worker and select **Deployments → View build history → Retry build**.
2. Confirm each build is green. Then check:
   - https://rozgarone-worker.bubunmohanta14.workers.dev/ returns `"name":"RozgarOne API"`.
   - https://rozgarone-admin.bubunmohanta14.workers.dev/ loads the login page.
   - https://rozgarone-web.bubunmohanta14.workers.dev/ loads the app.
3. Build logs are under **Deployments → View build history**. Cloudflare also posts a check run on the GitHub commit.

## Deploy order

The admin panel calls the backend through a service binding, so a new backend route must be live before the
admin panel uses it. When one commit changes both, the two builds start together. If the admin build finishes first and
the new route 404s, select **Retry build** on `rozgarone-admin` after the backend is done.

## Manual deploy (fallback)

```bash
cd backend-cloudflare && npm ci && npm run deploy:ci
cd admin-panel && npm ci && npm run build && npm run deploy
cd mobile && npm ci && npm run build:web && npm run deploy:web
```

## Rollback

Go to the Worker's **Deployments** page, open an earlier version and select **Deploy**, or run
`npx wrangler rollback` in the Worker's folder. D1 migrations are not rolled back automatically.
