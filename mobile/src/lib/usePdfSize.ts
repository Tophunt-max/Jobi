import { useEffect, useState } from 'react';

const cache = new Map<string, number | null>();

/**
 * File size of a linked PDF from a HEAD request, or null when the server does not say.
 * Results are cached for the session. Browsers may block the request (CORS); that also gives null.
 */
export function usePdfSize(url: string): number | null {
  const [size, setSize] = useState<number | null>(() => cache.get(url) ?? null);

  useEffect(() => {
    if (!url || cache.has(url)) return;
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 8000);
    fetch(url, { method: 'HEAD', signal: controller.signal })
      .then((res) => {
        const len = Number(res.headers.get('content-length'));
        const value = res.ok && Number.isFinite(len) && len > 0 ? len : null;
        cache.set(url, value);
        setSize(value);
      })
      .catch(() => cache.set(url, null))
      .finally(() => clearTimeout(timer));
    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [url]);

  return size;
}

export function formatBytes(bytes: number): string {
  if (bytes >= 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  return `${Math.max(1, Math.round(bytes / 1024))} KB`;
}
