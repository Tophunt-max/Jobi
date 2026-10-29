# Sarkari Jobs

| Part | Folder | Docs |
|---|---|---|
| Android app (Kotlin, Jetpack Compose, Room) | `app/` | this file |
| Backend (Cloudflare Worker + D1 + R2) | `backend-cloudflare/` | [CLOUDFLARE_SETUP.md](backend-cloudflare/CLOUDFLARE_SETUP.md) |
| Admin panel (static Next.js on Cloudflare Pages) | `admin-panel/` | [README](admin-panel/README.md) |

Deploy in this order: backend, then admin panel, then the app.

## Android app

### Configuration

Copy `.env.example` to `.env` (gitignored), or pass the same names as environment variables or `-P` Gradle properties:

| Name | Required | Notes |
|---|---|---|
| `API_BASE_URL` | release builds | Worker URL, `https://…`. Release builds fail without it. |
| `ADMOB_APP_ID`, `ADMOB_*_ID` | for revenue | Release only. Debug builds always use Google's test IDs. |

The APK contains no secrets. The Gemini key lives on the Worker (`wrangler secret put GEMINI_API_KEY`).

### Build

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
# Release (R8 minified), needs the upload keystore:
KEYSTORE_PATH=/path/upload.jks STORE_PASSWORD=… KEY_PASSWORD=… ./gradlew :app:bundleRelease
```

### Data flow

The app has no admin features. On launch and on manual refresh it calls `GET /api/sync/all` once and writes the result to Room in a transaction. Bookmarks and notification read-state are local and survive every sync. If the network fails, the app shows cached data with an offline message; it never substitutes bundled sample data.

The user's eligibility profile, document locker and applied-exam tracker are stored on the device only and excluded from cloud backup.

Deadline reminders fire at 09:00, 3 days and 1 day before the last date of each bookmarked job. They are restored after reboot or app update, and work without the exact-alarm permission (slightly less precise).

### Room migrations

The schema is exported to `app/schemas/`. Any change to an `@Entity` needs a version bump and a `Migration` in `AppDatabase`. Destructive fallback is enabled only from v1.

## CI (`.github/workflows/android-build-apk.yml`)

Every push and PR runs Android unit tests, lint and a debug build; Worker migrations and the API smoke test; and the admin panel typecheck and static build. Pushing a `v*` tag builds a signed AAB and APK and attaches them to a GitHub Release.

Repository settings needed:

- **Variables:** `API_BASE_URL`, optionally `ADMOB_APP_ID`, `ADMOB_BANNER_ID`, `ADMOB_INTERSTITIAL_ID`, `ADMOB_REWARDED_ID`, `ADMOB_NATIVE_ID`.
- **Secrets:** `UPLOAD_KEYSTORE_BASE64`, `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_PASSWORD` (release), and optionally `DEBUG_KEYSTORE_BASE64` so debug builds can be installed over each other.
