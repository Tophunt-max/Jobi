/**
 * Public write endpoints used by the Android app:
 *   POST /api/reports      - report wrong/outdated notice information
 *   POST /api/ai/generate  - AI mentor proxy (Gemini key stays server-side)
 * Both are rate limited per client IP.
 */
import { json, readJson, HttpError } from "../http.js";
import { optionalString, requiredString, sha256Hex, oneOf } from "../validation.js";

const REPORT_REASONS_MAX = 80;
const MAX_REPORTS_PER_IP_PER_DAY = 20;

const AI_TASKS = ["eligibility", "strategy", "query"];
const MAX_PROMPT_CHARS = 8000;
const GEMINI_TIMEOUT_MS = 60_000;

const SYSTEM_INSTRUCTION =
    "You are the AI Sarkari Job & Eligibility Mentor for Indian government job aspirants. " +
    "Give accurate, structured guidance on recruitment eligibility, reservation and age relaxation rules, " +
    "and exam preparation. Only answer questions about Indian government jobs, exams and admissions; " +
    "politely decline anything else. Always remind the user to confirm details in the official advertisement.";

async function clientHash(request, env) {
    const ip = request.headers.get("CF-Connecting-IP") || "unknown";
    return sha256Hex(`${env.REPORT_SALT || "sarkari"}|${ip}`);
}

export async function handleClient(request, env, url, cors) {
    const path = url.pathname;
    if (request.method !== "POST") return null;

    if (path === "/api/reports") {
        const body = await readJson(request, { maxBytes: 8 * 1024 });
        const jobId = requiredString(body.job_id, "job_id", { max: 120 });
        const reason = requiredString(body.reason, "reason", { max: REPORT_REASONS_MAX });
        const details = optionalString(body.details, "details", { max: 1000 });

        const job = await env.DB.prepare("SELECT id FROM jobs WHERE id = ? AND status IN ('ACTIVE', 'EXPIRED')").bind(jobId).first();
        if (!job) throw new HttpError(404, "Job not found");

        const reporter = await clientHash(request, env);
        const since = new Date(Date.now() - 24 * 3600_000).toISOString();
        const recent = await env.DB.prepare(
            "SELECT COUNT(*) AS c FROM job_reports WHERE reporter_hash = ? AND created_at >= ?"
        ).bind(reporter, since).first();
        if ((recent?.c || 0) >= MAX_REPORTS_PER_IP_PER_DAY) {
            throw new HttpError(429, "Too many reports today, please try again tomorrow");
        }

        await env.DB.prepare(`
            INSERT INTO job_reports (id, job_id, reason, details, status, reporter_hash, created_at)
            VALUES (?, ?, ?, ?, 'OPEN', ?, ?)
        `).bind(`rep-${crypto.randomUUID()}`, jobId, reason, details, reporter, new Date().toISOString()).run();

        return json({ success: true, message: "Report received" }, { status: 201, headers: cors });
    }

    if (path === "/api/ai/generate") {
        if (env.AI_RATE_LIMITER && typeof env.AI_RATE_LIMITER.limit === "function") {
            const ip = request.headers.get("CF-Connecting-IP") || "unknown";
            const { success } = await env.AI_RATE_LIMITER.limit({ key: `ai:${ip}` });
            if (!success) throw new HttpError(429, "AI mentor is busy, please try again in a minute");
        }
        const body = await readJson(request, { maxBytes: 32 * 1024 });
        const task = oneOf(body.task, "task", AI_TASKS);
        const prompt = requiredString(body.prompt, "prompt", { max: MAX_PROMPT_CHARS });

        if (!env.GEMINI_API_KEY) throw new HttpError(503, "AI mentor is not configured");

        const model = env.GEMINI_MODEL || "gemini-2.5-flash";
        const controller = new AbortController();
        const timer = setTimeout(() => controller.abort(), GEMINI_TIMEOUT_MS);
        let res;
        try {
            res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(model)}:generateContent`, {
                method: "POST",
                signal: controller.signal,
                headers: {
                    "Content-Type": "application/json",
                    "x-goog-api-key": env.GEMINI_API_KEY
                },
                body: JSON.stringify({
                    systemInstruction: { parts: [{ text: SYSTEM_INSTRUCTION }] },
                    contents: [{ role: "user", parts: [{ text: prompt }] }],
                    generationConfig: { temperature: task === "query" ? 0.5 : 0.3, maxOutputTokens: 2048 }
                })
            });
        } catch (err) {
            throw new HttpError(504, err && err.name === "AbortError" ? "AI mentor timed out" : "AI mentor is unreachable");
        } finally {
            clearTimeout(timer);
        }

        if (!res.ok) {
            console.warn(`Gemini HTTP ${res.status}: ${(await res.text()).slice(0, 300)}`);
            throw new HttpError(502, "AI mentor is temporarily unavailable");
        }
        const data = await res.json();
        const text = (data?.candidates?.[0]?.content?.parts || [])
            .map((p) => p.text || "")
            .join("")
            .trim();
        if (!text) throw new HttpError(502, "AI mentor returned an empty answer");

        return json({ success: true, text }, { headers: { ...cors, "Cache-Control": "no-store" } });
    }

    return null;
}
