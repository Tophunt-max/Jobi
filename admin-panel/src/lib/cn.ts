import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/** "2026-10-29T14:18:29.000Z" or "2026-10-29 14:18:29" -> "29 Oct 2026, 14:18" in local time. */
export function formatDateTime(value?: string | null): string {
  if (!value) return '—';
  const iso = value.includes('T') ? value : value.replace(' ', 'T') + (/[zZ]|[+-]\d\d:?\d\d$/.test(value) ? '' : 'Z');
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false });
}

/** Relative time like "5 min ago". */
export function timeAgo(value?: string | null): string {
  if (!value) return 'never';
  const iso = value.includes('T') ? value : value.replace(' ', 'T') + 'Z';
  const diff = Date.now() - new Date(iso).getTime();
  if (Number.isNaN(diff)) return value;
  const m = Math.round(diff / 60000);
  if (m < 1) return 'just now';
  if (m < 60) return `${m} min ago`;
  const h = Math.round(m / 60);
  if (h < 24) return `${h} h ago`;
  const d = Math.round(h / 24);
  return `${d} d ago`;
}

export function hostOf(url?: string | null): string {
  if (!url) return '';
  try {
    return new URL(url).hostname.replace(/^www\./, '');
  } catch {
    return '';
  }
}
