# Assets

Every image is original artwork drawn in code (`scripts/assets/*.mjs`) and rendered with
[sharp](https://sharp.pixelplumbing.com/). Do not edit the image files by hand. Change the
drawing code and run:

```bash
npm run assets
```

## Layout

```
assets/
├── app/                    Store icons and native splash (PNG; the stores require PNG)
│   ├── icon.png                       iOS / Expo icon, 1024×1024
│   ├── android-icon-{foreground,background,monochrome}.png
│   ├── splash-icon.png                Native splash logo (blue background is set in app.config.ts)
│   └── favicon.png                    Web
└── images/                 In-app images (WebP, rendered at 3x)
    ├── brand/              logo-tile, logo-mark, laurel
    ├── splash/             background (branded splash screen)
    ├── onboarding/         jobs, eligibility, alerts
    ├── home/               header, hero
    ├── quick/              Home shortcut tiles (latest-jobs, results, admit-cards, answer-key, syllabus, exam-calendar, …)
    ├── categories/         Category illustrations (central-govt, state-govt, railway, banking, defence, police, …)
    ├── emblems/            Circular organisation emblems, one per category
    ├── content/            Single-colour list icons, tinted at runtime (result, admit-card, pdf, answer-key, …)
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
