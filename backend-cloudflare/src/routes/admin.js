/**
 * Admin endpoints. Every route here requires a valid admin key (see auth.js).
 */
import { json, readJson, HttpError } from "../http.js";
import { requireAdmin } from "../auth.js";
import {
    ADMIN_STATUSES, CATEGORIES, clampInt, optionalString, oneOf,
    shortId, slugify, validateAdConfig, validateManualJob
} from "../validation.js";
import { executeJobImport } from "../importer/pipeline.js";
import { scrapeSource } from "../importer/scraper.js";
import { DEFAULT_AD_CONFIG, loadAdConfig, saveAdConfig } from "../settings.js";

const MAX_BATCH = 100;
const MAX_UPLOAD_BYTES = 10 * 1024 * 1024;

function notificationFor(job, id) {
    const posts = job.total_posts ? `${job.total_posts} posts. ` : "";
    const last = job.last_date ? `Last date: ${job.last_date}.` : "See official notice for dates.";
    return {
        id,
        title: `New ${job.category}: ${job.title}`.slice(0, 200),
        body: `${job.organization}. ${posts}${last}`.slice(0, 500)
    };
}

/** Builds the statements that publish a pending job and queue its notification. */
function approveStatements(db, job, approvedBy) {
    const now = new Date().toISOString();
    const n = notificationFor(job, `notif-${crypto.randomUUID()}`);
    return [
        db.prepare(`
            UPDATE jobs
            SET status = 'ACTIVE', verified = 1, approved_by = ?, approved_at = ?, reviewed_at = ?,
                published_at = ?, updated_at = ?, rejection_reason = NULL
            WHERE id = ? AND status = 'pending_review'
        `).bind(approvedBy, now, now, now, now, job.id),
        db.prepare(`
            INSERT INTO job_notifications (id, job_id, title, body, status, type, target_slug, priority)
            SELECT ?, ?, ?, ?, 'PENDING', 'NEW_JOB', ?, 'HIGH'
            WHERE changes() > 0
        `).bind(n.id, job.id, n.title, n.body, job.slug || job.id)
    ];
}

function idFromPath(path, prefix, suffix) {
    const raw = path.slice(prefix.length, path.length - suffix.length);
    const id = decodeURIComponent(raw);
    if (!id || id.includes("/") || id.length > 120) throw new HttpError(404, "Not found");
    return id;
}

export async function handleAdmin(request, env, url, cors) {
    const path = url.pathname;
    const method = request.method;

    await requireAdmin(request, env);
    const ok = (data, status = 200) => json({ success: true, ...data }, { status, headers: { ...cors, "Cache-Control": "no-store" } });

    // ---------- Sources ----------
    if (path === "/api/admin/job-import/sources" && method === "GET") {
        const { results } = await env.DB.prepare("SELECT * FROM job_sources ORDER BY state ASC, name ASC").all();
        return ok({ sources: results || [] });
    }

    if (path === "/api/admin/job-import/sources" && method === "POST") {
        const body = await readJson(request);
        const id = optionalString(body.id, "id", { max: 60 });
        if (!id) throw new HttpError(400, "Source id is required");
        if (typeof body.enabled !== "boolean") throw new HttpError(400, "enabled must be a boolean");
        const interval = body.polling_interval_hours === undefined || body.polling_interval_hours === null
            ? null
            : clampInt(body.polling_interval_hours, { min: 1, max: 168, fallback: 6 });

        const res = await env.DB.prepare(`
            UPDATE job_sources SET enabled = ?, polling_interval_hours = COALESCE(?, polling_interval_hours) WHERE id = ?
        `).bind(body.enabled ? 1 : 0, interval, id).run();
        if (!res.meta || res.meta.changes === 0) throw new HttpError(404, `Source ${id} not found`);
        return ok({ message: `Source ${id} updated` });
    }

    if (path.startsWith("/api/admin/job-import/sources/") && path.endsWith("/test") && method === "POST") {
        const id = idFromPath(path, "/api/admin/job-import/sources/", "/test");
        const source = await env.DB.prepare("SELECT * FROM job_sources WHERE id = ?").bind(id).first();
        if (!source) throw new HttpError(404, "Source not found");
        try {
            const notices = await scrapeSource(source);
            return ok({ source: source.name, count: notices.length, sample: notices.slice(0, 5) });
        } catch (err) {
            return json(
                { success: false, error: "SOURCE_FETCH_FAILED", message: String(err.message || err).slice(0, 300), source: source.name },
                { status: 502, headers: cors }
            );
        }
    }

    // ---------- Import runs ----------
    if (path === "/api/admin/job-import/run" && method === "POST") {
        const body = await readJson(request, { optional: true });
        const sourceId = optionalString(body.source_id, "source_id", { max: 60 });
        const summary = await executeJobImport(env, { sourceId, triggeredBy: "ADMIN_MANUAL", force: true });
        return ok({ message: "Job import completed", summary });
    }

    if (path === "/api/admin/job-import/runs" && method === "GET") {
        const limit = clampInt(url.searchParams.get("limit"), { min: 1, max: 200, fallback: 50 });
        const { results } = await env.DB.prepare(
            "SELECT * FROM job_import_runs ORDER BY started_at DESC LIMIT ?"
        ).bind(limit).all();
        return ok({ runs: results || [] });
    }

    // ---------- Jobs: listing ----------
    if (path === "/api/admin/jobs/pending" && method === "GET") {
        const limit = clampInt(url.searchParams.get("limit"), { min: 1, max: 200, fallback: 100 });
        const offset = clampInt(url.searchParams.get("offset"), { min: 0, max: 100_000, fallback: 0 });
        const { results } = await env.DB.prepare(
            "SELECT * FROM jobs WHERE status = 'pending_review' ORDER BY created_at DESC LIMIT ? OFFSET ?"
        ).bind(limit, offset).all();
        return ok({ count: (results || []).length, data: results || [] });
    }

    if (path === "/api/admin/jobs" && method === "GET") {
        const status = url.searchParams.get("status");
        const category = url.searchParams.get("category");
        const limit = clampInt(url.searchParams.get("limit"), { min: 1, max: 200, fallback: 100 });
        const offset = clampInt(url.searchParams.get("offset"), { min: 0, max: 100_000, fallback: 0 });
        const where = [];
        const binds = [];
        if (status && status !== "ALL") {
            where.push("status = ?");
            binds.push(oneOf(status, "status", ADMIN_STATUSES));
        }
        if (category && category !== "All") {
            where.push("category = ?");
            binds.push(oneOf(category, "category", CATEGORIES));
        }
        const sql = `SELECT * FROM jobs ${where.length ? "WHERE " + where.join(" AND ") : ""}
                     ORDER BY created_at DESC LIMIT ? OFFSET ?`;
        const { results } = await env.DB.prepare(sql).bind(...binds, limit, offset).all();
        return ok({ count: (results || []).length, data: results || [] });
    }

    // ---------- Jobs: moderation ----------
    if (path === "/api/admin/jobs/batch-approve" && method === "POST") {
        const body = await readJson(request);
        const ids = Array.isArray(body.ids) ? [...new Set(body.ids.filter((x) => typeof x === "string" && x.length <= 120))] : [];
        if (ids.length === 0) throw new HttpError(400, "ids must be a non-empty array of job IDs");
        if (ids.length > MAX_BATCH) throw new HttpError(400, `At most ${MAX_BATCH} jobs can be approved at once`);
        const approvedBy = optionalString(body.approved_by, "approved_by", { max: 80 }) || "admin";

        const placeholders = ids.map(() => "?").join(",");
        const { results } = await env.DB.prepare(
            `SELECT * FROM jobs WHERE status = 'pending_review' AND id IN (${placeholders})`
        ).bind(...ids).all();
        const jobs = results || [];
        if (jobs.length === 0) return ok({ message: "No pending jobs matched", approvedCount: 0 });

        const stmts = jobs.flatMap((j) => approveStatements(env.DB, j, approvedBy));
        await env.DB.batch(stmts);
        return ok({ message: `Approved ${jobs.length} jobs`, approvedCount: jobs.length });
    }

    if (path.startsWith("/api/admin/jobs/") && path.endsWith("/approve") && method === "POST") {
        const id = idFromPath(path, "/api/admin/jobs/", "/approve");
        const body = await readJson(request, { optional: true });
        const approvedBy = optionalString(body.approved_by, "approved_by", { max: 80 }) || "admin";

        const job = await env.DB.prepare("SELECT * FROM jobs WHERE id = ?").bind(id).first();
        if (!job) throw new HttpError(404, "Job not found");
        if (job.status !== "pending_review") throw new HttpError(409, `Job is ${job.status}, only pending_review jobs can be approved`);

        await env.DB.batch(approveStatements(env.DB, job, approvedBy));
        return ok({ message: "Job approved and published", notificationQueued: true });
    }

    if (path.startsWith("/api/admin/jobs/") && path.endsWith("/reject") && method === "POST") {
        const id = idFromPath(path, "/api/admin/jobs/", "/reject");
        const body = await readJson(request, { optional: true });
        const reason = optionalString(body.reason, "reason", { max: 500 }) || "Rejected by administrator";
        const now = new Date().toISOString();

        const res = await env.DB.prepare(`
            UPDATE jobs SET status = 'REJECTED', rejection_reason = ?, reviewed_at = ?, updated_at = ?
            WHERE id = ? AND status IN ('pending_review', 'ACTIVE')
        `).bind(reason, now, now, id).run();
        if (!res.meta || res.meta.changes === 0) {
            const exists = await env.DB.prepare("SELECT status FROM jobs WHERE id = ?").bind(id).first();
            if (!exists) throw new HttpError(404, "Job not found");
            throw new HttpError(409, `Job is already ${exists.status}`);
        }
        return ok({ message: "Job rejected" });
    }

    if (path === "/api/admin/jobs" && method === "POST") {
        const body = await readJson(request);
        const j = validateManualJob(body);
        const now = new Date().toISOString();
        const id = `manual-${crypto.randomUUID()}`;
        const slug = `${slugify(j.title) || "notice"}-${shortId()}`;

        const res = await env.DB.prepare(`
            INSERT INTO jobs (
                id, title, slug, organization, department, post_name, category, state, total_posts,
                qualification, min_age, max_age, application_start_date, last_date, exam_date,
                fee_general, fee_sc_st, salary_scale, selection_process,
                official_url, official_notification_url, official_apply_url, notification_pdf_r2_key,
                source_id, source_domain, advertisement_number,
                status, verified, approved_by, approved_at, reviewed_at, published_at, is_featured,
                created_at, updated_at
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?, ?,
                'manual', ?, ?,
                'ACTIVE', 1, ?, ?, ?, ?, ?,
                ?, ?
            )
            ON CONFLICT DO NOTHING
        `).bind(
            id, j.title, slug, j.organization, j.department, j.post_name, j.category, j.state, j.total_posts,
            j.qualification, j.min_age, j.max_age, j.application_start_date, j.last_date, j.exam_date,
            j.fee_general, j.fee_sc_st, j.salary_scale, j.selection_process,
            j.official_url, j.official_notification_url, j.official_apply_url, j.notification_pdf_r2_key,
            j.source_domain, j.advertisement_number,
            j.approved_by, now, now, now, j.is_featured,
            now, now
        ).run();

        if (!res.meta || res.meta.changes === 0) {
            throw new HttpError(409, "A job with this official notification URL already exists");
        }

        const n = notificationFor({ ...j, id }, `notif-${crypto.randomUUID()}`);
        await env.DB.prepare(`
            INSERT INTO job_notifications (id, job_id, title, body, status, type, target_slug, priority)
            VALUES (?, ?, ?, ?, 'PENDING', 'NEW_JOB', ?, 'HIGH')
        `).bind(n.id, id, n.title, n.body, slug).run();

        return ok({ message: `Published "${j.title}"`, jobId: id, slug }, 201);
    }

    // ---------- User reports ----------
    if (path === "/api/admin/reports" && method === "GET") {
        const status = url.searchParams.get("status") || "OPEN";
        const limit = clampInt(url.searchParams.get("limit"), { min: 1, max: 200, fallback: 100 });
        const where = status === "ALL" ? "" : "WHERE r.status = ?";
        const binds = status === "ALL" ? [] : [oneOf(status, "status", ["OPEN", "RESOLVED", "DISMISSED"])];
        const { results } = await env.DB.prepare(`
            SELECT r.id, r.job_id, r.reason, r.details, r.status, r.created_at, r.resolved_at, r.resolution_note,
                   j.title AS job_title, j.organization AS job_organization, j.official_notification_url AS job_url
            FROM job_reports r LEFT JOIN jobs j ON j.id = r.job_id
            ${where}
            ORDER BY r.created_at DESC LIMIT ?
        `).bind(...binds, limit).all();
        return ok({ count: (results || []).length, data: results || [] });
    }

    if (path.startsWith("/api/admin/reports/") && path.endsWith("/resolve") && method === "POST") {
        const id = idFromPath(path, "/api/admin/reports/", "/resolve");
        const body = await readJson(request, { optional: true });
        const status = oneOf(body.status, "status", ["RESOLVED", "DISMISSED"], "RESOLVED");
        const note = optionalString(body.note, "note", { max: 500 });
        const res = await env.DB.prepare(
            "UPDATE job_reports SET status = ?, resolution_note = ?, resolved_at = ? WHERE id = ? AND status = 'OPEN'"
        ).bind(status, note, new Date().toISOString(), id).run();
        if (!res.meta || res.meta.changes === 0) throw new HttpError(404, "Open report not found");
        return ok({ message: `Report ${status.toLowerCase()}` });
    }

    // ---------- Ads ----------
    if (path === "/api/admin/ads" && method === "GET") {
        return ok({ data: await loadAdConfig(env) });
    }

    if (path === "/api/admin/ads" && method === "POST") {
        const body = await readJson(request);
        const current = await loadAdConfig(env).catch(() => ({ ...DEFAULT_AD_CONFIG }));
        const { updatedAt, ...base } = current;
        const next = validateAdConfig(body, base);
        await saveAdConfig(env, next);
        return ok({ message: "Ad configuration saved", data: { ...next, updatedAt: new Date().toISOString() } });
    }

    // ---------- R2 upload (PDF notices only) ----------
    if (path === "/api/admin/upload" && method === "POST") {
        if (!env.FILES_BUCKET) throw new HttpError(503, "File storage is not configured");
        const declared = Number(request.headers.get("content-length") || "0");
        if (declared > MAX_UPLOAD_BYTES) throw new HttpError(413, "PDF exceeds 10 MB");

        const buf = await request.arrayBuffer();
        if (buf.byteLength === 0) throw new HttpError(400, "Empty upload");
        if (buf.byteLength > MAX_UPLOAD_BYTES) throw new HttpError(413, "PDF exceeds 10 MB");
        const head = new Uint8Array(buf, 0, Math.min(5, buf.byteLength));
        if (String.fromCharCode(...head) !== "%PDF-") throw new HttpError(415, "Only PDF files are accepted");

        const key = `notices/${new Date().toISOString().slice(0, 7)}/${crypto.randomUUID()}.pdf`;
        await env.FILES_BUCKET.put(key, buf, {
            httpMetadata: { contentType: "application/pdf", contentDisposition: "inline" }
        });
        return ok({ message: "PDF stored", r2Key: key, downloadUrl: `${url.origin}/api/files/${key}` }, 201);
    }

    throw new HttpError(404, "Admin route not found");
}
