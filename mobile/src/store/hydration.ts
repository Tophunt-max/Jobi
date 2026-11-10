import { useSyncExternalStore } from 'react';

import { useContentStore } from './contentStore';
import { useSettingsStore } from './settingsStore';
import { useUserStore } from './userStore';

const stores = [useContentStore, useUserStore, useSettingsStore];

function allHydrated() {
  return stores.every((s) => s.persist.hasHydrated());
}

function subscribe(onChange: () => void) {
  const unsubs = stores.map((s) => s.persist.onFinishHydration(onChange));
  return () => unsubs.forEach((u) => u());
}

/** True once every persisted store has loaded from disk. */
export function useStoresHydrated(): boolean {
  return useSyncExternalStore(subscribe, allHydrated, () => false);
}
