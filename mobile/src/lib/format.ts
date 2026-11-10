export function formatInr(amount: number): string {
  try {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  } catch {
    return `₹${Math.round(amount).toLocaleString('en-IN')}`;
  }
}

export function formatCount(n: number): string {
  try {
    return n.toLocaleString('en-IN');
  } catch {
    return String(n);
  }
}

export function hostnameOf(url: string): string {
  const m = /^https?:\/\/([^/?#:]+)/i.exec(url.trim());
  return m ? m[1]!.toLowerCase() : '';
}

export function isHttpsUrl(url: string | null | undefined): url is string {
  return typeof url === 'string' && /^https:\/\/[^\s]+$/i.test(url.trim());
}

/** Upgrades http:// links to https:// (government sites almost all support it). */
export function toHttps(url: string): string {
  return url.trim().replace(/^http:\/\//i, 'https://');
}

export function isPdfUrl(url: string): boolean {
  return /\.pdf($|[?#])/i.test(url) || /\/api\/files\//.test(url);
}

export function makeId(prefix = 'id'): string {
  const rand = Math.random().toString(36).slice(2, 10);
  return `${prefix}-${Date.now().toString(36)}-${rand}`;
}
