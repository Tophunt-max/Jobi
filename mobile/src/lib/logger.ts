/**
 * Minimal logger. Debug output is dropped in production builds; warnings and
 * errors keep only the message so personal data never ends up in device logs.
 * Swap the sink for Sentry or similar when crash reporting is added.
 */
type Level = 'debug' | 'info' | 'warn' | 'error';

function describe(error: unknown): string {
  if (error instanceof Error) return `${error.name}: ${error.message}`;
  return typeof error === 'string' ? error : 'unknown error';
}

function log(level: Level, scope: string, message: string, error?: unknown) {
  if (!__DEV__ && (level === 'debug' || level === 'info')) return;
  const line = `[${scope}] ${message}${error === undefined ? '' : ` (${describe(error)})`}`;
  if (level === 'error') console.error(line);
  else if (level === 'warn') console.warn(line);
  else console.log(line);
}

export function createLogger(scope: string) {
  return {
    debug: (message: string) => log('debug', scope, message),
    info: (message: string) => log('info', scope, message),
    warn: (message: string, error?: unknown) => log('warn', scope, message, error),
    error: (message: string, error?: unknown) => log('error', scope, message, error),
  };
}
