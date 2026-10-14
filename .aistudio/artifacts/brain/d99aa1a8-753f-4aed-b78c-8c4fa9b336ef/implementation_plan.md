# Implementation Plan: Cloudflare Workers + D1 Database + R2 Storage Integration

This plan outlines the architecture, deployment scripts, and Android client implementation to connect the application with a Cloudflare serverless backend (Workers + D1 SQLite database + R2 object storage).

---

## Architecture Overview

```
                                      ┌─────────────────────────────────────────┐
                                      │        Cloudflare Edge Network          │
                                      │                                         │
┌───────────────────────────┐         │  ┌───────────────────────────────────┐  │
│      Android App          │  HTTPS  │  │        Cloudflare Worker          │  │
│                           │─────────┼─▶│       (Serverless REST API)       │  │
│  ┌─────────────────────┐  │         │  │   /api/jobs, /api/admit-cards,   │  │
│  │ CloudflareApiClient │  │         │  │   /api/results, /api/upload      │  │
│  └──────────┬──────────┘  │         │  └─────────────┬─────────────┬───────┘  │
│             ▼             │         │                │             │          │
│  ┌─────────────────────┐  │         │                ▼             ▼          │
│  │ CloudflareJobRepo   │  │         │        ┌──────────────┐ ┌─────────────┐ │
│  └──────────┬──────────┘  │         │        │Cloudflare D1 │ │Cloudflare R2│ │
│             ▼             │         │        │(SQLite Edge) │ │(PDF Storage)│ │
│  ┌─────────────────────┐  │         │        └──────────────┘ └─────────────┘ │
│  │ Room Database       │  │         │                                         │
│  │ (Offline Cache)     │  │         │                                         │
│  └─────────────────────┘  │         │                                         │
└───────────────────────────┘         └─────────────────────────────────────────┘
```

---

## Phase 1: Cloudflare Backend Deliverables (Bundled Scripts & Configuration)

Provide ready-to-deploy files in a dedicated project directory (`backend-cloudflare/`):

1. **`wrangler.toml`**:
   - Worker definition, environment variables.
   - `[[d1_databases]]` binding: `DB` pointing to D1 database `sarkari_jobs_db`.
   - `[[r2_buckets]]` binding: `FILES_BUCKET` pointing to R2 bucket `sarkari-job-docs`.

2. **`schema.sql` (D1 Database Schema)**:
   - `jobs` table with all metadata: `id`, `title`, `department`, `category` (Latest Job, Admit Card, Result, Syllabus, Answer Key), `total_posts`, `qualification`, `application_start_date`, `last_date`, `exam_date`, `salary_scale`, `official_url`, `apply_url`, `pdf_r2_key`, `created_at`.
   - Indices on `category`, `last_date`, and `department` for sub-millisecond edge queries.
   - Initial sample datasets for Sarkari jobs, admit cards, and results.

3. **`worker.js` (Cloudflare Worker REST API)**:
   - `GET /api/jobs`: Supports query filtering (`?category=Admit Card`, `?search=SSC`, `?status=ACTIVE`).
   - `GET /api/jobs/:id`: Detailed view.
   - `POST /api/jobs`: Admin endpoint to publish/update jobs with authentication token header.
   - `POST /api/upload`: Direct upload handler for PDF notifications and admit cards into Cloudflare R2 with zero egress bandwidth costs.
   - `GET /api/files/:filename`: Secure streaming or download URL from R2.

---

## Phase 2: Android Networking & Data Layer

1. **Retrofit API Service (`com.example.data.remote.cloudflare`)**:
   - `CloudflareApiService.kt`: Defines endpoints for `getJobs()`, `getJobById()`, `getJobsByCategory()`, and `searchJobs()`.
   - DTO classes with `kotlinx.serialization`: `CloudflareJobDto`, `CloudflareApiResponse<T>`.

2. **Offline-First Repository Integration (`com.example.data.repository`)**:
   - `CloudflareJobRepository.kt`:
     - Reads directly from Room cache for instantaneous UI rendering.
     - Fetches live updates from Cloudflare Worker in background coroutine.
     - Upserts fresh jobs into Room database (`JobDao`), maintaining full offline capability.
     - Gracefully falls back to existing local Room data or demo items if network is unavailable or worker URL is unconfigured.

3. **Configuration & Environment Management**:
   - Default configurable base URL in `com.example.util.CloudflareConfig.kt` (pointing to demo endpoint or customizable worker domain).
   - Allows users or admins to plug in their custom Worker URL (`https://<worker-name>.<account>.workers.dev`) seamlessly.

---

## Phase 3: UI & Sync Indicator Enhancement

1. **Cloudflare Sync Status Badge**:
   - Update `JobListingScreen` and `AppTopBar` to display connection state:
     - 🟠 "Edge Connecting"
     - 🟢 "Cloudflare Edge Active" (D1 + R2 Connected)
     - ⚪ "Offline (Room Cache)"
2. **Document Viewer Integration**:
   - Seamlessly launch or download notification PDFs hosted on Cloudflare R2 directly from job detail view.

---

## Step-by-Step Deployment Instructions (For User)

Include a clear `CLOUDFLARE_SETUP.md` guide explaining:
1. `npm install -g wrangler` & `wrangler login`
2. `wrangler d1 create sarkari_jobs_db` & `wrangler d1 execute sarkari_jobs_db --file=schema.sql`
3. `wrangler r2 bucket create sarkari-job-docs`
4. `wrangler deploy`
5. Copying the generated URL into the Android app.
