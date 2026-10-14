/**
 * Cloudflare Worker Backend for Sarkari Job Portal
 * Integrates Cloudflare D1 (SQLite) and Cloudflare R2 (Object Storage for PDFs)
 */

export default {
    async fetch(request, env, ctx) {
        const url = new URL(request.url);
        const path = url.pathname;
        const method = request.method;

        // CORS headers to allow Android & Web access
        const corsHeaders = {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
            "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Admin-Key",
        };

        if (method === "OPTIONS") {
            return new Response(null, { headers: corsHeaders });
        }

        try {
            // Root health check
            if (path === "/" || path === "/api/health") {
                return jsonResponse({
                    status: "online",
                    backend: "Cloudflare Workers",
                    database: "Cloudflare D1 (SQLite)",
                    storage: "Cloudflare R2 (Zero Egress)",
                    timestamp: new Date().toISOString()
                }, corsHeaders);
            }

            // 1. GET /api/jobs - List or filter jobs
            if (path === "/api/jobs" && method === "GET") {
                const category = url.searchParams.get("category");
                const search = url.searchParams.get("search");
                const status = url.searchParams.get("status") || "ACTIVE";
                const limit = parseInt(url.searchParams.get("limit") || "50", 10);

                let query = "SELECT * FROM jobs WHERE 1=1";
                const params = [];

                if (category && category !== "All") {
                    query += " AND category = ?";
                    params.push(category);
                }

                if (status) {
                    query += " AND status = ?";
                    params.push(status);
                }

                if (search && search.trim() !== "") {
                    query += " AND (title LIKE ? OR department LIKE ? OR qualification LIKE ?)";
                    const term = `%${search.trim()}%`;
                    params.push(term, term, term);
                }

                query += " ORDER BY created_at DESC LIMIT ?";
                params.push(limit);

                const stmt = env.DB.prepare(query).bind(...params);
                const { results } = await stmt.all();

                return jsonResponse({
                    success: true,
                    count: results ? results.length : 0,
                    data: results || []
                }, corsHeaders);
            }

            // 2. GET /api/jobs/:id - Single job detail
            if (path.startsWith("/api/jobs/") && method === "GET") {
                const id = path.replace("/api/jobs/", "");
                const job = await env.DB.prepare("SELECT * FROM jobs WHERE id = ?").bind(id).first();

                if (!job) {
                    return jsonResponse({ success: false, message: "Job not found" }, corsHeaders, 404);
                }

                return jsonResponse({ success: true, data: job }, corsHeaders);
            }

            // 3. POST /api/jobs - Create or update job (Admin action)
            if (path === "/api/jobs" && method === "POST") {
                const body = await request.json();
                const id = body.id || `job-${Date.now()}`;
                const title = body.title || "";
                const department = body.department || "";
                const postName = body.postName || body.post_name || "";
                const category = body.category || "Latest Job";
                const totalPosts = parseInt(body.totalPosts || body.total_posts || 0, 10);
                const qualification = body.qualification || "";
                const minAge = parseInt(body.minAge || body.min_age || 18, 10);
                const maxAge = parseInt(body.maxAge || body.max_age || 40, 10);
                const appStartDate = body.applicationStartDate || body.application_start_date || "";
                const lastDate = body.lastDate || body.last_date || "";
                const examDate = body.examDate || body.exam_date || "";
                const feeGeneral = body.feeGeneral || body.fee_general || "";
                const feeScSt = body.feeScSt || body.fee_sc_st || "";
                const salaryScale = body.salaryScale || body.salary_scale || "";
                const officialUrl = body.officialUrl || body.official_url || "";
                const applyUrl = body.applyUrl || body.apply_url || "";
                const pdfKey = body.notificationPdfR2Key || body.notification_pdf_r2_key || "";
                const status = body.status || "ACTIVE";
                const isFeatured = body.isFeatured ? 1 : 0;

                await env.DB.prepare(`
                    INSERT INTO jobs (
                        id, title, department, post_name, category, total_posts, qualification,
                        min_age, max_age, application_start_date, last_date, exam_date,
                        fee_general, fee_sc_st, salary_scale, official_url, apply_url,
                        notification_pdf_r2_key, status, is_featured, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                    ON CONFLICT(id) DO UPDATE SET
                        title = excluded.title,
                        department = excluded.department,
                        post_name = excluded.post_name,
                        category = excluded.category,
                        total_posts = excluded.total_posts,
                        qualification = excluded.qualification,
                        min_age = excluded.min_age,
                        max_age = excluded.max_age,
                        application_start_date = excluded.application_start_date,
                        last_date = excluded.last_date,
                        exam_date = excluded.exam_date,
                        fee_general = excluded.fee_general,
                        fee_sc_st = excluded.fee_sc_st,
                        salary_scale = excluded.salary_scale,
                        official_url = excluded.official_url,
                        apply_url = excluded.apply_url,
                        notification_pdf_r2_key = excluded.notification_pdf_r2_key,
                        status = excluded.status,
                        is_featured = excluded.is_featured,
                        updated_at = CURRENT_TIMESTAMP
                `).bind(
                    id, title, department, postName, category, totalPosts, qualification,
                    minAge, maxAge, appStartDate, lastDate, examDate,
                    feeGeneral, feeScSt, salaryScale, officialUrl, applyUrl,
                    pdfKey, status, isFeatured
                ).run();

                return jsonResponse({
                    success: true,
                    message: "Job saved to Cloudflare D1 successfully",
                    id: id
                }, corsHeaders, 201);
            }

            // 4. GET /api/stats - Dashboard summary metrics
            if (path === "/api/stats" && method === "GET") {
                const total = await env.DB.prepare("SELECT COUNT(*) as count FROM jobs").first();
                const latest = await env.DB.prepare("SELECT COUNT(*) as count FROM jobs WHERE category = 'Latest Job'").first();
                const admits = await env.DB.prepare("SELECT COUNT(*) as count FROM jobs WHERE category = 'Admit Card'").first();
                const results = await env.DB.prepare("SELECT COUNT(*) as count FROM jobs WHERE category = 'Result'").first();

                return jsonResponse({
                    success: true,
                    data: {
                        totalJobs: total ? total.count : 0,
                        latestJobs: latest ? latest.count : 0,
                        admitCards: admits ? admits.count : 0,
                        results: results ? results.count : 0
                    }
                }, corsHeaders);
            }

            // 5. POST /api/upload - Upload file to Cloudflare R2 bucket (Zero Egress!)
            if (path === "/api/upload" && method === "POST") {
                const filename = url.searchParams.get("filename") || `doc-${Date.now()}.pdf`;
                const contentType = request.headers.get("content-type") || "application/pdf";
                const fileBody = await request.arrayBuffer();

                if (!env.FILES_BUCKET) {
                    return jsonResponse({
                        success: false,
                        message: "R2 FILES_BUCKET binding not configured in wrangler.toml"
                    }, corsHeaders, 500);
                }

                await env.FILES_BUCKET.put(filename, fileBody, {
                    httpMetadata: { contentType: contentType }
                });

                return jsonResponse({
                    success: true,
                    message: "File stored in Cloudflare R2 successfully with zero egress fees",
                    r2Key: filename,
                    downloadUrl: `${url.origin}/api/files/${encodeURIComponent(filename)}`
                }, corsHeaders, 201);
            }

            // 6. GET /api/files/:filename - Stream file from Cloudflare R2
            if (path.startsWith("/api/files/") && method === "GET") {
                const filename = decodeURIComponent(path.replace("/api/files/", ""));

                if (!env.FILES_BUCKET) {
                    return new Response("R2 FILES_BUCKET binding missing", { status: 500, headers: corsHeaders });
                }

                const object = await env.FILES_BUCKET.get(filename);
                if (!object) {
                    return new Response("File not found in R2 bucket", { status: 404, headers: corsHeaders });
                }

                const headers = new Headers();
                object.writeHttpMetadata(headers);
                headers.set("etag", object.httpEtag);
                headers.set("Access-Control-Allow-Origin", "*");
                headers.set("Cache-Control", "public, max-age=86400"); // 1 day edge cache

                return new Response(object.body, { headers });
            }

            return jsonResponse({ success: false, message: `Route not found: ${path}` }, corsHeaders, 404);

        } catch (err) {
            return jsonResponse({
                success: false,
                error: err.message || "Internal server error"
            }, corsHeaders, 500);
        }
    }
};

function jsonResponse(data, headers = {}, status = 200) {
    return new Response(JSON.stringify(data), {
        status: status,
        headers: {
            "Content-Type": "application/json;charset=UTF-8",
            ...headers
        }
    });
}
