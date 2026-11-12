// Converts the hand-made PNG artwork in assets/source/ into the files the app uses.
//
//   npm run assets          (runs this after the drawn assets)
//
// In-app images become WebP at 3x their on-screen size. Store icons and the native splash stay
// PNG because the stores and the splash plugin require PNG.
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

import sharp from 'sharp';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'assets');
const src = (p) => join(root, 'source', p);
const SCALE = 3;
/** Upscaling past this only adds file size; the source has no more detail to give. */
const MAX_UPSCALE = 3;

/**
 * [source, output, on-screen width in dp, on-screen height in dp]
 * Keep the dp sizes in step with the styles that show the image.
 */
const WEBP = [
  ['onboarding/jobs.png', 'images/onboarding/jobs.webp', 280, 280],
  ['splash/logo.png', 'images/splash/logo.webp', 190, 128],
  ['splash/laurel-left.png', 'images/splash/laurel-left.webp', 46, 82],
  ['splash/laurel-right.png', 'images/splash/laurel-right.webp', 42, 82],
  ['splash/government-building.png', 'images/splash/government-building.webp', 300, 174],
  ['splash/tricolor-wave.png', 'images/splash/tricolor-wave.webp', 390, 172],
];

const warnings = [];

/**
 * AI and design-tool exports often leave near-invisible pixels (alpha 1–15) around the art.
 * They show up as a grey box on light backgrounds and stop trim() from working, so clear them.
 */
const ALPHA_FLOOR = 16;
async function clean(file) {
  const { data, info } = await sharp(file).ensureAlpha().raw().toBuffer({ resolveWithObject: true });
  for (let i = 3; i < data.length; i += 4) if (data[i] < ALPHA_FLOOR) data[i - 3] = data[i - 2] = data[i - 1] = data[i] = 0;
  return sharp(data, { raw: info }).png();
}

/** Trims transparent borders and resizes to fit the box, never past MAX_UPSCALE. */
async function fitted(file, boxW, boxH) {
  const trimmed = await (await clean(file)).trim({ threshold: 1 }).toBuffer({ resolveWithObject: true });
  const { width, height } = trimmed.info;
  const fit = Math.min(boxW / width, boxH / height);
  const scale = Math.min(fit, MAX_UPSCALE);
  if (fit > 1.5 && !warnings.some((w) => w.startsWith(file.replace(root + '/', '')))) warnings.push(`${file.replace(root + '/', '')}: ${width}×${height} px, needs about ${Math.ceil(width * fit)}×${Math.ceil(height * fit)} px to stay sharp`);
  const w = Math.max(1, Math.round(width * scale));
  const h = Math.max(1, Math.round(height * scale));
  return sharp(trimmed.data).resize(w, h, { kernel: 'lanczos3' });
}

let total = 0;
for (const [from, to, dpW, dpH] of WEBP) {
  const out = join(root, to);
  await mkdir(dirname(out), { recursive: true });
  const img = await fitted(src(from), dpW * SCALE, dpH * SCALE);
  const buf = await img.webp({ quality: 88, alphaQuality: 100, effort: 6, smartSubsample: true }).toBuffer();
  await writeFile(out, buf);
  total += buf.length;
  const { width, height } = await sharp(buf).metadata();
  console.log(`${to.padEnd(44)} ${String(width).padStart(4)}×${String(height).padEnd(4)} ${(buf.length / 1024).toFixed(1)} KB`);
}

// App icon: full-bleed 1024 square. The source tile has rounded corners and a transparent rim,
// so it sits on a matching blue gradient; iOS and the stores apply their own corner mask.
{
  const size = 1024;
  const bg = Buffer.from(
    `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}"><defs><linearGradient id="g" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#147DF3"/><stop offset="1" stop-color="#002B86"/></linearGradient></defs><rect width="${size}" height="${size}" fill="url(#g)"/></svg>`,
  );
  const tile = await (await clean(src('app/app-icon.png'))).trim({ threshold: 1 }).toBuffer({ resolveWithObject: true });
  const scale = size / Math.min(tile.info.width, tile.info.height);
  // Overscan pushes the source's rounded corners and light rim out of the square.
  const w = Math.round(tile.info.width * scale * 1.14);
  const h = Math.round(tile.info.height * scale * 1.14);
  if (tile.info.width < size / 2) warnings.push(`source/app/app-icon.png: ${tile.info.width}×${tile.info.height} px, store icons need 1024×1024 px`);
  const art = await sharp(tile.data).resize(w, h, { kernel: 'lanczos3' }).extract({ left: Math.round((w - size) / 2), top: Math.round((h - size) / 2), width: size, height: size }).toBuffer();
  const buf = await sharp(bg).composite([{ input: art }]).flatten({ background: '#1D4ED8' }).png({ compressionLevel: 9 }).toBuffer();
  await writeFile(join(root, 'app/icon.png'), buf);
  console.log(`${'app/icon.png'.padEnd(44)} 1024×1024 ${(buf.length / 1024).toFixed(1)} KB`);
}

// Native splash logo: the dark wordmark needs a light card to be readable on the splash blue.
{
  const size = 400;
  const card = Buffer.from(
    `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}"><rect x="12" y="12" width="${size - 24}" height="${size - 24}" rx="88" fill="#FFFFFF"/></svg>`,
  );
  const logo = await (await fitted(src('splash/logo.png'), 300, 220)).png().toBuffer({ resolveWithObject: true });
  const buf = await sharp(card)
    .composite([{ input: logo.data, left: Math.round((size - logo.info.width) / 2), top: Math.round((size - logo.info.height) / 2) }])
    .png({ compressionLevel: 9 })
    .toBuffer();
  await writeFile(join(root, 'app/splash-icon.png'), buf);
  console.log(`${'app/splash-icon.png'.padEnd(44)} ${size}×${size} ${(buf.length / 1024).toFixed(1)} KB`);
}

console.log(`\n${WEBP.length} converted WebP images, ${(total / 1024).toFixed(0)} KB total`);
if (warnings.length) console.log(`\nLow-resolution sources (will look soft on 3x screens):\n  ${warnings.join('\n  ')}`);
