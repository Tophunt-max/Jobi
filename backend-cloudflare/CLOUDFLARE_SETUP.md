# Cloudflare Workers + D1 + R2 Backend Setup Guide (हिंदी व English)

इस गाइड की मदद से आप सिर्फ 5 मिनट में अपना Cloudflare Worker, D1 Database, और R2 Object Storage लाइव डिप्लॉय कर सकते हैं।

---

## 1. Prerequisites (ज़रूरी चीज़ें)
1. **Node.js**: अपने कंप्यूटर पर Node.js (v18+) इंस्टॉल रखें।
2. **Cloudflare Account**: [cloudflare.com](https://dash.cloudflare.com) पर फ़्री अकाउंट बनाएँ।

---

## 2. Step-by-Step Deployment Commands

अपने टर्मिनल या Command Prompt में `backend-cloudflare` फ़ोल्डर में जाएँ:

### Step A: Wrangler CLI इंस्टॉल करें और लॉगिन करें
```bash
npm install -g wrangler
wrangler login
```
*(यह ब्राउज़र खोलेगा जहाँ आप अपने Cloudflare अकाउंट को अनुमति देंगे।)*

---

### Step B: Cloudflare D1 Database बनाएँ
```bash
wrangler d1 create sarkari_jobs_db
```
**Output Example:**
```
✅ Successfully created DB 'sarkari_jobs_db'!
[[d1_databases]]
binding = "DB"
database_name = "sarkari_jobs_db"
database_id = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
```
➡️ **नोट:** ऊपर मिले `database_id` को `wrangler.toml` फ़ाइल में पेस्ट कर दें।

---

### Step C: D1 Database में स्कीमा और शुरुआती डेटा अपलोड करें
```bash
wrangler d1 execute sarkari_jobs_db --file=./schema.sql
```
*(यह कमांड `jobs` टेबल, इंडेक्स और सैंपल सरकारी भर्ती डेटा लाइव बना देगा।)*

---

### Step D: Cloudflare R2 Bucket बनाएँ (PDFs और एडमिट कार्ड स्टोरेज)
```bash
wrangler r2 bucket create sarkari-job-docs
```
*(Cloudflare R2 में 10GB स्टोरेज बिल्कुल मुफ़्त है और डेटा डाउनलोड (Egress) का कोई चार्ज नहीं लगता!)*

---

### Step E: Worker को लाइव डिप्लॉय करें!
```bash
wrangler deploy
```

डिप्लॉय होते ही आपको अपना लाइव API URL मिल जाएगा:
```
Published sarkari-jobs-worker (1.23 sec)
https://sarkari-jobs-worker.<your-subdomain>.workers.dev
```

---

## 3. Test Your Cloudflare API Endpoints

आप अपने ब्राउज़र या Postman में यह URLs टेस्ट कर सकते हैं:
* **Health Check:** `https://your-worker.workers.dev/api/health`
* **All Jobs:** `https://your-worker.workers.dev/api/jobs`
* **Filter by Category:** `https://your-worker.workers.dev/api/jobs?category=Admit%20Card`
* **Search Jobs:** `https://your-worker.workers.dev/api/jobs?search=SSC`
* **Dashboard Stats:** `https://your-worker.workers.dev/api/stats`

---

## 4. Android App में URL जोड़ें
इस URL को Android ऐप के Settings या `CloudflareConfig.kt` में डाल दें:
```kotlin
const val DEFAULT_WORKER_URL = "https://sarkari-jobs-worker.<your-subdomain>.workers.dev"
```
आपकी Android ऐप अब पूरी तरह Cloudflare Edge, D1 Database, और R2 Storage से कनेक्ट हो जाएगी!
