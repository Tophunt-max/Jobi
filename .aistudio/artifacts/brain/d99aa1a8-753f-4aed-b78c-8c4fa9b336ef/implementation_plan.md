# Latest Jobs Page UI Overhaul & Bugfix Plan

Resolve all visual layout bugs, badge clipping, search/filter desync, and header overlap issues on the **Latest Jobs** screen (`JobListingScreen.kt`) to deliver a bug-free, candidate-centric experience.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Card Action Style (Confirmed)**: Clean, focused card design with only the **Bookmark toggle button** and **"View Details" button**, eliminating visual clutter and accidental mis-clicks.
> - **Filter Organization (Confirmed)**: **Two dedicated horizontal scroll rows**:
>   - Row 1: **Categories** with live vacancy count badges (`All`, `UPSC`, `SSC`, `Railways`, `Banking`, `Teaching`, `Police/Defence`, etc.).
>   - Row 2: **Indian States & Central** with live vacancy count badges (`All India`, `UP`, `Bihar`, `Rajasthan`, `MP`, `Delhi`, etc.).
> - **Bugfixes Addressed (Confirmed 1, 2, 3)**:
>   1. Fix card overflow, badge collisions, and line-wrapping issues on narrow screens.
>   2. Eliminate duplicate search header, streamline status tabs ("All", "Active", "Closing Soon", "Upcoming").
>   3. Separate job vacancies from admit cards and results (with smart redirection banner to Results & Cards tab).

---

### 1. Overview & Core Concept

- **What It Does**: Transforms the "Latest Jobs" screen into a fast, fluid, and robust directory of verified government vacancies with zero layout clipping, instant reactive filtering, and smooth navigation into detailed vacancy briefings.
- **Target Persona**: Government job seekers filtering opportunities across central ministries and specific state commissions.
- **Key Value**: Clean visual hierarchy, prominent deadline countdowns, and responsive layout across all Android device screen sizes.

---

### 2. User Experience & Visual Design

#### Header & Search Area
- **Unified Single Header**: Compact, high-contrast search field (`Slate50` background, rounded 14dp, active clear icon) seamlessly integrated with the top status indicator.
- **Status Filter Tabs**:
  - `All Jobs`, `Active Openings`, `Closing Soon (🚨)`, and `Upcoming Notifications`.
  - Material 3 sliding pill indicator in `DeepNavy`.

#### Dual Horizontal Filter Rows
- **Category Filter Row**:
  - Chips with emoji icons and dynamic vacancy count badges (e.g. `🏛️ UPSC (18)`, `🚆 Railways (42)`).
  - Selected chip in solid `DeepNavy` with white text.
- **State Filter Row**:
  - Chips for `All India` and all 28+ states/UTs with live vacancy count tags.
  - Full two-way state binding with `JobViewModel.uiState.selectedState` so selecting a state from Home Screen properly reflects here.

#### Card Component Redesign (`GovernmentJobOpeningCard`)
- **Header**: Organization monogram inside gradient avatar (UPSC, SSC, RRB, etc.) + Org name + Advt number + Verified blue shield badge.
- **Status Pill**: Dynamic pill with pulsing dot (`Active` green, `Closing Soon` red, `Upcoming` blue).
- **Title**: Up to 2 lines with `TextOverflow.Ellipsis` and 22sp line height.
- **Responsive Metadata Badges**:
  - Vacancy badge (`👥 4,120 Posts`), Qualification badge (`🎓 Graduate`), and Location badge (`📍 Central / State`).
  - Implemented with flexible spacing to avoid overlapping or clipping on small screens.
- **Footer Container**:
  - Left: Calendar icon + "Closes: 28 Oct 2026" with high-contrast text.
  - Right: Gold Star Bookmark button (48dp touch target) + "View Details →" primary button in `DeepNavy`.

---

### 3. Key Technical Decisions & Trade-Offs

- **Decision 1: Full Two-Way Synchronization with `JobViewModel`**
  - *Chosen Approach*: Bind `searchQuery`, `selectedCategory`, `selectedState`, and `selectedStatus` directly to `JobViewModel` state flows rather than disconnected local screen state.
  - *Why*: Eliminates desync bugs where selecting a state on the Home Screen was ignored when navigating to the Latest Jobs tab.

- **Decision 2: Remove Mixed Admit Card & Result Dumps**
  - *Chosen Approach*: Keep the Latest Jobs list dedicated purely to job opening vacancies. If a query matches admit cards or results, show a prominent quick-switch banner that deep-links directly to the dedicated "Results & Cards" tab.
  - *Why*: Prevents candidate confusion and eliminates fragmented UI lists.

- **Decision 3: Navigation Bar & AdDock Content Padding**
  - *Chosen Approach*: Calculate bottom padding as `PaddingValues(bottom = 110.dp)` to guarantee that the last job card is 100% visible and not obscured by the docked adaptive banner or bottom bar.

---

### 4. Technical Architecture & Component Flow

```
┌────────────────────────────────────────────────────────┐
│             JobListingScreen (Latest Jobs)             │
├────────────────────────────────────────────────────────┤
│ [Search Bar: Query + Clear Button]                     │
│ [Status Tabs: All | Active | Closing Soon | Upcoming]  │
│ [Row 1: Category Filter Chips with Live Count Badges]  │
│ [Row 2: State Filter Chips with Live Count Badges]     │
│ [Active Filter Bar & Reset Button]                     │
├────────────────────────────────────────────────────────┤
│ LazyColumn (Content padding: 110dp bottom)             │
│  ├─ GovernmentJobOpeningCard (Fixed layout & badges)   │
│  ├─ NativeSponsoredJobCard (In-feed sponsor card)      │
│  └─ ResultsTabRedirectBanner (Optional when relevant)  │
└────────────────────────────────────────────────────────┘
```

### Verification & Testing Plan
1. **Compilation Check**: Run `gradle :app:compileDebugKotlin` to verify type safety and resource references.
2. **Unit & Robolectric Tests**: Run `gradle :app:testDebugUnitTest`.
3. **APK Build**: Run `gradle :app:assembleDebug` to ensure package integrity.
