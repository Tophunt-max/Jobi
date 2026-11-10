// @ts-check
const { withAppBuildGradle } = require('expo/config-plugins');

/**
 * Signs Android release builds with the upload key from environment variables,
 * read by Gradle at build time (nothing secret is written into android/):
 *
 *   ANDROID_KEYSTORE_PATH      absolute path to the .jks
 *   ANDROID_KEYSTORE_PASSWORD
 *   ANDROID_KEY_ALIAS          default "upload"
 *   ANDROID_KEY_PASSWORD
 *
 * Without ANDROID_KEYSTORE_PATH, release builds fall back to the debug key so CI can
 * still compile them; such builds cannot be uploaded to Google Play.
 */
const MARKER = '// @generated withAndroidReleaseSigning';

const SIGNING_BLOCK = `
        ${MARKER}
        release {
            if (System.getenv('ANDROID_KEYSTORE_PATH')) {
                storeFile file(System.getenv('ANDROID_KEYSTORE_PATH'))
                storePassword System.getenv('ANDROID_KEYSTORE_PASSWORD')
                keyAlias System.getenv('ANDROID_KEY_ALIAS') ?: 'upload'
                keyPassword System.getenv('ANDROID_KEY_PASSWORD')
            }
        }`;

const RELEASE_SIGNING =
  "signingConfig System.getenv('ANDROID_KEYSTORE_PATH') ? signingConfigs.release : signingConfigs.debug";

function apply(gradle) {
  if (gradle.includes(MARKER)) return gradle;

  const signingStart = gradle.indexOf('signingConfigs {');
  if (signingStart === -1) throw new Error('withAndroidReleaseSigning: signingConfigs block not found');
  const insertAt = gradle.indexOf('\n', signingStart) + 1;
  let out = gradle.slice(0, insertAt) + SIGNING_BLOCK.slice(1) + '\n' + gradle.slice(insertAt);

  const releaseMatch = /(buildTypes\s*\{[\s\S]*?release\s*\{[\s\S]*?)signingConfig signingConfigs\.debug/.exec(out);
  if (!releaseMatch) throw new Error('withAndroidReleaseSigning: release signingConfig not found');
  out = out.replace(releaseMatch[0], releaseMatch[1] + RELEASE_SIGNING);
  return out;
}

module.exports = function withAndroidReleaseSigning(config) {
  return withAppBuildGradle(config, (cfg) => {
    if (cfg.modResults.language !== 'groovy') {
      throw new Error('withAndroidReleaseSigning: only Groovy build.gradle is supported');
    }
    cfg.modResults.contents = apply(cfg.modResults.contents);
    return cfg;
  });
};

module.exports.apply = apply;
