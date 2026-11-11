# RozgarOne: pending tasks

Status on 11 Nov 2026. Everything below is still open. Secrets and variables are explained in
[PRODUCTION_SETUP.md](PRODUCTION_SETUP.md).

## 1. Finish the translations (blocked until the daily quota resets)

Translation stopped because the Cloudflare Workers AI free plan ran out (10,000 neurons per day;
the quota resets at 00:00 UTC, 5:30 AM IST).

Strings translated, out of 486:

| Language | Code | Done | Missing |
|---|---|---|---|
| English | en | 486 | 0 |
| Hindi | hi | 486 | 0 |
| Odia | or | 486 | 0 |
| Bengali | bn | 318 | 168 |
| Telugu | te | 295 | 191 |
| Marathi | mr | 279 | 207 |
| Tamil | ta | 256 | 230 |
| Gujarati | gu | 196 | 290 |
| Punjabi | pa | 12 | 474 |
| Urdu | ur | 12 | 474 |
| Kannada | kn | 12 | 474 |
| Malayalam | ml | 12 | 474 |
| Assamese | as | 12 | 474 |

Missing strings show in English until they are translated.

**How to run it.** Use a Cloudflare token that can use Workers AI:

```bash
cd mobile
export CLOUDFLARE_ACCOUNT_ID=...   # Cloudflare dashboard → Workers & Pages → Account ID
export CLOUDFLARE_API_TOKEN=...    # token with "Workers AI: Read"
python3 scripts/translate-locales.py bn te mr ta gu      # day 1: finish the partly done ones
python3 scripts/translate-locales.py pa ur kn ml as      # day 2 (or the same day on a paid plan)
```

- One free day covers roughly 5 languages. With the Workers Paid plan ($5/month) everything finishes in one run.
- The script is resumable. It only sends missing strings and saves after every group, so it can be run again after "FAILED" lines.
- It rejects any translation whose keys, `{{placeholders}}` or line breaks differ from English.

**After it finishes:**

1. Check the counts:
   ```bash
   cd mobile/src/i18n/locales && for f in *.json; do python3 -c "import json;n=lambda d:sum(n(v) if isinstance(v,dict) else 1 for v in d.values());print('$f',n(json.load(open('$f'))))"; done
   ```
2. In `mobile/src/i18n/index.ts`, add `complete: true` to every language that reaches 486. That hides the "partly translated" notice on the Language screen.
3. Run `npx tsc --noEmit` and `npx expo lint` in `mobile/`, then commit and open a PR.
4. Ask a native speaker to review at least Home, Job Details and Eligibility Checker in each language. The text is machine translated.

## 2. Cloudflare

- [ ] **Set `GEMINI_API_KEY`** on `rozgarone-worker`; the AI Assistant answers in offline mode until then:
  `cd backend-cloudflare && npx wrangler secret put GEMINI_API_KEY`
- [ ] **Delete the old database `sarkari_jobs_db`.** The old `sarkari-*` workers are already gone and all data is in `rozgarone_db`. Take a backup first, because a deleted database cannot be restored:
  ```bash
  npx wrangler d1 export sarkari_jobs_db --remote --output sarkari-backup.sql
  npx wrangler d1 delete sarkari_jobs_db
  ```
- [ ] (Optional) Enable R2 for PDF uploads (see PRODUCTION_SETUP.md, section 1.1).
- [ ] (Optional) Add custom domains, then update `ADMIN_ALLOWED_ORIGINS` and `API_BASE_URL`.

## 3. Admin panel and data

- [ ] **Approve the pending jobs.** All 67 imported jobs are `pending_review`, so the app shows no jobs. Open https://rozgarone-admin.bubunmohanta14.workers.dev → Pending.
- [ ] Store the admin API key in a password manager, then delete the copy left in the sandbox (`/projects/sandbox/rozgarone-admin-api-key.txt`).

## 4. GitHub

- [ ] Add repository secrets `CLOUDFLARE_API_TOKEN` and `CLOUDFLARE_ACCOUNT_ID` so pushes to `main` deploy the web app.
- [ ] Create the `release` environment and add the Android signing secrets (`UPLOAD_KEYSTORE_BASE64`, `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_PASSWORD`, `UPLOAD_KEY_ALIAS`).
- [ ] Add the real AdMob IDs as variables (without them the app shows Google test ads).
- [ ] Protect `main` so CI must pass before merging.
- [ ] (Optional) Google Play service account and Apple secrets for store uploads.

## 5. Mobile app

- [ ] **Make a new native build.** The redesign and voice search (new native module `expo-speech-recognition`) only reach phones in a new build. Push a tag such as `git tag v2.1.0 && git push origin v2.1.0`, or run `npx expo run:android` locally.
- [ ] Test on a real Android phone:
  - Voice search asks for microphone permission and fills the search box.
  - Deadline reminders fire for a saved job.
  - Theme switch (System / Light / Dark).
  - Onboarding appears on a fresh install only.
- [ ] Deploy the latest web app (automatic once the GitHub secrets above are set; manual: `cd mobile && APP_VARIANT=production npx expo export --platform web && npx wrangler deploy`).
