/**
 * Public, read-only endpoints consumed by the Android app.
 * Only ACTIVE (and, when asked, EXPIRED) notices are ever returned here.
 */
import { json, HttpError } from "../http.js";
import { clampInt, CATEGORIES, PUBLIC_STATUSES } from "../validation.js";
import {
    toAdmissionDto, toAdmitCardDto, toAnswerKeyDto, toNotificationDto,
    toPublicJob, toResultDto, toSyllabusDto
} from "../mappers.js";
import { DEFAULT_AD_CONFIG, loadAdConfig } from "../settings.js";

const PUBLIC_CACHE = { "Cache-Control": "public, max-age=60, s-maxage=120" };

function escapeLike(s) {
    return s.replace(/[\\%_]/g, (c) => `\\${c}`);
}

async function queryJobs(env, params) {
    const where = [];
    const binds = [];

    const status = params.status && PUBLIC_STATUSES.includes(params.status) ? params.status : "ACTIVE";
    where.push("status = ?");
    binds.push(status);

    if (params.category && CATEGORIES.includes(params.category)) {
        where.push("category = ?");
        binds.push(params.category);
    }
    if (params.state && params.state !== "All" && params.state !== "All India") {
        where.push("(state = ? OR state = 'All India')");
        binds.push(params.state.slice(0, 60));
    }
    if (params.sourceId) {
        where.push("source_id = ?");
        binds.push(params.sourceId.slice(0, 60));
    }
    if (params.search) {
        const term = `%${escapeLike(params.search.slice(0, 100))}%`;
        where.push("(title LIKE ? ESCAPE '\\' OR organization LIKE ? ESCAPE '\\' OR post_name LIKE ? ESCAPE '\\' OR advertisement_number LIKE ? ESCAPE '\\')");
        binds.push(term, term, term, term);
    }

    const sql = `SELECT * FROM jobs WHERE ${where.join(" AND ")}
                 ORDER BY COALESCE(published_at, created_at) DESC
                 LIMIT ? OFFSET ?`;
    binds.push(params.limit, params.offset);
    const { results } = await env.DB.prepare(sql).bind(...binds).all();
    return results || [];
}

function listParams(url, defaults = {}) {
    const q = url.searchParams;
    return {
        category: q.get("category") || defaults.category || null,
        state: (q.get("state") || "").trim() || null,
        search: (q.get("search") || "").trim() || null,
        status: q.get("status") || null,
        sourceId: (q.get("source_id") || "").trim() || null,
        limit: clampInt(q.get("limit"), { min: 1, max: 100, fallback: 50 }),
        offset: clampInt(q.get("offset"), { min: 0, max: 10_000, fallback: 0 })
    };
}

async function categoryList(env, url, category, mapper) {
    const rows = await queryJobs(env, listParams(url, { category }));
    const data = rows.map(mapper);
    return { success: true, count: data.length, data };
}

async function recentNotifications(env, limit) {
    const { results } = await env.DB.prepare(`
        SELECT n.*, j.slug AS job_slug
        FROM job_notifications n
        JOIN jobs j ON j.id = n.job_id AND j.status = 'ACTIVE'
        ORDER BY n.created_at DESC
        LIMIT ?
    `).bind(limit).all();
    return (results || []).map((r) => toNotificationDto({ ...r, target_slug: r.target_slug || r.job_slug }));
}

export async function handlePublic(request, env, url, cors) {
    const path = url.pathname;
    const h = { ...cors, ...PUBLIC_CACHE };

    if (path === "/" ) {
        return json({ success: true, name: "Sarkari Job Portal API", version: "3.0.0" }, { headers: cors });
    }

    if (path === "/api/health") {
        let database = "unbound";
        if (env.DB) {
            try {
                await env.DB.prepare("SELECT 1").first();
                database = "connected";
            } catch {
                database = "error";
            }
        }
        const ok = database === "connected";
        return json({
            status: ok ? "ok" : "degraded",
            backend: "cloudflare-workers",
            database,
            storage: env.FILES_BUCKET ? "connected" : "unbound",
            importer: "scheduled",
            timestamp: new Date().toISOString()
        }, { status: ok ? 200 : 503, headers: cors });
    }

    if (path === "/api/jobs") {
        const rows = await queryJobs(env, listParams(url));
        return json({ success: true, count: rows.length, data: rows.map(toPublicJob) }, { headers: h });
    }

    if (path.startsWith("/api/jobs/")) {
        const id = decodeURIComponent(path.slice("/api/jobs/".length));
        if (!id || id.includes("/") || id.length > 120) throw new HttpError(404, "Job not found");
        const job = await env.DB.prepare(
            "SELECT * FROM jobs WHERE (id = ? OR slug = ?) AND status IN ('ACTIVE', 'EXPIRED')"
        ).bind(id, id).first();
        if (!job) throw new HttpError(404, "Job not found");
        return json({ success: true, data: toPublicJob(job) }, { headers: h });
    }

    if (path === "/api/results") return json(await categoryList(env, url, "Result", toResultDto), { headers: h });
    if (path === "/api/admit-cards") return json(await categoryList(env, url, "Admit Card", toAdmitCardDto), { headers: h });
    if (path === "/api/syllabi") return json(await categoryList(env, url, "Syllabus", toSyllabusDto), { headers: h });
    if (path === "/api/admissions") return json(await categoryList(env, url, "Admission", toAdmissionDto), { headers: h });
    if (path === "/api/answer-keys") return json(await categoryList(env, url, "Answer Key", toAnswerKeyDto), { headers: h });

    if (path === "/api/notifications") {
        const limit = clampInt(url.searchParams.get("limit"), { min: 1, max: 100, fallback: 50 });
        const data = await recentNotifications(env, limit);
        return json({ success: true, count: data.length, data }, { headers: h });
    }

    if (path === "/api/sync/all") {
        const { results } = await env.DB.prepare(
            "SELECT * FROM jobs WHERE status = 'ACTIVE' ORDER BY COALESCE(published_at, created_at) DESC LIMIT 500"
        ).all();
        const rows = results || [];
        const by = (cat) => rows.filter((r) => r.category === cat);
        return json({
            success: true,
            server_timestamp: Date.now(),
            version: "3.0",
            data: {
                jobs: by("Latest Job").map(toPublicJob),
                results: by("Result").map(toResultDto),
                admit_cards: by("Admit Card").map(toAdmitCardDto),
                syllabi: by("Syllabus").map(toSyllabusDto),
                admissions: by("Admission").map(toAdmissionDto),
                answer_keys: by("Answer Key").map(toAnswerKeyDto),
                notifications: await recentNotifications(env, 50)
            }
        }, { headers: h });
    }

    if (path === "/api/sources") {
        const state = url.searchParams.get("state");
        let sql = "SELECT id, name, official_domain, listing_url, parser_type, state, enabled, last_successful_fetch_at FROM job_sources WHERE enabled = 1";
        const binds = [];
        if (state && state !== "All") {
            sql += " AND (state = ? OR state = 'All India')";
            binds.push(state.slice(0, 60));
        }
        sql += " ORDER BY state ASC, name ASC";
        const { results } = await env.DB.prepare(sql).bind(...binds).all();
        return json({ success: true, count: (results || []).length, sources: results || [] }, { headers: h });
    }

    if (path === "/api/stats") {
        const row = await env.DB.prepare(`
            SELECT
              SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) AS totalJobs,
              SUM(CASE WHEN status = 'ACTIVE' AND category = 'Latest Job' THEN 1 ELSE 0 END) AS latestJobs,
              SUM(CASE WHEN status = 'ACTIVE' AND category = 'Admit Card' THEN 1 ELSE 0 END) AS admitCards,
              SUM(CASE WHEN status = 'ACTIVE' AND category = 'Result' THEN 1 ELSE 0 END) AS results,
              SUM(CASE WHEN status = 'pending_review' THEN 1 ELSE 0 END) AS pendingReview
            FROM jobs
        `).first();
        const src = await env.DB.prepare("SELECT COUNT(*) AS c FROM job_sources WHERE enabled = 1").first();
        return json({
            success: true,
            data: {
                totalJobs: row?.totalJobs || 0,
                latestJobs: row?.latestJobs || 0,
                admitCards: row?.admitCards || 0,
                results: row?.results || 0,
                pendingReview: row?.pendingReview || 0,
                activeSources: src?.c || 0
            }
        }, { headers: h });
    }

    if (path === "/api/ads/config") {
        const cfg = await loadAdConfig(env).catch((e) => {
            console.error("ad_config load failed:", e);
            return DEFAULT_AD_CONFIG;
        });
        return json({ success: true, data: cfg }, { headers: { ...cors, "Cache-Control": "public, max-age=300" } });
    }

    return null;
}
