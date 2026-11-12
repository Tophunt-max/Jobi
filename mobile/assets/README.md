# Assets

Most images are original artwork drawn in code (`scripts/assets/*.mjs`) and rendered with
[sharp](https://sharp.pixelplumbing.com/). The brand artwork (app icons, logo, splash laurels,
building and wave) is drawn in `brand-art.mjs` from the uploaded designs in `source/reference/`,
which were too small (42–175 px) to use directly. Hand-made PNGs in `source/` are converted to
WebP by `scripts/assets/convert.mjs`. Do not edit the files in `app/` or `images/`
by hand. Change the drawing code or replace a file in `source/`, then run:

```bash
npm run assets
```

## Adding a new PNG

1. Put the PNG in `source/<group>/` with a kebab-case name (for example `source/splash/logo.png`).
2. Add a line to `PNG` in `scripts/assets/convert.mjs` with the on-screen size in dp.
3. Run `npm run assets`, then add the WebP to `src/assets/images.ts`.

Export sources at **3× the on-screen size** or larger (for example 840×840 px for a 280 dp
image). The script trims transparent borders, clears near-invisible pixels, never upscales,
and lists every source that is too small to stay sharp.

## Layout

```
assets/
├── source/                 Hand-made PNG originals (not bundled; converted by convert.mjs)
│   ├── onboarding/jobs.png            → images/onboarding/jobs.webp
│   └── reference/                     Uploaded brand designs (low-res), redrawn in brand-art.mjs
├── app/                    Store icons and native splash (PNG; the stores require PNG)
│   ├── icon.png                       iOS / Expo icon, 1024×1024
│   ├── android-icon-{foreground,background,monochrome}.png
│   ├── splash-icon.png                Native splash logo (blue background is set in app.config.ts)
│   └── favicon.png                    Web
└── images/                 In-app images (WebP, rendered at 3x)
    ├── brand/              logo-mark (header and About; same art as the app icon)
    ├── splash/             background, logo, laurel-left/right, government-building, tricolor-wave
    ├── onboarding/         jobs, eligibility, alerts
    ├── home/               header, hero
    ├── quick/              Home shortcut tiles (latest-jobs, results, admit-cards, answer-key, syllabus, exam-calendar, …)
    ├── categories/         Category illustrations (central-govt, state-govt, railway, banking, defence, police, …)
    ├── emblems/            Circular organisation emblems, one per category
    ├── content/            Single-colour list icons, tinted at runtime (result, admit-card, pdf, answer-key, …)
    ├── lists/              Full-colour list icons, one file per row tone (red, green, purple, blue, orange)
    │   ├── results/
    │   ├── admit-cards/
    │   └── syllabus/
    ├── notifications/      Notification type icons
    ├── ai/                 robot (AI assistant)
    ├── eligibility/        checklist
    ├── profile/            avatar (default)
    └── states/             empty (empty lists)
```

Code reaches the images through `src/assets/images.ts`. Add new files there as well, because
Metro only bundles images that are `require`d with a static path.

The emblems are generic artwork per category. They are not official logos, and the State
Emblem of India is deliberately not used.
