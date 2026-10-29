/**
 * Generic official-portal scraper.
 *
 * Fetches a source's public listing page and extracts recruitment-related links
 * (notifications, advertisements, admit cards, results, answer keys, syllabus,
 * admissions) using the Workers HTMLRewriter streaming parser.
 *
 * Extracted notices are always inserted as `pending_review` and must be approved
 * by an admin before they become visible in the app. Only fields that can be read
 * from the page are populated; nothing is invented.
 */

const USER_AGENT = "SarkariJobPortalBot/2.0 (+https://github.com/Tophunt-max/Jobi; official-notices-indexer)";
const FETCH_TIMEOUT_MS = 15_000;
const MAX_HTML_BYTES = 3 * 1024 * 1024;
const MAX_NOTICES_PER_SOURCE = 25;

const INCLUDE_PATTERN = /(recruit|vacanc|advertis|advt|notification|notice|employment|engagement|walk[-\s]?in|admit\s*card|hall\s*ticket|e-?admit|call\s*letter|result|merit\s*list|select(ed|ion)\s*list|answer\s*key|syllabus|exam(ination)?\s*(pattern|scheme|schedule|date)|admission|apply\s*online|online\s*application|bharti|bharati)/i;
const EXCLUDE_PATTERN = /(tender|auction|rti\b|right to information|citizen charter|contact us|sitemap|privacy|disclaimer|copyright|feedback|grievance|login|sign\s*in|home\s*page|screen\s*reader|skip to|annual report|budget|gazette of tender|corrigendum to tender|e-?procurement|quotation|citizen engagement)/i;

export async function scrapeSource(source) {
    const listingUrl = new URL(source.listing_url);
    if (listingUrl.protocol !== "https:" && listingUrl.protocol !== "http:") {
        throw new Error(`Unsupported listing URL scheme: ${listingUrl.protocol}`);
    }

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), FETCH_TIMEOUT_MS);
    let response;
    try {
        response = await fetch(listingUrl.toString(), {
            method: "GET",
            redirect: "follow",
            signal: controller.signal,
            headers: {
                "User-Agent": USER_AGENT,
                "Accept": "text/html,application/xhtml+xml;q=0.9,*/*;q=0.5",
                "Accept-Language": "en-IN,en;q=0.9,hi;q=0.8"
            }
        });
    } catch (err) {
        if (err && err.name === "AbortError") {
            throw new Error(`Timed out after ${FETCH_TIMEOUT_MS / 1000}s`);
        }
        throw new Error(`Network error: ${err && err.message ? err.message : String(err)}`);
    } finally {
        clearTimeout(timer);
    }

    if (!response.ok) {
        // Drain body to free the connection.
        await response.body?.cancel();
        throw new Error(`HTTP ${response.status} from ${listingUrl.hostname}`);
    }

    const contentType = response.headers.get("content-type") || "";
    if (!/text\/html|application\/xhtml/i.test(contentType)) {
        await response.body?.cancel();
        throw new Error(`Unexpected content-type: ${contentType || "none"}`);
    }

    const declared = Number(response.headers.get("content-length") || "0");
    if (declared > MAX_HTML_BYTES) {
        await response.body?.cancel();
        throw new Error(`Listing page too large (${declared} bytes)`);
    }

    const finalUrl = new URL(response.url || listingUrl.toString());
    const links = await extractLinks(response);
    return toNotices(links, source, finalUrl);
}

/**
 * Collects <a href> elements with their visible text.
 */
async function extractLinks(response) {
    const links = [];
    let current = null;

    const rewriter = new HTMLRewriter()
        .on("a[href]", {
            element(el) {
                current = { href: el.getAttribute("href") || "", text: "", title: el.getAttribute("title") || "" };
                links.push(current);
                el.onEndTag(() => {
                    current = null;
                });
            },
            text(chunk) {
                if (current && current.text.length < 600) {
                    current.text += chunk.text;
                }
            }
        });

    // Consume the transformed stream so handlers run, with a byte cap.
    const reader = rewriter.transform(response).body.getReader();
    let total = 0;
    while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        total += value.byteLength;
        if (total > MAX_HTML_BYTES) {
            await reader.cancel();
            break;
        }
    }
    return links;
}

function toNotices(links, source, baseUrl) {
    const allowedSuffixes = allowedHostSuffixes(source.official_domain);
    const seen = new Set();
    const notices = [];

    for (const link of links) {
        if (notices.length >= MAX_NOTICES_PER_SOURCE) break;

        const text = cleanText(link.text || link.title);
        if (text.length < 12 || text.length > 300) continue;
        if (!INCLUDE_PATTERN.test(text) || EXCLUDE_PATTERN.test(text)) continue;
        if (isGenericMenuLabel(text)) continue;

        const href = (link.href || "").trim();
        if (!href || href.startsWith("#") || /^(javascript|mailto|tel|data):/i.test(href)) continue;

        let abs;
        try {
            abs = new URL(href, baseUrl);
        } catch {
            continue;
        }
        if (abs.protocol !== "https:" && abs.protocol !== "http:") continue;
        if (!isAllowedHost(abs.hostname, allowedSuffixes)) continue;

        abs.hash = "";
        const canonical = abs.toString();
        if (seen.has(canonical)) continue;
        seen.add(canonical);

        notices.push({
            title: text,
            organization: source.name,
            category: inferCategory(text),
            state: source.state || "All India",
            official_url: `${baseUrl.protocol}//${baseUrl.host}/`,
            official_notification_url: canonical,
            advertisement_number: extractAdvtNumber(text),
            source_domain: abs.hostname
        });
    }
    return notices;
}

/**
 * Navigation/menu links ("Notifications", "Apply Online", "Examination Schedule") point to
 * listing pages, not to a specific notice. Real notices are longer or carry a number/year.
 */
export function isGenericMenuLabel(text) {
    const words = text.split(/\s+/).filter(Boolean);
    return words.length < 4 && !/\d/.test(text);
}

function cleanText(s) {
    return String(s || "")
        .replace(/&nbsp;/gi, " ")
        .replace(/&amp;/gi, "&")
        .replace(/\s+/g, " ")
        .replace(/^[»›•\-\s]+/, "")
        .replace(/\b(new|NEW)\b\s*$/g, "")
        .trim();
}

/**
 * Links must stay on the source's own domain or on Indian government hosts
 * (.gov.in / .nic.in), which official portals commonly use for applications.
 */
function allowedHostSuffixes(officialDomain) {
    const d = String(officialDomain || "").toLowerCase().replace(/^www\./, "");
    return [d, ".gov.in", ".nic.in"].filter(Boolean);
}

function isAllowedHost(hostname, suffixes) {
    const h = hostname.toLowerCase();
    return suffixes.some((s) => (s.startsWith(".") ? h.endsWith(s) : h === s || h.endsWith(`.${s}`)));
}

export function inferCategory(text) {
    const t = text.toLowerCase();
    if (/answer\s*key/.test(t)) return "Answer Key";
    if (/admit\s*card|hall\s*ticket|e-?admit|call\s*letter/.test(t)) return "Admit Card";
    if (/result|merit\s*list|select(ed|ion)\s*list|score\s*card|cut[-\s]?off/.test(t)) return "Result";
    if (/syllabus|exam(ination)?\s*(pattern|scheme)/.test(t)) return "Syllabus";
    if (/admission|entrance|counselling|counseling/.test(t)) return "Admission";
    return "Latest Job";
}

export function extractAdvtNumber(text) {
    const m = text.match(/\b(?:advt\.?|advertisement|notice|cen|notification)\s*(?:no\.?|number)?\s*[:.-]?\s*([A-Z0-9][A-Z0-9/().-]{2,40})/i);
    return m ? m[1].replace(/[.,)]+$/, "") : null;
}
