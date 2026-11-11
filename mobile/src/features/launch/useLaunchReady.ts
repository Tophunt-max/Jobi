import { useEffect, useState } from 'react';

import { createLogger } from '@/lib/logger';
import { useStoresHydrated } from '@/store/hydration';

const log = createLogger('launch');

/**
 * Longest we wait for the persisted stores before showing the app anyway.
 * zustand never marks a store as hydrated when reading or migrating it throws, so without
 * this cap a broken storage entry would leave the user on the splash forever.
 */
export const HYDRATION_TIMEOUT_MS = 5000;

/**
 * True when the app can render its screens: every store has hydrated, or the timeout passed.
 * After a timeout the stores keep their defaults and still update if hydration finishes later.
 */
export function useLaunchReady(): boolean {
  const hydrated = useStoresHydrated();
  const [timedOut, setTimedOut] = useState(false);

  useEffect(() => {
    if (hydrated) return;
    const timer = setTimeout(() => {
      log.warn(`stores not hydrated after ${HYDRATION_TIMEOUT_MS} ms, continuing with defaults`);
      setTimedOut(true);
    }, HYDRATION_TIMEOUT_MS);
    return () => clearTimeout(timer);
  }, [hydrated]);

  return hydrated || timedOut;
}
