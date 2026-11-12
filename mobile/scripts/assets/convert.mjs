// Renders the brand artwork and converts hand-made PNGs (assets/source/) for the app.
//
//   npm run assets          (runs this after the drawn assets)
//
// In-app images become WebP at 3x their on-screen size. Store icons and the native splash stay
// PNG because the stores and the splash plugin require PNG.
//
// The splash pieces and app icon were uploaded at 42–175 px (kept in assets/source/reference/).
// They are redrawn as vectors in brand-art.mjs so they stay sharp at every size.
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

import sharp from 'sharp';

import * as brand from './brand-art.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'assets');
const SCALE = 3;

const warnings = [];
let total = 0;

async function write(rel, buf) {
  const out = join(root, rel);
  await mkdir(dirname(out), { recursive: true });
  await writeFile(out, buf);
  const { width, height } = await sharp(buf).metadata();
  console.log(`${rel.padEnd(44)} ${String(width).padStart(4)}×${String(height).padEnd(4)} ${(buf.length / 1024).toFixed(1)} KB`);
  return buf.length;
}

const webp = (img) => img.webp({ quality: 90, alphaQuality: 100, effort: 6, smartSubsample: true }).toBuffer();

// ---- Vector brand artwork -------------------------------------------------------------
/** [output, svg]; each svg carries its on-screen size in dp. */
const VECTOR = [
  ['images/splash/logo.webp', brand.logo()],
  ['images/splash/laurel-left.webp', brand.laurel('left')],
  ['images/splash/laurel-right.webp', brand.laurel('right')],
  ['images/splash/government-building.webp', brand.governmentBuilding()],
  ['images/splash/tricolor-wave.webp', brand.tricolorWave()],
  ['images/brand/logo-mark.webp', brand.roundedIcon(64)],
];
for (const [rel, svgText] of VECTOR) {
  total += await write(rel, await webp(sharp(Buffer.from(svgText), { density: 72 * SCALE })));
}
await write('app/icon.png', await sharp(Buffer.from(brand.appIcon())).flatten({ background: '#0B5FD8' }).png({ compressionLevel: 9 }).toBuffer());
for (const [rel, svgText, size] of [
  ['app/android-icon-background.png', brand.adaptiveBackground(), 432],
  ['app/android-icon-foreground.png', brand.adaptiveForeground(), 432],
  ['app/android-icon-monochrome.png', brand.adaptiveMonochrome(), 432],
  ['app/favicon.png', brand.roundedIcon(48), 48],
]) {
  const density = 72 * (size / (size === 48 ? 48 : 432));
  await write(rel, await sharp(Buffer.from(svgText), { density }).resize(size, size).png({ compressionLevel: 9 }).toBuffer());
}
await write('app/splash-icon.png', await sharp(Buffer.from(brand.splashIcon()), { density: 72 * 2 }).resize(800, 800).png({ compressionLevel: 9 }).toBuffer());

// ---- Hand-made PNGs -------------------------------------------------------------------
/** AI and design-tool exports often leave near-invisible pixels (alpha < 16); they show as a grey box. */
const ALPHA_FLOOR = 16;
async function clean(file) {
  const { data, info } = await sharp(file).ensureAlpha().raw().toBuffer({ resolveWithObject: true });
  for (let i = 3; i < data.length; i += 4) if (data[i] < ALPHA_FLOOR) data[i - 3] = data[i - 2] = data[i - 1] = data[i] = 0;
  return sharp(data, { raw: info }).png();
}

/** [source in assets/source/, output, on-screen width in dp, on-screen height in dp] */
const PNG = [['onboarding/jobs.png', 'images/onboarding/jobs.webp', 280, 280]];
for (const [from, to, dpW, dpH] of PNG) {
  const file = join(root, 'source', from);
  const trimmed = await (await clean(file)).trim({ threshold: 1 }).toBuffer({ resolveWithObject: true });
  const { width, height } = trimmed.info;
  const fit = (Math.min((dpW * SCALE) / width, (dpH * SCALE) / height));
  if (fit > 1) warnings.push(`source/${from}: ${width}×${height} px, export at least ${Math.ceil(width * fit)}×${Math.ceil(height * fit)} px`);
  const scale = Math.min(fit, 1);
  total += await write(to, await webp(sharp(trimmed.data).resize(Math.round(width * scale), Math.round(height * scale), { kernel: 'lanczos3' })));
}

console.log(`\n${VECTOR.length + PNG.length} brand/converted WebP images, ${(total / 1024).toFixed(0)} KB total`);
if (warnings.length) console.log(`\nSources smaller than 3x their on-screen size (will look soft):\n  ${warnings.join('\n  ')}`);
