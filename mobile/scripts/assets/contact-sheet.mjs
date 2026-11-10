#!/usr/bin/env node
// Builds a single preview image of every WebP asset: node scripts/assets/contact-sheet.mjs <out.png> [filter]
import { readdir } from 'node:fs/promises';
import { dirname, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

import sharp from 'sharp';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'assets', 'images');
const [out = 'contact-sheet.png', filter = ''] = process.argv.slice(2);

async function walk(dir) {
  const entries = await readdir(dir, { withFileTypes: true });
  const files = await Promise.all(entries.map((e) => (e.isDirectory() ? walk(join(dir, e.name)) : [join(dir, e.name)])));
  return files.flat();
}

const files = (await walk(root)).filter((f) => f.endsWith('.webp') && relative(root, f).includes(filter)).sort();
const cell = 220;
const cols = 6;
const rows = Math.ceil(files.length / cols);
const composites = [];
for (const [i, f] of files.entries()) {
  const img = await sharp(f).resize(cell - 20, cell - 20, { fit: 'contain', background: { r: 0, g: 0, b: 0, alpha: 0 } }).png().toBuffer();
  composites.push({ input: img, left: (i % cols) * cell + 10, top: Math.floor(i / cols) * cell + 10 });
}
await sharp({ create: { width: cols * cell, height: rows * cell, channels: 4, background: '#E5E7EB' } })
  .composite(composites)
  .png()
  .toFile(out);
console.log(files.map((f, i) => `${i}: ${relative(root, f)}`).join('\n'));
