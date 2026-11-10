# Sarkari Jobs

| Part | Folder | Docs |
|---|---|---|
| Mobile app for Android, iOS and web (Expo, React Native) | `mobile/` | [README](mobile/README.md) |
| Backend (Cloudflare Worker + D1 + R2) | `backend-cloudflare/` | [CLOUDFLARE_SETUP.md](backend-cloudflare/CLOUDFLARE_SETUP.md) |
| Admin panel (static Next.js on Cloudflare) | `admin-panel/` | [README](admin-panel/README.md) |

Deploy in this order: backend, then admin panel, then the app.

The Gemini key lives on the Worker (`wrangler secret put GEMINI_API_KEY`). The app bundle contains no secrets.

## Data flow

On launch, on pull-to-refresh and when it returns to the foreground (at most every 15 minutes), the app calls `GET /api/sync/all` and caches the result on the device. Bookmarks, read state, the eligibility profile, the document locker and the applications tracker are stored on the device only and are never uploaded. If the network fails, the app shows cached data with an offline message.

Deadline reminders fire at 09:00, 3 days and 1 day before the last date of each saved notice (Android and iOS).

## Previous Kotlin app

The original Android app (Kotlin, Jetpack Compose, Room) was removed from this repository. It is still in git history and in the `v1.1.x` tags. The Expo app keeps its applicationId and imports its on-device data on first launch; see [Upgrade from the Kotlin app](mobile/README.md#upgrade-from-the-kotlin-app).

## CI

- `.github/workflows/ci.yml`: Worker migrations and API smoke test; admin panel typecheck and static build.
- `.github/workflows/mobile.yml`: Expo typecheck, lint, `expo-doctor`, and Android/iOS/web bundles, on changes under `mobile/`.

App store builds run on EAS (`npx eas-cli@latest build`); see the mobile README.
