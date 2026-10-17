# Sarkari Job Portal — Next.js Admin Panel (Cloudflare Deployment)

Modern, lightweight, production-grade Next.js Admin Panel designed to be deployed for **₹0 budget** on **Cloudflare Pages** or **Cloudflare Workers**. It connects seamlessly to your existing Cloudflare Worker backend (`backend-cloudflare/src/index.js`), Cloudflare D1 (SQLite database), and Cloudflare R2 (PDF storage).

---

## 🚀 Key Features

1. **Dashboard Overview**: Real-time counts of pending review notices, active jobs, admit cards, exam results, and active official recruitment portals.
2. **Pending Review Queue**: Review freshly scraped government job notices from official portals (All India & Odisha).
   - One-click **Approve & Publish to Mobile App** (automatically queues FCM push notification).
   - **Reject with Reason** modal dialog.
   - Inspect full details: title, organization, advt number, posts, salary scale, dates, and official notification PDF/apply URLs.
3. **Official Sources Manager (30+ Sources)**:
   - Configure All India (SSC, UPSC, RRB, IBPS, SBI, RBI, NABARD, India Post GDS, DRDO, ISRO, LIC, DSSSB) and Odisha (OPSC, OSSC, OSSSC, BSE, OAVS, GRIDCO, OPTCL, OHPC) sources.
   - Enable/disable toggles for any portal.
   - Live **Test Adapter** button to inspect crawler output directly in the browser.
   - One-click **Crawl Now** for a single source.
4. **Active Notices & Database Browser**:
   - Filter all live published records in Cloudflare D1 by category (`Latest Job`, `Admit Card`, `Result`, `Answer Key`, `Syllabus`) and status.
5. **Execution Logs & Audit History**:
   - Track automated 6-hour cron triggers and manual crawler runs with counts for fetched, newly inserted, and duplicate skipped records.
6. **Manual Recruitment Notice Publisher**: Directly create and publish verified government job openings, admit cards, or exam results with total vacancies, pay levels, eligibility, official PDF URLs, and online apply links.

7. **Configurable Connection**:
   - Change your Cloudflare Worker URL and Admin Secret Key directly in the UI with local persistence.

---

## ⚡ Deployment to Cloudflare Pages (₹0 Cost)

### Option A: 1-Command CLI Deployment (Fastest)

1. Make sure you have Node.js installed.
2. Navigate into `admin-panel`:
```bash
cd admin-panel
npm install
npm run build
```
3. Deploy directly to Cloudflare Pages using Wrangler:
```bash
npx wrangler pages deploy out --project-name=sarkari-jobs-admin
```
4. Wrangler will output your live URL: `https://sarkari-jobs-admin.pages.dev`!

---

### Option B: Cloudflare Dashboard (GitHub / GitLab Continuous Deployment)

1. Push your repository to GitHub or GitLab.
2. Go to [Cloudflare Dashboard](https://dash.cloudflare.com/) > **Workers & Pages** > **Create Application** > **Pages** > **Connect to Git**.
3. Select your repository.
4. Configure Build Settings:
   - **Framework preset**: `Next.js (Static HTML Export)`
   - **Root directory**: `admin-panel`
   - **Build command**: `npm run build`
   - **Build output directory**: `out`
5. Under **Environment variables (Advanced)**, add:
   - `NEXT_PUBLIC_DEFAULT_API_URL`: Your Cloudflare Worker URL (e.g. `https://sarkari-jobs-worker.<your-subdomain>.workers.dev`)
   - `NEXT_PUBLIC_DEFAULT_ADMIN_KEY`: Your admin secret key matching `backend-cloudflare/wrangler.toml`
6. Click **Save and Deploy**. Cloudflare will automatically build and publish your admin panel!

---

## 🛠 Local Development

```bash
cd admin-panel
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.
In the **Settings** tab, enter your Cloudflare Worker URL and Admin Secret Key, then click **Test Connection** to verify live communication with Cloudflare D1!
