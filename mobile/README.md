# Sarkari Jobs – Expo app (Android, iOS, web)

React Native rewrite of the original Kotlin app (removed from the repo; see the `v1.1.x` tags), built with Expo SDK 58 and Expo Router.
It talks to the same Cloudflare Worker (`../backend-cloudflare`); no backend change is needed.

## Folder structure

```
mobile/
├── app.config.ts            Build-time config (env vars, plugins, identifiers)
├── plugins/                 Config plugins (Android release signing)
├── scripts/ci-version.sh    Version name/code from the release tag
├── modules/legacy-data/     Android-only native module that imports the Kotlin app's data
├── public/_headers          Security headers for the web build (Cloudflare Pages)
└── src/
    ├── app/                 Routes only (Expo Router). Every file is a screen.
    │   ├── (tabs)/          Home, Jobs, Results, AI Mentor, Profile
    │   ├── job/[id].tsx     Job detail
    │   ├── report/[id].tsx  Report a problem
    │   ├── tools/           Age, salary, physical standards
    │   ├── profile/         Eligibility, document locker, applications
    │   └── alerts, notifications, language, browser
    ├── components/          Shared UI (components/ui is the design system)
    ├── config/env.ts        Typed runtime config
    ├── constants/           States, sectors, categories, stages
    ├── features/            Feature logic and feature components
    │   ├── ads/             AdMob (native) + sponsor card; *.web.ts = no-op on web
    │   ├── alerts/          Job-alert matching
    │   ├── bootstrap/       Start-up tasks (sync, ads, reminders, migration)
    │   ├── browser/         Locked-down in-app WebView (native), new tab (web)
    │   ├── jobs/            Status, filters, job card, open/bookmark hooks
    │   ├── mentor/          AI prompts, offline answers, chat store
    │   ├── migration/       Kotlin-app data import
    │   ├── notifications/   In-app inbox
    │   ├── profile/         Eligibility checks, physical standards, dossier
    │   ├── reminders/       Deadline reminders (expo-notifications; inbox-only on web)
    │   └── tools/           Salary maths
    ├── i18n/                i18next setup and locale files
    ├── lib/                 Dates, formatting, storage, logger, links
    ├── services/api/        HTTP client, endpoints, DTO → model mappers
    ├── store/               Zustand stores persisted with AsyncStorage
    ├── theme/               Light/dark tokens
    └── types/domain.ts      App models
```

## Run locally

```bash
cd mobile
cp .env.example .env          # set EXPO_PUBLIC_API_BASE_URL
npm ci
npm run web                   # web in the browser
npx expo run:android          # needs Android Studio / SDK
npx expo run:ios              # needs macOS + Xcode
```

AdMob, notifications, the WebView and the legacy module are native code, so the app
does not run in Expo Go. Use a development build (`npx expo run:android` / `npx expo run:ios`).

Checks: `npm run typecheck`, `npm run lint`, `npx expo-doctor`, `npm run export:web`.

## Configuration

All values are read in `app.config.ts`. Set them in `.env` locally; in CI they come from
GitHub repository variables and secrets (see below).

| Name | Needed | Notes |
|---|---|---|
| `APP_VARIANT` | yes | `development`, `preview` or `production` (CI uses `production`) |
| `EXPO_PUBLIC_API_BASE_URL` | yes | Worker URL, must be `https://` for production |
| `ADMOB_ANDROID_APP_ID`, `ADMOB_IOS_APP_ID` | production | Non-production builds always use Google test IDs |
| `ADMOB_{BANNER,INTERSTITIAL,REWARDED}_{ANDROID,IOS}` | production | Empty falls back to test units |
| `VERSION_NAME`, `VERSION_CODE` | CI | Set from the release tag by `scripts/ci-version.sh` |
| `APPLE_TEAM_ID` | iOS release | Lets Xcode sign automatically |
| `ANDROID_PACKAGE`, `IOS_BUNDLE_ID` | no | Default keeps the Kotlin app's ID `com.aistudio.sarkarijobportal.kxrtzp` |

Production config fails fast on a missing/non-https API URL or a malformed AdMob ID.

## CI/CD (GitHub Actions, no EAS)

`.github/workflows/mobile.yml` builds everything on GitHub runners:

| Trigger | What happens |
|---|---|
| PR or push touching `mobile/` | Typecheck, lint, `expo-doctor`, JS bundles, Android release APK (debug-signed, for verification) |
| Push to `main` | Same, plus web export and deploy to Cloudflare Pages |
| Tag `vX.Y.Z` | Signed AAB + APK attached to a GitHub Release, AAB uploaded to Google Play internal track (draft), iOS archive uploaded to App Store Connect / TestFlight, web deploy |
| Manual run | Same as a push, plus the iOS job (simulator build unless Apple secrets are set) |

Native projects are generated in CI with `expo prebuild`; `android/` and `ios/` are never committed.
Release signing is added by `plugins/withAndroidReleaseSigning.js`, which reads the keystore
from environment variables at build time.

**Version numbers:** tag `v2.3.4` gives version name `2.3.4` and version code
`VERSION_CODE_OFFSET + 2*10000 + 3*100 + 4` (20304). The Kotlin app used small codes
(GitHub run numbers), so `v2.0.0` → 20000 is already higher. If Play reports a conflict,
set the `VERSION_CODE_OFFSET` variable.

### Repository settings

Put the secrets in a GitHub environment named `release` (the signing jobs use it), so you
can require approval before a release runs.

| Kind | Name | Used for |
|---|---|---|
| Variable | `API_BASE_URL` | Worker URL (required for tags) |
| Variable | `ADMOB_ANDROID_APP_ID` (or old `ADMOB_APP_ID`), `ADMOB_BANNER_ANDROID` (or `ADMOB_BANNER_ID`), `ADMOB_INTERSTITIAL_ANDROID`, `ADMOB_REWARDED_ANDROID`, `ADMOB_*_IOS` | Real ads |
| Variable | `CF_PAGES_PROJECT` (default `sarkari-jobs-web`), `VERSION_CODE_OFFSET` | Optional |
| Secret | `UPLOAD_KEYSTORE_BASE64`, `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_PASSWORD`, `UPLOAD_KEY_ALIAS` (default `upload`) | Android signing. Same upload key as the Kotlin app. |
| Secret | `PLAY_SERVICE_ACCOUNT_JSON` | Optional Google Play upload |
| Secret | `APPLE_TEAM_ID`, `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_KEY_P8_BASE64` | Optional iOS signing and TestFlight upload (App Store Connect API key, App Manager role) |
| Secret | `CLOUDFLARE_API_TOKEN`, `CLOUDFLARE_ACCOUNT_ID` | Optional web deploy (token with Pages edit permission) |

Optional steps are skipped with a warning when their secrets are missing.

### Making a release

```bash
git tag v2.0.0 && git push origin v2.0.0
```

1. The first time, open the Play Console internal track and test the upgrade from the
   Kotlin app (see below) before promoting the draft.
2. For iOS, the app record must already exist in App Store Connect with the same bundle ID.

### Local release builds

```bash
npx expo prebuild -p android --clean
ANDROID_KEYSTORE_PATH=/path/upload.jks ANDROID_KEYSTORE_PASSWORD=… ANDROID_KEY_PASSWORD=… \
  (cd android && ./gradlew :app:bundleRelease)
```

## Upgrade from the Kotlin app

On first launch, `src/features/migration/legacy.ts` calls the `legacy-data` module, which reads:

- SharedPreferences `user_private_data`: eligibility profile, document locker, applied exams
- SharedPreferences `targeted_job_alert_prefs`: alert preferences
- Room DB `sarkari_job_portal.db`: bookmarked jobs and read notifications

The data is saved into the new stores. The old copies are deleted on the next launch,
after the new data is on disk. Bookmarks then get fresh reminders.

Test it before release: install the last Kotlin APK (GitHub release `v1.1.2`), add a profile, locker entries, an
application and bookmarks, then install the Expo build over it with the same signing key.

## Behaviour changes from the Kotlin app

- Job status (open / closing soon / closed) is computed from the date when shown, and all
  dates are normalised to ISO, so "closing soon" sorting is correct.
- The chosen language is saved. English and Hindi are complete; the other 11 languages
  have the old app's translated labels and fall back to English for the rest.
- Ad unit IDs are fixed at build time. The admin panel can still switch formats on/off,
  change frequency and run the sponsor card, but it can no longer swap AdMob unit IDs.
- Every job opening goes through one interstitial rule (the Profile screen used to skip it).
- The rewarded "solved model paper" unlock was removed: it opened the official syllabus PDF,
  so the label was misleading. `showRewarded()` is ready for a genuine reward.
- PDFs open in the system browser instead of the Google Docs viewer.
- The AI prompt no longer includes the candidate's name or document numbers.
- Web: no AdMob and no system reminders (browsers cannot schedule them); deadline alerts
  still appear in the in-app inbox.
