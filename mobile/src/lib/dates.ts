/**
 * Date helpers. Official notices use many formats ("15-11-2026", "15/11/2026",
 * "15 Nov 2026", "2026-11-15"). Everything is normalised to ISO "YYYY-MM-DD"
 * at the API boundary so sorting and reminders work on one format.
 */

const MONTHS: Record<string, number> = {
  jan: 0, feb: 1, mar: 2, apr: 3, may: 4, jun: 5,
  jul: 6, aug: 7, sep: 8, sept: 8, oct: 9, nov: 10, dec: 11,
};

function build(year: number, monthIndex: number, day: number): Date | null {
  if (year < 1900 || year > 2200 || monthIndex < 0 || monthIndex > 11 || day < 1 || day > 31) return null;
  const d = new Date(year, monthIndex, day);
  // Reject roll-overs such as 31 Feb.
  if (d.getFullYear() !== year || d.getMonth() !== monthIndex || d.getDate() !== day) return null;
  return d;
}

/** Parses a date in any supported format to local midnight, or null. */
export function parseDate(input: string | null | undefined): Date | null {
  const value = (input ?? '').trim();
  if (!value) return null;

  let m = /^(\d{4})-(\d{1,2})-(\d{1,2})(?:[T ].*)?$/.exec(value);
  if (m) return build(Number(m[1]), Number(m[2]) - 1, Number(m[3]));

  m = /^(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})$/.exec(value);
  if (m) return build(Number(m[3]), Number(m[2]) - 1, Number(m[1]));

  // 15 Nov 2026, 15 November 2026, 15-Nov-2026
  m = /^(\d{1,2})(?:st|nd|rd|th)?[\s\-/.]+([A-Za-z]{3,9})[\s\-/.,]+(\d{4})$/.exec(value);
  if (m) {
    const month = MONTHS[m[2]!.slice(0, m[2]!.toLowerCase().startsWith('sept') ? 4 : 3).toLowerCase()];
    if (month !== undefined) return build(Number(m[3]), month, Number(m[1]));
  }

  // Nov 15, 2026
  m = /^([A-Za-z]{3,9})\s+(\d{1,2})(?:st|nd|rd|th)?,?\s+(\d{4})$/.exec(value);
  if (m) {
    const month = MONTHS[m[1]!.slice(0, 3).toLowerCase()];
    if (month !== undefined) return build(Number(m[3]), month, Number(m[2]));
  }

  return null;
}

function pad(n: number): string {
  return n < 10 ? `0${n}` : String(n);
}

export function toIsoDate(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

/** Returns ISO "YYYY-MM-DD" when the input is a recognisable date, otherwise "". */
export function normalizeDate(input: string | null | undefined): string {
  const d = parseDate(input);
  return d ? toIsoDate(d) : '';
}

export function startOfToday(now: Date = new Date()): Date {
  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

/** Whole days from today until the ISO date (negative when it has passed), or null. */
export function daysUntil(iso: string, now: Date = new Date()): number | null {
  const d = parseDate(iso);
  if (!d) return null;
  return Math.round((d.getTime() - startOfToday(now).getTime()) / 86_400_000);
}

/** "15 Nov 2026" for ISO dates; free text is returned unchanged. */
export function formatDate(value: string, locale = 'en-IN'): string {
  const d = parseDate(value);
  if (!d) return value;
  try {
    return d.toLocaleDateString(locale, { day: 'numeric', month: 'short', year: 'numeric' });
  } catch {
    return toIsoDate(d);
  }
}

/** Parses D1 timestamps ("2026-11-10 08:00:00" is UTC) to epoch milliseconds, or 0. */
export function parseServerTimestamp(value: string | null | undefined): number {
  if (!value) return 0;
  let iso = value.includes('T') ? value : value.replace(' ', 'T');
  if (!/(Z|[+-]\d\d:?\d\d)$/.test(iso)) iso += 'Z';
  const t = Date.parse(iso);
  return Number.isFinite(t) ? t : 0;
}

/** Age in completed years on a given date. */
export function ageOn(dob: Date, on: Date): { years: number; months: number; days: number } {
  let years = on.getFullYear() - dob.getFullYear();
  let months = on.getMonth() - dob.getMonth();
  let days = on.getDate() - dob.getDate();
  if (days < 0) {
    months -= 1;
    days += new Date(on.getFullYear(), on.getMonth(), 0).getDate();
  }
  if (months < 0) {
    years -= 1;
    months += 12;
  }
  return { years, months, days };
}

export function relativeTime(epochMs: number, now: number = Date.now()): string {
  const diff = Math.max(0, now - epochMs);
  const minutes = Math.floor(diff / 60_000);
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 30) return `${days}d ago`;
  return new Date(epochMs).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
}
