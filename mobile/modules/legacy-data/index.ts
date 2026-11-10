import LegacyData from './src/LegacyDataModule';
import type { LegacyPayload } from './src/LegacyData.types';

export type { LegacyPayload } from './src/LegacyData.types';

export async function readLegacyData(): Promise<LegacyPayload | null> {
  if (!LegacyData) return null;
  const raw = await LegacyData.readAsync();
  if (!raw) return null;
  return JSON.parse(raw) as LegacyPayload;
}

export async function clearLegacyData(): Promise<void> {
  await LegacyData?.clearAsync();
}
