# Mobile Application UI Redesign & Feature Enhancement Plan

Based on your preferences, this plan covers a complete overhaul of the Android mobile application UI using **Modern Material 3 with vibrant accents**, incorporating all requested features: **Job card redesign with countdown timers and urgency badges, eligibility & salary tags, quick PDF/share action buttons, live category counters, upgraded details view, and enhanced candidate utility tools**.

---

## 1. Visual Theme & Design System Upgrade
- **Vibrant Material 3 Palette (`ui/theme/Color.kt`, `Theme.kt`)**:
  - High-contrast primary blues (`#1A56DB`, `#2563EB`) paired with emerald success indicators (`#059669`) and urgency amber/crimson tones (`#DC2626`, `#D97706`).
  - Subtle gradient tints and refined border radiuses (16dp–20dp) for cards and modals.
  - Consistent surface elevations and typography hierarchy for high legibility on handheld screens.

---

## 2. Redesigned Job Cards (`ui/components/JobCard.kt`)
- **Urgency & Countdown Badges**:
  - Dynamic deadline indicators: "Closes in 2 days" (Urgent red badge), "Ends in 6 days" (Amber badge), or "Closing: DD MMM" (Slate badge).
- **Live Vacancy & Category Tags**:
  - Bold vacancy highlight chip (e.g. `⚡ 3,450 Posts`) alongside category badge (`Latest Job`, `Admit Card`, `Result`).
- **Eligibility & Salary Tag Chips**:
  - Qualification chip (e.g. `Graduation`, `10th/12th Pass`, `Engineering Degree`).
  - Salary scale chip (e.g. `Level-7 (₹44,900 - ₹1,42,400)`).
  - Age bracket tag (e.g. `18 - 32 Yrs`).
- **Quick Action Bar on Each Card**:
  - **Quick Share**: One-tap share to WhatsApp, Telegram, or system share sheet with formatted notification summary and link.
  - **PDF Notice**: Direct action to open official notification PDF.
  - **Apply Now**: Direct button to launch the official recruitment portal.
  - **Bookmark**: Single-tap save for offline access.

---

## 3. Dynamic Category Navigation & Search (`HomeScreen.kt`, `FilterChipsRow.kt`)
- **Live Vacancy Count Badges on Category Switcher**:
  - Category tabs (`Latest Jobs`, `Admit Cards`, `Results`, `Answer Keys`, `Syllabus`) showing real-time notice counts (e.g., `Jobs (24)`, `Admit Cards (8)`).
- **Quick Filter Bar**:
  - Instant toggle pills for `All India`, `Odisha`, `Central Govt`, `Bank/SSC/UPSC`, and deadline urgency filters (`Closing Soon`).
- **Enhanced Search Experience**:
  - Responsive search field with clear action, vacancy suggestions, and state selector.

---

## 4. Redesigned Job Details Screen (`ui/screens/JobDetailScreen.kt`)
- **Hero Header**:
  - Organization banner with official verification badge, advertisement number, and quick status.
- **Key Information Matrix**:
  - Visual 4-cell metric grid: Total Vacancies, Pay Scale Band, Age Limit, and Application Fee.
- **Application Timeline Stepper**:
  - Visual step-by-step progress tracking: Notification Release ➔ Application Window ➔ Admit Card ➔ Exam Date ➔ Result.
- **Sticky Bottom Action Dock**:
  - Prominent dual CTA: "Apply on Official Portal" & "Official PDF Notice", with floating "Add to Calendar / Set Reminder".

---

## 5. Candidate Utility Tools Hub (`AgeCalculatorSheet.kt`, `SalaryCalculatorSheet.kt`)
- **Age & Eligibility Calculator**:
  - Modern bottom sheet with date picker, category selection (General, OBC, SC/ST, PwD), and real-time eligibility feedback for active notifications.
- **7th CPC In-Hand Salary Calculator**:
  - Interactive level and city tier (X, Y, Z) selectors showing Basic Pay, DA, HRA, TA, and estimated net monthly in-hand salary.

---

## Verification Plan
1. **Compilation**: Execute `compile_applet` to ensure zero compilation or syntax errors across Jetpack Compose components.
2. **Visual & Interaction Verification**: Validate card sizing, responsive padding on diverse device dimensions, ripple effects, and modal dismissals.
