import AsyncStorage from '@react-native-async-storage/async-storage';
import { createJSONStorage, type StateStorage } from 'zustand/middleware';

import { createLogger } from './logger';

const log = createLogger('storage');

/**
 * Key-value storage shared by every persisted store.
 * AsyncStorage is backed by SQLite on Android, files on iOS and localStorage on web.
 * Keys are versioned so a schema change can start fresh instead of crashing.
 */
const safeStorage: StateStorage = {
  getItem: async (name) => {
    try {
      const raw = await AsyncStorage.getItem(name);
      if (raw === null) return null;
      try {
        JSON.parse(raw);
      } catch {
        // zustand would throw on this and never finish hydrating; start that store fresh instead.
        log.warn(`discarding unreadable ${name}`);
        await AsyncStorage.removeItem(name).catch(() => {});
        return null;
      }
      return raw;
    } catch (e) {
      log.warn(`read failed for ${name}`, e);
      return null;
    }
  },
  setItem: async (name, value) => {
    try {
      await AsyncStorage.setItem(name, value);
    } catch (e) {
      log.warn(`write failed for ${name}`, e);
    }
  },
  removeItem: async (name) => {
    try {
      await AsyncStorage.removeItem(name);
    } catch (e) {
      log.warn(`remove failed for ${name}`, e);
    }
  },
};

export const persistStorage = createJSONStorage(() => safeStorage);

export async function readJson<T>(key: string): Promise<T | null> {
  const raw = await safeStorage.getItem(key);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as T;
  } catch (e) {
    log.warn(`discarding unreadable ${key}`, e);
    return null;
  }
}

export async function writeJson(key: string, value: unknown): Promise<void> {
  await safeStorage.setItem(key, JSON.stringify(value));
}

export const StorageKeys = {
  content: 'sj.content.v1',
  user: 'sj.user.v1',
  settings: 'sj.settings.v1',
  ads: 'sj.ads.v1',
} as const;
