import { requireOptionalNativeModule, type NativeModule } from 'expo';

type LegacyDataNative = NativeModule & {
  readAsync(): Promise<string | null>;
  clearAsync(): Promise<void>;
};

/** Present only in Android builds; null on iOS and web (the old app was Android-only). */
export default requireOptionalNativeModule<LegacyDataNative>('LegacyData');
