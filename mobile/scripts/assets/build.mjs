#!/usr/bin/env node
// Renders every RozgarOne image from code to WebP (and the store icons to PNG).
//
//   npm run assets
//
// Output: mobile/assets/images/<group>/<name>.webp at 3x, plus mobile/assets/app/*.png.
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

import sharp from 'sharp';

import { categoryIcon, emblem } from './categories.mjs';
import * as art from './illustrations.mjs';
import { admitCardIcon, LIST_TONES, resultIcon, syllabusIcon } from './list-icons.mjs';
import { C, circleIcon, tile, tintable } from './shapes.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'assets');
const SCALE = 3;

/** [output path relative to assets/, svg string, options] */
const images = [
  // brand
  ['images/brand/logo-tile.webp', art.logoTile()],
  ['images/brand/logo-mark.webp', art.logoMark()],
  ['images/brand/laurel.webp', art.laurel()],
  // splash + onboarding
  ['images/splash/background.webp', art.splashBackground(), { scale: 2, quality: 80 }],
  ['images/onboarding/jobs.webp', art.onboardingJobs()],
  ['images/onboarding/eligibility.webp', art.onboardingEligibility()],
  ['images/onboarding/alerts.webp', art.onboardingAlerts()],
  // home
  ['images/home/header.webp', art.homeHeader(), { scale: 2, quality: 80 }],
  ['images/home/hero.webp', art.heroBanner()],
  // quick links
  ['images/quick/latest-jobs.webp', tile('lj', C.blue500, C.blue700, 'briefcase')],
  ['images/quick/results.webp', tile('rs', C.purple500, '#6D28D9', 'results')],
  ['images/quick/admit-cards.webp', tile('ac', C.orange500, C.saffronDark, 'idCard')],
  ['images/quick/answer-key.webp', tile('ak', C.sky500, '#0369A1', 'answerKey')],
  ['images/quick/syllabus.webp', tile('sy', C.blue500, C.indigo600, 'book')],
  ['images/quick/exam-calendar.webp', tile('ec', C.red500, C.rose600, 'calendar')],
  ['images/quick/eligibility.webp', tile('el', C.green500, C.green600, 'checkCircle')],
  ['images/quick/admissions.webp', tile('ad', C.teal500, C.teal600, 'cap')],
  // content list icons (tinted in the app)
  ['images/content/result.webp', tintable('trophy')],
  ['images/content/admit-card.webp', tintable('idCard')],
  ['images/content/pdf.webp', tintable('pdf')],
  ['images/content/answer-key.webp', tintable('answerKey')],
  ['images/content/admission.webp', tintable('cap')],
  ['images/content/calendar.webp', tintable('calendar')],
  ['images/content/download.webp', tintable('download')],
  // notifications
  ['images/notifications/new-job.webp', circleIcon('#FFEDD5', C.orange500, 'bell')],
  ['images/notifications/admit-card.webp', circleIcon(C.blue100, C.blue600, 'checkCircle')],
  ['images/notifications/result.webp', circleIcon(C.red100, C.red600, 'megaphone')],
  ['images/notifications/deadline.webp', circleIcon(C.red100, C.rose600, 'calendarAlert')],
  ['images/notifications/general.webp', circleIcon(C.purple100, C.purple600, 'sparkle')],
  // ai, eligibility, profile, states
  ['images/ai/robot.webp', art.robot()],
  ['images/eligibility/checklist.webp', art.checklist()],
  ['images/profile/avatar.webp', art.avatar()],
  ['images/states/empty.webp', art.emptyState()],
];

const CATEGORY_KEYS = [
  'central-govt',
  'state-govt',
  'railway',
  'banking',
  'defence',
  'police',
  'teaching',
  'medical',
  'psu',
  'other',
  'ssc',
  'upsc',
  'engineering',
];
for (const key of CATEGORY_KEYS) {
  images.push([`images/categories/${key}.webp`, categoryIcon(key)]);
  images.push([`images/emblems/${key}.webp`, emblem(key)]);
}

// Full-colour list icons, one per tone: images/lists/<kind>/<tone>.webp
for (const tone of LIST_TONES) {
  images.push([`images/lists/results/${tone[0]}.webp`, resultIcon(tone)]);
  images.push([`images/lists/admit-cards/${tone[0]}.webp`, admitCardIcon(tone)]);
  images.push([`images/lists/syllabus/${tone[0]}.webp`, syllabusIcon(tone)]);
}

const pngs = [
  ['app/icon.png', art.appIcon(), 1],
  ['app/android-icon-foreground.png', art.adaptiveForeground(), 1],
  ['app/android-icon-background.png', art.adaptiveBackground(), 1],
  ['app/android-icon-monochrome.png', art.adaptiveMonochrome(), 1],
  ['app/splash-icon.png', art.splashIcon(), 2.56],
  ['app/favicon.png', art.favicon(), 1],
];

async function render(svgText, scale) {
  return sharp(Buffer.from(svgText), { density: 72 * scale });
}

let total = 0;
for (const [rel, svgText, opts = {}] of images) {
  const out = join(root, rel);
  await mkdir(dirname(out), { recursive: true });
  const img = await render(svgText, opts.scale ?? SCALE);
  const buf = await img.webp({ quality: opts.quality ?? 90, alphaQuality: 100, effort: 6 }).toBuffer();
  await writeFile(out, buf);
  total += buf.length;
  console.log(`${rel.padEnd(44)} ${(buf.length / 1024).toFixed(1)} KB`);
}
for (const [rel, svgText, scale] of pngs) {
  const out = join(root, rel);
  await mkdir(dirname(out), { recursive: true });
  const img = await render(svgText, scale);
  const buf = await img.png({ compressionLevel: 9 }).toBuffer();
  await writeFile(out, buf);
  console.log(`${rel.padEnd(44)} ${(buf.length / 1024).toFixed(1)} KB`);
}
console.log(`\n${images.length} WebP images, ${(total / 1024).toFixed(0)} KB total`);
