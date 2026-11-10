# Sarkari Jobs – Expo app (Android, iOS, web)

React Native rewrite of the Kotlin app in `../app`, built with Expo SDK 58 and Expo Router.
It talks to the same Cloudflare Worker (`../backend-cloudflare`); no backend change is needed.

## Folder structure

```
mobile/
├── app.config.ts            Build-time config (env vars, plugins, identifiers)
├── eas.json                 EAS build profiles: development, preview, production
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
does not run in Expo Go. Use a development build (`npx expo run:*` or
`npx eas-cli@latest build --profile development`).

Checks: `npm run typecheck`, `npm run lint`, `npx expo-doctor`, `npm run export:web`.

## Configuration

All values are read in `app.config.ts`. Set them in `.env` locally or as
[EAS environment variables](https://docs.expo.dev/eas/environment-variables/) for cloud builds.

| Name | Needed | Notes |
|---|---|---|
| `APP_VARIANT` | yes | `development`, `preview` or `production` (set by `eas.json`) |
| `EXPO_PUBLIC_API_BASE_URL` | yes | Worker URL, must be `https://` for production |
| `ADMOB_ANDROID_APP_ID`, `ADMOB_IOS_APP_ID` | production | Non-production builds always use Google test IDs |
| `ADMOB_{BANNER,INTERSTITIAL,REWARDED}_{ANDROID,IOS}` | production | Empty falls back to test units |
| `EAS_PROJECT_ID`, `EXPO_OWNER` | EAS | From `npx eas-cli@latest init` |
| `ANDROID_PACKAGE`, `IOS_BUNDLE_ID` | no | Default keeps the Kotlin app's ID `com.aistudio.sarkarijobportal.kxrtzp` |

Production config fails fast on a missing/non-https API URL or a malformed AdMob ID.

## Releasing

### Android (replaces the Kotlin app on the Play Store)

The Expo app keeps the same `applicationId`, so it ships as an update and the user keeps their data:

1. Upload the existing upload keystore to EAS: `npx eas-cli@latest credentials -p android`
   (alias `upload`). Without the same key, Play rejects the update.
2. Set the starting version code above the last Kotlin release:
   `npx eas-cli@latest build:version:set -p android`. After that `autoIncrement` handles it.
3. `npx eas-cli@latest build -p android --profile production`
4. Release to the internal track first and test the upgrade path (see below).

### iOS

`npx eas-cli@latest build -p ios --profile production`, then `eas submit -p ios`.
Register the bundle ID (or set `IOS_BUNDLE_ID`) in App Store Connect first.

### Web

```bash
EXPO_PUBLIC_API_BASE_URL=https://… npx expo export --platform web
npx wrangler pages deploy dist --project-name sarkari-jobs-web
```

The export is a single-page app; Cloudflare Pages serves `index.html` for unknown paths.
The Worker's public API already allows any origin, so no CORS change is needed.

## Upgrade from the Kotlin app

On first launch, `src/features/migration/legacy.ts` calls the `legacy-data` module, which reads:

- SharedPreferences `user_private_data`: eligibility profile, document locker, applied exams
- SharedPreferences `targeted_job_alert_prefs`: alert preferences
- Room DB `sarkari_job_portal.db`: bookmarked jobs and read notifications

The data is saved into the new stores. The old copies are deleted on the next launch,
after the new data is on disk. Bookmarks then get fresh reminders.

Test it before release: install the last Kotlin APK, add a profile, locker entries, an
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
