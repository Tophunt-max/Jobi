/**
 * Input validation and normalisation helpers.
 */
import { HttpError } from "./http.js";

export const CATEGORIES = ["Latest Job", "Admit Card", "Result", "Answer Key", "Syllabus", "Admission"];
export const PUBLIC_STATUSES = ["ACTIVE", "EXPIRED"];
export const ADMIN_STATUSES = ["ACTIVE", "EXPIRED", "pending_review", "REJECTED"];

export function clampInt(raw, { min, max, fallback }) {
    if (raw === null || raw === undefined || raw === "") return fallback;
    const n = Number.parseInt(String(raw), 10);
    if (!Number.isFinite(n)) return fallback;
    return Math.min(max, Math.max(min, n));
}

export function optionalString(value, field, { max = 500 } = {}) {
    if (value === undefined || value === null) return null;
    if (typeof value !== "string" && typeof value !== "number") {
        throw new HttpError(400, `${field} must be a string`);
    }
    const s = String(value).trim();
    if (s.length > max) throw new HttpError(400, `${field} must be at most ${max} characters`);
    return s === "" ? null : s;
}

export function requiredString(value, field, opts) {
    const s = optionalString(value, field, opts);
    if (!s) throw new HttpError(400, `${field} is required`);
    return s;
}

export function optionalInt(value, field, { min = 0, max = 10_000_000 } = {}) {
    if (value === undefined || value === null || value === "") return null;
    const n = Number(value);
    if (!Number.isInteger(n) || n < min || n > max) {
        throw new HttpError(400, `${field} must be an integer between ${min} and ${max}`);
    }
    return n;
}

export function optionalHttpsUrl(value, field) {
    const s = optionalString(value, field, { max: 2048 });
    if (!s) return null;
    let u;
    try {
        u = new URL(s);
    } catch {
        throw new HttpError(400, `${field} must be a valid URL`);
    }
    if (u.protocol !== "https:") {
        throw new HttpError(400, `${field} must use https://`);
    }
    return u.toString();
}

export function oneOf(value, field, allowed, fallback = undefined) {
    if (value === undefined || value === null || value === "") {
        if (fallback !== undefined) return fallback;
        throw new HttpError(400, `${field} is required`);
    }
    if (!allowed.includes(value)) {
        throw new HttpError(400, `${field} must be one of: ${allowed.join(", ")}`);
    }
    return value;
}

/**
 * Normalises common Indian date formats to ISO YYYY-MM-DD.
 * Unrecognised strings (e.g. "September 2026", "To be announced") are kept as-is
 * so no information is lost; they simply won't sort or expire automatically.
 */
export function normalizeDate(raw) {
    if (raw === undefined || raw === null) return null;
    const clean = String(raw).trim();
    if (!clean) return null;

    if (/^\d{4}-\d{2}-\d{2}$/.test(clean)) return clean;

    const m = clean.match(/^(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})$/);
    if (m) {
        const [, d, mo, y] = m;
        const day = Number(d);
        const month = Number(mo);
        if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
            return `${y}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
        }
    }
    return clean.slice(0, 100);
}

export function slugify(text) {
    return String(text)
        .toLowerCase()
        .normalize("NFKD")
        .replace(/[^\w\s-]/g, "")
        .trim()
        .replace(/[\s_-]+/g, "-")
        .replace(/^-+|-+$/g, "")
        .slice(0, 80);
}

export function shortId() {
    return crypto.randomUUID().replace(/-/g, "").slice(0, 10);
}

export async function sha256Hex(message) {
    const buf = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(message));
    return Array.from(new Uint8Array(buf))
        .map((b) => b.toString(16).padStart(2, "0"))
        .join("");
}

/**
 * Validates the body for manually creating a job from the admin panel.
 * Only real columns from the schema are accepted; nothing is invented.
 */
export function validateManualJob(body) {
    const title = requiredString(body.title, "title", { max: 300 });
    const organization = requiredString(body.organization, "organization", { max: 200 });
    const category = oneOf(body.category, "category", CATEGORIES, "Latest Job");
    const minAge = optionalInt(body.min_age, "min_age", { min: 10, max: 70 });
    const maxAge = optionalInt(body.max_age, "max_age", { min: 10, max: 70 });
    if (minAge !== null && maxAge !== null && minAge > maxAge) {
        throw new HttpError(400, "min_age cannot be greater than max_age");
    }

    const officialUrl = optionalHttpsUrl(body.official_url, "official_url");
    const notificationUrl = optionalHttpsUrl(body.official_notification_url, "official_notification_url");
    const applyUrl = optionalHttpsUrl(body.official_apply_url, "official_apply_url");
    if (!officialUrl && !notificationUrl) {
        throw new HttpError(400, "Either official_url or official_notification_url is required");
    }

    const sourceDomain = new URL(notificationUrl || officialUrl).hostname;

    return {
        title,
        organization,
        department: optionalString(body.department, "department", { max: 200 }),
        post_name: optionalString(body.post_name, "post_name", { max: 300 }),
        category,
        state: optionalString(body.state, "state", { max: 60 }) || "All India",
        total_posts: optionalInt(body.total_posts, "total_posts", { min: 0, max: 1_000_000 }) ?? 0,
        qualification: optionalString(body.qualification, "qualification", { max: 1000 }),
        min_age: minAge,
        max_age: maxAge,
        application_start_date: normalizeDate(body.application_start_date),
        last_date: normalizeDate(body.last_date),
        exam_date: normalizeDate(body.exam_date),
        fee_general: optionalString(body.fee_general, "fee_general", { max: 100 }),
        fee_sc_st: optionalString(body.fee_sc_st, "fee_sc_st", { max: 100 }),
        salary_scale: optionalString(body.salary_scale, "salary_scale", { max: 200 }),
        selection_process: optionalString(body.selection_process, "selection_process", { max: 500 }),
        official_url: officialUrl,
        official_notification_url: notificationUrl,
        official_apply_url: applyUrl,
        advertisement_number: optionalString(body.advertisement_number, "advertisement_number", { max: 120 }),
        notification_pdf_r2_key: optionalString(body.notification_pdf_r2_key, "notification_pdf_r2_key", { max: 200 }),
        source_domain: sourceDomain,
        is_featured: body.is_featured ? 1 : 0,
        approved_by: optionalString(body.approved_by, "approved_by", { max: 80 }) || "admin-panel"
    };
}

/**
 * Ad configuration schema. Unknown keys are dropped.
 */
const AD_BOOL_KEYS = ["masterEnabled", "bannerEnabled", "interstitialEnabled", "nativeEnabled", "rewardedEnabled", "customSponsorActive"];
const AD_UNIT_KEYS = ["bannerId", "interstitialId", "nativeId", "rewardedId"];
const AD_TEXT_KEYS = { sponsorName: 80, sponsorHeadline: 120, sponsorBody: 300, sponsorRating: 20, sponsorCtaText: 40, sponsorEmoji: 8 };

export function validateAdConfig(body, defaults) {
    const out = { ...defaults };

    for (const k of AD_BOOL_KEYS) {
        if (k in body) {
            if (typeof body[k] !== "boolean") throw new HttpError(400, `${k} must be a boolean`);
            out[k] = body[k];
        }
    }
    for (const k of AD_UNIT_KEYS) {
        if (k in body) {
            const v = String(body[k] ?? "").trim();
            if (v && !/^ca-app-pub-\d{16}\/\d{10}$/.test(v)) {
                throw new HttpError(400, `${k} must be an AdMob ad unit ID (ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX) or empty`);
            }
            out[k] = v;
        }
    }
    if ("interstitialClickInterval" in body) {
        out.interstitialClickInterval = optionalInt(body.interstitialClickInterval, "interstitialClickInterval", { min: 1, max: 50 }) ?? defaults.interstitialClickInterval;
    }
    if ("interstitialCooldownSeconds" in body) {
        out.interstitialCooldownSeconds = optionalInt(body.interstitialCooldownSeconds, "interstitialCooldownSeconds", { min: 15, max: 3600 }) ?? defaults.interstitialCooldownSeconds;
    }
    for (const [k, max] of Object.entries(AD_TEXT_KEYS)) {
        if (k in body) out[k] = optionalString(body[k], k, { max }) ?? "";
    }
    if ("sponsorTargetUrl" in body) {
        out.sponsorTargetUrl = optionalHttpsUrl(body.sponsorTargetUrl, "sponsorTargetUrl") ?? "";
    }
    return out;
}
