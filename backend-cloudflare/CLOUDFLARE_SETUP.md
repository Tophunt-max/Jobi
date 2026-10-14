# Automated Government Job Importer: Cloudflare Workers + D1 + R2 + Android

यह गाइड आपके **Indian Government Job Portal (Kotlin Android)** और **Cloudflare Workers Backend** के सम्पूर्ण ऑटोमेशन को सेट अप व डिप्लॉय करने के लिए तैयार की गई है।

---

## 🌟 आर्किटेक्चर सारांश (Architecture Overview)

```
[Official Sources Registry]
  ├── All India: SSC, UPSC, RRB, IBPS, SBI, KVS
  └── Odisha: OPSC, OSSC, OSSSC
           │
           ▼ (Scheduled Cron Trigger: Every 6 Hours / Admin Manual)
[Cloudflare Worker Backend API]
  ├── Bounded Source Adapters (Timeouts + Error Isolation)
  ├── Concurrency-Safe Deduplication (Canonical URL + Advt No + SHA-256)
  └── Date Parser & Validation (DD/MM/YYYY, YYYY-MM-DD)
           │
           ▼
[Cloudflare D1 Database] (Jobs saved as status='pending_review')
           │
           ▼
[Android Admin Review Queue] ──(Admin Approve)──► [Status: ACTIVE]
                                                       │
                                                       ├──► Live in App (Instant Sync)
                                                       └──► FCM Push Notification Queued
```

---

## 🚀 5-Minute Setup & Deployment Guide

### चरण 1: Cloudflare CLI (Wrangler) इंस्टॉल व लॉगिन करें
अपने टर्मिनल में चलाएँ:
```bash
npm install -g wrangler
wrangler login
```

### चरण 2: Cloudflare D1 Database बनाएँ
```bash
wrangler d1 create sarkari_jobs_db
```
कमांड चलने के बाद आपको एक `database_id` मिलेगा। उसे `backend-cloudflare/wrangler.toml` में पेस्ट करें:
```toml
[[d1_databases]]
binding = "DB"
database_name = "sarkari_jobs_db"
database_id = "your-actual-database-id-here"
```

### चरण 3: Database Schema & Official Sources Seed चलाएँ
```bash
cd backend-cloudflare
wrangler d1 execute sarkari_jobs_db --file=./schema.sql --remote
```
यह कमांड आपके D1 में `jobs`, `job_sources` (All India व Odisha के आधिकारिक पोर्टल्स), `job_import_runs`, और `job_notifications` टेबल्स तैयार कर देगा।

### चरण 4: Cloudflare R2 Bucket (PDF Storage - Zero Egress Fees)
```bash
wrangler r2 bucket create sarkari-job-docs
```

### चरण 5: Worker डिप्लॉय करें
```bash
wrangler deploy
```
डिप्लॉय होते ही आपको अपना लाइव Worker URL मिल जाएगा, उदाहरण:
`https://sarkari-jobs-worker.<your-account>.workers.dev`

---

## ⚙️ Cron Triggers (Scheduled Auto-Import)
`wrangler.toml` में हर 6 घंटे पर चलने वाला क्रॉन ट्रिगर पहले से कॉन्फ़िगर है:
```toml
[triggers]
crons = ["0 */6 * * *"]
```
जब भी क्रॉन ट्रिगर चलता है, `scheduled(event, env, ctx)` हैंडलर:
1. सभी एक्टिव ऑल इंडिया व ओडिशा पोर्टल्स को चेक करता है।
2. नए रिक्रूटमेंट नोटिसेस फ़ेच करता है।
3. डुप्लिकेट्स को हटाकर नए नोटिसेस को `status = 'pending_review'` के रूप में D1 में सेव करता है।
4. पूरे रन के आँकड़े `job_import_runs` टेबल में सुरक्षित रखता है।

---

## 📱 Android App में Worker URL सेट करना
App खोलें ➔ **Profile / Settings** ➔ **Cloudflare Worker URL** में अपना लाइव Worker URL डालें या डिफ़ॉल्ट URL का उपयोग करें।

Admin स्क्रीन में:
* **Dashboard Tab**: "Run Importer Now" बटन पर क्लिक करके किसी भी समय तत्काल आयात चला सकते हैं।
* **Review Queue Tab**: नए आयातित नोटिसेस को एक क्लिक में "Approve & Publish" या "Reject" कर सकते हैं।
* **Sources Tab**: SSC, UPSC, RRB, IBPS, SBI, KVS, OPSC, OSSC, OSSSC आदि पोर्टल्स की स्थिति देख सकते हैं।
* **Import History Tab**: प्रत्येक क्रॉन व मैन्युअल रन का विस्तृत लॉग (Fetched, Inserted, Duplicates, Errors) देख सकते हैं।
