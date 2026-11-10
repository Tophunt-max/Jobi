# RozgarOne production setup: secrets and variables

This guide lists every secret, variable and binding used by the Cloudflare Workers and by
GitHub Actions, where to get each value, and what still has to be done for production.

## Live services

| Service | URL | Connected to the backend through |
|---|---|---|
| Backend API (`rozgarone-worker`) | https://rozgarone-worker.bubunmohanta14.workers.dev | D1 database `rozgarone_db` |
| Admin panel (`rozgarone-admin`) | https://rozgarone-admin.bubunmohanta14.workers.dev | Service binding `BACKEND` |
| Web app (`rozgarone-web`) | https://rozgarone-web.bubunmohanta14.workers.dev | Backend URL built into the app |
| Android / iOS app (Expo) | Built by GitHub Actions | Backend URL built into the app |

## 1. Cloudflare Workers

You can add secrets and variables in two ways:

- **Dashboard:** Workers & Pages → worker name → Settings → Variables and Secrets.
- **Command line:** run `npx wrangler secret put NAME` inside `backend-cloudflare/`.

Never put secrets in `wrangler.toml` or in chat. Enter them only in the dashboard or with
`wrangler secret put`.

### 1.1 `rozgarone-worker` (backend)

| Name | Type | Required | What it does |
|---|---|---|---|
| `ADMIN_API_KEY` | Secret | Yes | Master key for the admin API, at least 32 characters. Use it only for automation and to create the first admin user. Day-to-day admins log in with username and password. |
| `REPORT_SALT` | Secret | Recommended | Salt for hashing IP addresses in reports and login logs. Set it once and do not change it, or old hashes will no longer match new ones. |
| `GEMINI_API_KEY` | Secret | For AI Mentor | Google Gemini key. Without it, `POST /api/ai/generate` returns 503 "AI mentor is not configured". |
| `ENVIRONMENT` | Variable (`wrangler.toml`) | Yes | `production` |
| `ADMIN_ALLOWED_ORIGINS` | Variable (`wrangler.toml`) | Yes | Comma-separated list of websites allowed to send write requests to `/api/admin/*`. Add your custom domain here when you have one. |
| `GEMINI_MODEL` | Variable (`wrangler.toml`) | Yes | Gemini model name, `gemini-2.5-flash` |
| `DB` | D1 binding | Yes | Database `rozgarone_db` |
| `FILES_BUCKET` | R2 binding | Optional | R2 bucket for notice PDF uploads. Without it, PDF upload returns 503 and everything else still works. |
| `PUBLIC_RATE_LIMITER`, `ADMIN_RATE_LIMITER`, `LOGIN_RATE_LIMITER`, `AI_RATE_LIMITER` | Rate limit bindings | Yes | Per-IP rate limits: 120, 120, 10 and 6 requests per minute. |

**Generate and set the secrets:**

```bash
cd backend-cloudflare

# ADMIN_API_KEY (64 random characters)
python3 -c "import secrets;print(secrets.token_urlsafe(48),end='')" | npx wrangler secret put ADMIN_API_KEY

# REPORT_SALT (set once only)
python3 -c "import secrets;print(secrets.token_hex(16),end='')" | npx wrangler secret put REPORT_SALT

# GEMINI_API_KEY (paste the key when prompted)
npx wrangler secret put GEMINI_API_KEY
```

Store `ADMIN_API_KEY` in a password manager. To rotate it, run the same command again with a
new value. The new key works as soon as the command finishes.

**Gemini key:**

1. Create a key at https://aistudio.google.com/apikey.
2. For production, turn on billing in the Google Cloud project.
3. Restrict the key to the "Generative Language API".
4. The `AI_RATE_LIMITER` binding (6 requests per minute per IP) keeps costs under control.

**R2 for PDF uploads (optional):**

1. Enable R2 in the Cloudflare dashboard.
2. Create the bucket: `npx wrangler r2 bucket create rozgarone-docs`
3. Uncomment the `[[r2_buckets]]` block in `backend-cloudflare/wrangler.toml`.
4. Redeploy: `npx wrangler deploy`

### 1.2 `rozgarone-admin` (admin panel)

This worker needs no secrets. It reaches the backend through the service binding
`BACKEND`, which points to `rozgarone-worker`. Admins log in with users stored in the
`admin_users` table.

To create the first admin user:

```bash
curl -X POST https://rozgarone-worker.bubunmohanta14.workers.dev/api/admin/users \
  -H "Authorization: Bearer $ADMIN_API_KEY" -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"at-least-12-characters","role":"owner"}'
```

To reset a password without the panel, see "First owner / lost access" in `admin-panel/README.md`.

### 1.3 `rozgarone-web` (web app)

This worker needs no secrets. It only serves static files. Its config is in
`mobile/wrangler.toml`.

### 1.4 Deploy commands

```bash
# Backend
cd backend-cloudflare && npm ci && npx wrangler deploy
npx wrangler d1 migrations apply rozgarone_db --remote   # only when a new migration is added

# Admin panel (deploy the backend first)
cd admin-panel && npm ci && npm run build && npx wrangler deploy

# Web app
cd mobile && npm ci && APP_VARIANT=production npx expo export --platform web && npx wrangler deploy
```

Back up the database before running migrations:

```bash
npx wrangler d1 export rozgarone_db --remote --output backup.sql
```

## 2. GitHub Actions

### 2.1 Create the `release` environment

1. Go to **Settings → Environments → New environment** and name it `release`.
2. Under **Required reviewers**, add yourself so that no release runs without your approval.
3. Under **Deployment branches and tags**, allow only tags matching `v*`.

The Android signing, iOS and store upload secrets go in this environment.

### 2.2 Variables

Location: **Settings → Secrets and variables → Actions → Variables**

| Name | Required | Value / purpose |
|---|---|---|
| `API_BASE_URL` | No (default is in code) | `https://rozgarone-worker.bubunmohanta14.workers.dev`. Change it when you add a custom domain. |
| `ADMOB_ANDROID_APP_ID` | For ad revenue | Format `ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX` |
| `ADMOB_BANNER_ANDROID` | For ad revenue | Format `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX` |
| `ADMOB_INTERSTITIAL_ANDROID` | For ad revenue | Same format as above |
| `ADMOB_REWARDED_ANDROID` | For ad revenue | Same format as above |
| `ADMOB_IOS_APP_ID` | For iOS ad revenue | Format `ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX` |
| `ADMOB_BANNER_IOS`, `ADMOB_INTERSTITIAL_IOS`, `ADMOB_REWARDED_IOS` | For iOS ad revenue | Format `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX` |
| `VERSION_CODE_OFFSET` | No | Default `0`. Change it only if you need to raise the Play Store version code. |
| `ANDROID_PACKAGE` | No | Default `com.rozgarone.app`. Do not change it after the first Play Store release. |

If the AdMob IDs are not set, the app shows Google's test ads and earns nothing. An ID in the
wrong format makes the build fail on purpose.

### 2.3 Repository secrets (web auto-deploy)

Location: **Settings → Secrets and variables → Actions → Secrets → Repository secrets**

Add these at the **repository level**, not in the `release` environment, because the web
deploy job does not use an environment.

| Name | Where to get it |
|---|---|
| `CLOUDFLARE_API_TOKEN` | Cloudflare → My Profile → API Tokens → Create Token → template **"Edit Cloudflare Workers"**. Under Account Resources, select only your account. Create a separate token for CI. |
| `CLOUDFLARE_ACCOUNT_ID` | Cloudflare dashboard → Workers & Pages → "Account ID" on the right side |

After this, every push to `main` deploys the web app.

### 2.4 Secrets in the `release` environment

**Android signing (required for the Play Store):**

| Name | Purpose |
|---|---|
| `UPLOAD_KEYSTORE_BASE64` | The upload keystore file, base64-encoded |
| `UPLOAD_STORE_PASSWORD` | Keystore password |
| `UPLOAD_KEY_PASSWORD` | Key password. If empty, the store password is used. |
| `UPLOAD_KEY_ALIAS` | Key alias, default `upload` |

Create the keystore once:

```bash
keytool -genkeypair -v -keystore upload.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 upload.jks   # put this output in UPLOAD_KEYSTORE_BASE64
```

- Keep an offline backup of `upload.jks` and its passwords.
- Turn on **Play App Signing** in the Play Console. If the upload key is lost, Google can reset it.

**Google Play upload (optional):**

| Name | Where to get it |
|---|---|
| `PLAY_SERVICE_ACCOUNT_JSON` | 1. In Google Cloud Console, create a service account and download its JSON key. 2. In Play Console → Users and permissions, invite the service account's email. 3. Give it only the **"Release to testing tracks"** permission, only for the RozgarOne app. |

- You must upload the first AAB to the Play Console by hand.
- After that, every release tag uploads a draft to the internal track automatically.

**iOS (optional, needs an Apple Developer account, $99 per year):**

| Name | Where to get it |
|---|---|
| `APPLE_TEAM_ID` | developer.apple.com → Membership |
| `ASC_KEY_ID`, `ASC_ISSUER_ID` | App Store Connect → Users and Access → Integrations → App Store Connect API → new key with role **App Manager** |
| `ASC_KEY_P8_BASE64` | Run `base64 -w0 AuthKey_XXXX.p8` on the downloaded `.p8` file |

Before the first iOS release, create the app record in App Store Connect with bundle ID
`com.rozgarone.app`.

### 2.5 What each trigger does

| Trigger | Result |
|---|---|
| Pull request | Backend smoke test, admin panel build, mobile typecheck, lint, bundles and Android verification build |
| Push to `main` | Same as a pull request, plus web export and deploy (if the Cloudflare secrets are set) |
| Tag `vX.Y.Z` | Signed AAB and APK attached to a GitHub Release, Play internal draft, iOS TestFlight upload and web deploy |

Release a version:

```bash
git tag v2.0.0 && git push origin v2.0.0
```

Tag `v2.3.4` produces version name `2.3.4` and version code
`VERSION_CODE_OFFSET + 20304`. Each new tag must be higher than the previous one.

## 3. Production checklist

- [ ] Set `GEMINI_API_KEY` on `rozgarone-worker`.
- [ ] Store `ADMIN_API_KEY` in a password manager.
- [ ] Add the `CLOUDFLARE_API_TOKEN` and `CLOUDFLARE_ACCOUNT_ID` repository secrets.
- [ ] Create the `release` environment with required reviewers.
- [ ] Create the Android upload keystore, back it up, and add the 4 signing secrets.
- [ ] Add the real AdMob IDs as variables.
- [ ] Create the Play Console app and upload the first AAB by hand.
- [ ] (Optional) Add `PLAY_SERVICE_ACCOUNT_JSON`.
- [ ] (Optional) Add the Apple secrets for iOS.
- [ ] (Optional) Enable R2 and the `FILES_BUCKET` binding.
- [ ] Protect `main` under Settings → Branches so that CI must pass before merging.
- [ ] Add custom domains (for example `api.rozgarone.in` and `admin.rozgarone.in`), then update `ADMIN_ALLOWED_ORIGINS` and `API_BASE_URL`.
- [ ] Delete the old Cloudflare resources `sarkari-jobs-worker`, `sarkari-jobs-admin` and `sarkari_jobs_db`.
- [ ] Approve the pending jobs in the admin panel so they appear in the apps.

### Known gaps

- The backend and the admin panel are not deployed by CI. Run `wrangler deploy` by hand
  (section 1.4), or add a deploy job that runs after a merge to `main`.
