# Implementation Plan - Next.js Web Admin Panel: Ads & Monetization Management

Revise the architecture to implement the complete Ads and Monetization Control Center inside the **standalone Next.js Web Admin Panel** (`/admin-panel`), rather than the Android mobile client. The Next.js panel will store and manage configurations via Cloudflare Worker API & D1 database, and the Android mobile app will fetch this remote configuration in real-time.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Primary Interface**: All AdMob switches, unit ID management, frequency capping, and direct sponsor campaigns will be managed exclusively in the **Next.js Web Admin Panel** at route `/ads` (with a dedicated sidebar navigation item).
> - **Android App Role**: The Android app acts as a pure consumer: it fetches `/api/ads/config` on launch and caches it locally, so any changes made in the Next.js panel reflect immediately across all candidate devices.
> - **Mobile UI Cleanup**: The PIN verification and Admin Ads screen inside the Android app will be cleanly removed to keep the mobile experience dedicated strictly to job aspirants.

---

### 1. Overview & Core Concept

- **What It Does**: Provides portal administrators with a centralized Web Dashboard in Next.js to toggle AdMob ad formats (Banner, Interstitial, Native, Rewarded), configure production unit IDs, tune candidate UX frequency guardrails, and run direct coaching affiliate campaigns.
- **Target Persona**: Portal Administrator & Operations Team managing monetization, sponsor banners, and Google AdMob compliance.
- **Key Value**: 100% remote control over app monetization without needing to rebuild, re-sign, or push new APK updates to the Google Play Store.

---

### 2. User Experience & Visual Design (Next.js Admin Panel)

#### Visual Identity & Theme
- Built using the Next.js admin design system: Dark slate sidebar (`bg-slate-900`), clean white content cards (`bg-white border-slate-200`), royal blue accents (`bg-blue-600`), and emerald status pills (`bg-emerald-500/10 text-emerald-600`).
- Consistent with existing pages (`/jobs`, `/sources`, `/pending`, `/settings`).

#### Web Admin Navigation
- New Sidebar Item: **Ads & Monetization** (`/ads`, Icon: `Megaphone` / `DollarSign`, Badge: `Revenue`).

#### Interactive Sections on `/ads`
1. **Master Revenue Banner**:
   - Master Switch with active pulse indicator (`Monetization Active` vs `Monetization Paused`).
   - Quick Mode Badge: `Google AdMob Official Test Mode` vs `Production Live IDs Active`.
2. **Granular Format Toggles (4 Cards)**:
   - **Adaptive Banner**: Docked above bottom bar.
   - **Full-Screen Interstitials**: Vacancy detail transition ads.
   - **Native In-Feed Sponsored Cards**: Job listings and Home feed.
   - **Rewarded Video Ads**: Syllabus & Mock Paper unlock ads.
3. **UX & Frequency Capping Controls**:
   - Interactive Slider: Job view interval before showing an interstitial (1 to 10 clicks).
   - Interactive Slider: Minimum cooldown gap between interstitials (15s to 180s).
4. **AdMob Unit IDs Configuration Form**:
   - Inputs for Banner, Interstitial, Native, and Rewarded Ad Unit IDs.
   - "One-Click Load Official Test IDs" button.
   - Live validation showing whether an ID is a Google test unit or custom production unit.
5. **Direct Sponsor & Affiliate Campaign Manager**:
   - Toggle: `Enable Direct Sponsor Override`.
   - Fields: Advertiser Name, Campaign Headline, Description, Rating, CTA Button, Emoji, and Destination URL.
   - **Interactive Live Feed Card Preview**: Real-time component showing exactly how candidates see the card on mobile screens.
6. **Diagnostics & Status**:
   - Direct link to test API response (`/api/ads/config`).
   - "Save & Deploy Changes to Mobile Apps" action button with toast notifications.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Cloudflare D1 Backend Storage vs Local Storage**
  - *Chosen Approach*: Persist ad configuration in Cloudflare D1 database (`app_settings` key-value table) via Worker API.
  - *Why*: Allows changes made in the Next.js web panel to propagate instantly to all mobile devices worldwide.
- **Decision 2: Mobile App Offline Resilience**
  - *Chosen Approach*: Android app caches the latest configuration in `SharedPreferences`. If the device is offline or the backend is unreachable, it seamlessly falls back to cached settings or safe official test defaults without crashing.
- **Decision 3: Separation of Concerns**
  - *Chosen Approach*: Remove admin controls from the Android client. The mobile app remains clean and fast for job seekers, while administrative tasks remain strictly on the web portal.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│             Next.js Web Admin Panel                    │
│      Route: /ads (Sidebar > Ads & Monetization)        │
│  - Master & Format Toggles                             │
│  - AdMob Unit IDs & Test Mode                          │
│  - UX Frequency Capping (Sliders)                      │
│  - Direct Sponsor Campaign & Live Mobile Preview       │
└──────────────────────────┬─────────────────────────────┘
                           │ POST /api/admin/ads (X-Admin-Key)
                           ▼
┌────────────────────────────────────────────────────────┐
│           Cloudflare Worker Backend                    │
│  - GET  /api/ads/config  (Public for Android App)       │
│  - GET  /api/admin/ads   (Authenticated for Next.js)   │
│  - POST /api/admin/ads   (Authenticated for Next.js)   │
│  - Cloudflare D1 Storage: `app_settings`               │
└──────────────────────────┬─────────────────────────────┘
                           │ GET /api/ads/config
                           ▼
┌────────────────────────────────────────────────────────┐
│                 Android Mobile Client                  │
│  - AdSettingsRepository fetches /api/ads/config        │
│  - AdManager: dynamic frequency capping & IDs          │
│  - AdaptiveBannerAdView: dynamic show/hide & unit ID   │
│  - NativeSponsoredJobCard: dynamic sponsor campaign   │
└────────────────────────────────────────────────────────┘
```

#### Deliverables & File Changes Planned:
1. **Next.js Web Admin Panel**:
   - `/admin-panel/src/app/ads/page.tsx`: Complete web interface for ad management.
   - `/admin-panel/src/components/Sidebar.tsx`: Add "Ads & Monetization" navigation item.
   - `/admin-panel/src/lib/types.ts`: Define `AdConfig` interface.
   - `/admin-panel/src/lib/api.ts`: Add `getAdConfig()` and `saveAdConfig()` methods.
2. **Cloudflare Worker Backend**:
   - `/backend-cloudflare/src/index.js`:
     - Add `GET /api/ads/config` (Public endpoint for mobile app).
     - Add `GET /api/admin/ads` & `POST /api/admin/ads` (Protected with `X-Admin-Key`).
     - Persist settings in D1 table `app_settings`.
3. **Android Client Synchronization & Cleanup**:
   - `/app/src/main/java/com/example/ads/AdSettingsRepository.kt`: Add network sync method fetching from `/api/ads/config` on startup with local cache fallback.
   - `/app/src/main/java/com/example/ui/screens/ProfileScreen.kt` & `MainActivity.kt`: Clean up mobile PIN dialog and mobile admin entry, maintaining clean candidate UX.

---

## Verification Plan

- **Next.js Web Admin**: Build and test Next.js frontend pages (`npm run build` in `admin-panel`) ensuring zero syntax or TypeScript errors.
- **Backend API**: Verify `GET /api/ads/config` returns expected JSON structure and `POST /api/admin/ads` updates values.
- **Android App**: Run `compile_applet` to verify Android app builds cleanly and correctly handles the remote configuration.
