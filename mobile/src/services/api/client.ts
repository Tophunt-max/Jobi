import { env } from '@/config/env';
import { createLogger } from '@/lib/logger';

const log = createLogger('api');

export type ApiErrorKind = 'not_configured' | 'network' | 'timeout' | 'http' | 'parse' | 'rate_limited';

export class ApiError extends Error {
  constructor(
    readonly kind: ApiErrorKind,
    message: string,
    readonly status = 0,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

type RequestOptions = {
  method?: 'GET' | 'POST';
  body?: unknown;
  timeoutMs?: number;
  /** Retries apply to GET requests on network errors, timeouts and 5xx. */
  retries?: number;
  signal?: AbortSignal;
};

const DEFAULT_TIMEOUT_MS = 20_000;

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function once<T>(path: string, opts: RequestOptions): Promise<T> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), opts.timeoutMs ?? DEFAULT_TIMEOUT_MS);
  const onOuterAbort = () => controller.abort();
  opts.signal?.addEventListener('abort', onOuterAbort);

  let res: Response;
  try {
    res = await fetch(`${env.apiBaseUrl}${path}`, {
      method: opts.method ?? 'GET',
      headers: {
        Accept: 'application/json',
        ...(opts.body !== undefined ? { 'Content-Type': 'application/json' } : {}),
      },
      body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined,
      signal: controller.signal,
    });
  } catch {
    if (controller.signal.aborted && !opts.signal?.aborted) {
      throw new ApiError('timeout', 'The server took too long to respond');
    }
    throw new ApiError('network', 'Could not reach the server', 0);
  } finally {
    clearTimeout(timer);
    opts.signal?.removeEventListener('abort', onOuterAbort);
  }

  let payload: unknown = null;
  const text = await res.text();
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      throw new ApiError('parse', 'The server sent an unreadable response', res.status);
    }
  }

  if (!res.ok) {
    const message =
      (payload && typeof payload === 'object' && 'message' in payload && typeof payload.message === 'string'
        ? payload.message
        : null) ?? `Request failed (${res.status})`;
    throw new ApiError(res.status === 429 ? 'rate_limited' : 'http', message, res.status);
  }
  return payload as T;
}

/** JSON request to the Cloudflare Worker. Throws ApiError. */
export async function apiRequest<T>(path: string, opts: RequestOptions = {}): Promise<T> {
  if (!env.isApiConfigured) {
    throw new ApiError('not_configured', 'The backend URL is not configured in this build');
  }
  const method = opts.method ?? 'GET';
  const retries = method === 'GET' ? (opts.retries ?? 2) : 0;

  for (let attempt = 0; ; attempt++) {
    try {
      return await once<T>(path, opts);
    } catch (e) {
      const err = e as ApiError;
      const retryable = err.kind === 'network' || err.kind === 'timeout' || (err.kind === 'http' && err.status >= 500);
      if (!retryable || attempt >= retries || opts.signal?.aborted) {
        log.warn(`${method} ${path} failed`, err);
        throw err;
      }
      await sleep(600 * 2 ** attempt);
    }
  }
}
