/**
 * Maps rows from the `jobs` table into the category-specific DTO shapes the
 * Android client expects (see app/.../data/remote/model/BackendDtos.kt).
 * Category-specific extras live in jobs.details_json.
 */

function details(row) {
    if (!row.details_json) return {};
    try {
        const d = JSON.parse(row.details_json);
        return d && typeof d === "object" ? d : {};
    } catch {
        return {};
    }
}

function domainOf(row) {
    if (row.source_domain) return row.source_domain;
    try {
        return new URL(row.official_notification_url || row.official_url).hostname;
    } catch {
        return "";
    }
}

function primaryLink(row) {
    return row.official_notification_url || row.official_url || "";
}

function publishedDate(row) {
    return (row.published_at || row.created_at || "").slice(0, 10);
}

export function toResultDto(row) {
    const d = details(row);
    return {
        id: row.id,
        exam_name: row.title,
        recruiting_org: row.organization,
        advt_number: row.advertisement_number || "",
        publication_date: row.result_date || publishedDate(row),
        official_result_url: primaryLink(row),
        cut_off_summary: d.cut_off_summary || "",
        merit_list_available: d.merit_list_available !== false,
        verification_domain: domainOf(row),
        stage: d.stage || "Result"
    };
}

export function toAdmitCardDto(row) {
    const d = details(row);
    return {
        id: row.id,
        exam_name: row.title,
        recruiting_org: row.organization,
        release_date: row.admit_card_date || publishedDate(row),
        exam_date: row.exam_date || "",
        official_download_url: row.official_apply_url || primaryLink(row),
        instructions: d.instructions || "",
        city_slip_released: d.city_slip_released === true,
        verification_domain: domainOf(row)
    };
}

export function toSyllabusDto(row) {
    const d = details(row);
    return {
        id: row.id,
        exam_name: row.title,
        recruiting_org: row.organization,
        exam_pattern: d.exam_pattern || "",
        subjects_and_marks_json: typeof d.subjects_and_marks === "string"
            ? d.subjects_and_marks
            : JSON.stringify(Array.isArray(d.subjects_and_marks) ? d.subjects_and_marks : []),
        duration_minutes: Number.isInteger(d.duration_minutes) ? d.duration_minutes : 0,
        negative_marking_rule: d.negative_marking_rule || "",
        official_pdf_url: primaryLink(row),
        verification_domain: domainOf(row)
    };
}

export function toAdmissionDto(row) {
    const d = details(row);
    const today = new Date().toISOString().slice(0, 10);
    const closed = row.status === "EXPIRED" || (/^\d{4}-\d{2}-\d{2}$/.test(row.last_date || "") && row.last_date < today);
    return {
        id: row.id,
        exam_or_course_name: row.title,
        conducting_authority: row.organization,
        eligibility_course: row.qualification || "",
        application_start_date: row.application_start_date || "",
        application_closing_date: row.last_date || "",
        entrance_exam_date: row.exam_date || "",
        official_apply_url: row.official_apply_url || row.official_url || primaryLink(row),
        official_information_bulletin_url: primaryLink(row),
        verification_domain: domainOf(row),
        status: d.status || (closed ? "CLOSED" : "OPEN")
    };
}

export function toAnswerKeyDto(row) {
    const d = details(row);
    return {
        id: row.id,
        exam_name: row.title,
        recruiting_org: row.organization,
        status: d.status || "PROVISIONAL",
        release_date: row.answer_key_date || publishedDate(row),
        objection_deadline: d.objection_deadline || row.last_date || "",
        objection_fee: d.objection_fee || row.fee_general || "",
        official_link: primaryLink(row),
        verification_domain: domainOf(row)
    };
}

export function toNotificationDto(row) {
    const ts = Date.parse((row.created_at || "").replace(" ", "T") + (/(Z|[+-]\d\d:?\d\d)$/.test(row.created_at || "") ? "" : "Z"));
    return {
        id: row.id,
        title: row.title,
        body: row.body,
        type: row.type || "NEW_JOB",
        target_slug: row.target_slug || row.job_id || "",
        timestamp: Number.isFinite(ts) ? ts : Date.now(),
        is_read: false,
        priority: row.priority || "HIGH"
    };
}

/** Public job shape: strips internal moderation fields. */
const INTERNAL_JOB_FIELDS = ["content_hash", "approved_by", "rejection_reason", "reviewed_at", "fetched_at", "details_json"];

export function toPublicJob(row) {
    const out = { ...row };
    for (const f of INTERNAL_JOB_FIELDS) delete out[f];
    return out;
}
