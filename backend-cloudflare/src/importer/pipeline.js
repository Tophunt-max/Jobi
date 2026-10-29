/**
 * Import pipeline: scrape enabled sources, dedupe, and queue notices for review.
 *
 * Guarantees:
 * - Single flight: a new run is refused while another run started < 15 min ago is RUNNING.
 * - Stale RUNNING rows (> 30 min, e.g. Worker killed mid-run) are marked FAILED.
 * - Deduplication is enforced by the database (UNIQUE index on official_notification_url
 *   and on content_hash), so concurrent inserts cannot create duplicates.
 * - One failing source never aborts the others; per-source errors are recorded.
 * - Cron runs respect each source's polling_interval_hours.
 */
import { scrapeSource } from "./scraper.js";
import { sha256Hex, slugify, shortId } from "../validation.js";
import { HttpError } from "../http.js";

const SOURCE_CONCURRENCY = 4;

export async function executeJobImport(env, { sourceId = null, triggeredBy = "CRON_TRIGGER", force = false } = {}) {
    const db = env.DB;

    await db.prepare(`
        UPDATE job_import_runs
        SET status = 'FAILED', finished_at = ?, error_summary = 'Run did not finish (worker terminated or timed out)'
        WHERE status = 'RUNNING' AND started_at < ?
    `).bind(new Date().toISOString(), new Date(Date.now() - 30 * 60_000).toISOString()).run();

    const running = await db.prepare(
        "SELECT id FROM job_import_runs WHERE status = 'RUNNING' AND started_at >= ? LIMIT 1"
    ).bind(new Date(Date.now() - 15 * 60_000).toISOString()).first();
    if (running) {
        throw new HttpError(409, `Another import run (${running.id}) is already in progress`);
    }

    let sources;
    if (sourceId) {
        const row = await db.prepare("SELECT * FROM job_sources WHERE id = ?").bind(sourceId).first();
        if (!row) throw new HttpError(404, `Source ${sourceId} not found`);
        sources = [row];
    } else {
        const { results } = await db.prepare("SELECT * FROM job_sources WHERE enabled = 1 ORDER BY id").all();
        sources = (results || []).filter((s) => force || isDue(s));
    }

    const runId = `run-${crypto.randomUUID()}`;
    const startedAt = new Date().toISOString();
    try {
        // A partial UNIQUE index (status) WHERE status = 'RUNNING' makes this atomic.
        await db.prepare(`
            INSERT INTO job_import_runs (id, source_id, started_at, status, triggered_by)
            VALUES (?, ?, ?, 'RUNNING', ?)
        `).bind(runId, sourceId || "ALL_ENABLED", startedAt, triggeredBy).run();
    } catch (err) {
        if (/UNIQUE/i.test(String(err && err.message))) {
            throw new HttpError(409, "Another import run is already in progress");
        }
        throw err;
    }

    const totals = { fetched: 0, inserted: 0, duplicates: 0, errors: 0 };
    const errors = [];

    // Process sources with bounded concurrency.
    const queue = [...sources];
    async function worker() {
        while (queue.length) {
            const source = queue.shift();
            try {
                const r = await importSource(db, source);
                totals.fetched += r.fetched;
                totals.inserted += r.inserted;
                totals.duplicates += r.duplicates;
            } catch (err) {
                totals.errors++;
                const msg = (err && err.message ? err.message : String(err)).slice(0, 255);
                errors.push(`${source.id}: ${msg}`);
                console.warn(`[IMPORTER] ${source.id} failed: ${msg}`);
                await db.prepare(
                    "UPDATE job_sources SET last_fetched_at = ?, last_error = ? WHERE id = ?"
                ).bind(new Date().toISOString(), msg, source.id).run();
            }
        }
    }
    await Promise.all(Array.from({ length: Math.min(SOURCE_CONCURRENCY, queue.length || 1) }, worker));

    const finishedAt = new Date().toISOString();
    const status = totals.errors === 0 ? "SUCCESS" : (totals.errors < sources.length ? "PARTIAL" : "FAILED");

    await db.prepare(`
        UPDATE job_import_runs
        SET finished_at = ?, fetched_count = ?, inserted_count = ?, duplicate_count = ?,
            error_count = ?, status = ?, error_summary = ?
        WHERE id = ?
    `).bind(
        finishedAt, totals.fetched, totals.inserted, totals.duplicates, totals.errors,
        sources.length === 0 ? "SUCCESS" : status,
        errors.join("; ").slice(0, 1000) || null,
        runId
    ).run();

    return {
        runId,
        status: sources.length === 0 ? "SUCCESS" : status,
        startedAt,
        finishedAt,
        sourcesProcessed: sources.length,
        totalFetched: totals.fetched,
        totalInserted: totals.inserted,
        totalDuplicates: totals.duplicates,
        totalErrors: totals.errors,
        errors
    };
}

function isDue(source) {
    if (!source.last_fetched_at) return true;
    const last = Date.parse(source.last_fetched_at);
    if (!Number.isFinite(last)) return true;
    const hours = Number(source.polling_interval_hours) || 6;
    // 5 minute grace so a 6h cron doesn't skip a 6h source due to jitter.
    return Date.now() - last >= hours * 3600_000 - 5 * 60_000;
}

async function importSource(db, source) {
    const notices = await scrapeSource(source);
    const now = new Date().toISOString();

    const statements = [];
    for (const n of notices) {
        const contentHash = await sha256Hex(`${source.id}|${n.official_notification_url}|${n.title.toLowerCase()}`);
        statements.push(db.prepare(`
            INSERT INTO jobs (
                id, title, slug, organization, category, state,
                official_url, official_notification_url,
                source_id, source_domain, advertisement_number,
                fetched_at, status, verified, content_hash, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending_review', 0, ?, ?, ?)
            ON CONFLICT DO NOTHING
        `).bind(
            `job-imp-${crypto.randomUUID()}`,
            n.title,
            `${slugify(n.title) || "notice"}-${shortId()}`,
            n.organization,
            n.category,
            n.state,
            n.official_url,
            n.official_notification_url,
            source.id,
            n.source_domain,
            n.advertisement_number,
            now,
            contentHash,
            now,
            now
        ));
    }

    let inserted = 0;
    if (statements.length) {
        const results = await db.batch(statements);
        inserted = results.reduce((acc, r) => acc + (r.meta && r.meta.changes ? r.meta.changes : 0), 0);
    }

    await db.prepare(
        "UPDATE job_sources SET last_fetched_at = ?, last_successful_fetch_at = ?, last_error = NULL WHERE id = ?"
    ).bind(now, now, source.id).run();

    return { fetched: notices.length, inserted, duplicates: notices.length - inserted };
}

/**
 * Marks ACTIVE notices whose ISO last_date is in the past as EXPIRED.
 * Only touches application-type categories where a closing date is meaningful.
 */
export async function expireOldJobs(env) {
    const today = new Date().toISOString().slice(0, 10);
    const res = await env.DB.prepare(`
        UPDATE jobs
        SET status = 'EXPIRED', updated_at = ?
        WHERE status = 'ACTIVE'
          AND category IN ('Latest Job', 'Admission')
          AND last_date GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]'
          AND last_date < ?
    `).bind(new Date().toISOString(), today).run();
    return res.meta ? res.meta.changes : 0;
}
