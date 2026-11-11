// Multi-colour category illustrations (64x64) and the circular emblems built from them.
//
// Style rules, so the set reads as one family:
// - Artwork sits inside x/y 6..58 and is visually centred.
// - Each shape uses a top-to-bottom gradient of one hue (light -> dark), plus a soft white
//   highlight on the upper-left. No outlines.
// - At most one accent colour per icon (gold/saffron), used sparingly.
import { C, arcPoints, linear, svg } from './shapes.mjs';

const v = (id, top, bottom) =>
  linear(id, [
    [0, top],
    [1, bottom],
  ]);
const d = (id, from, to) =>
  linear(id, [
    [0, from],
    [1, to],
  ], { x1: 0, y1: 0, x2: 1, y2: 1 });

/** Small tricolour flag on a pole; the pole foot sits at (x, y). */
function miniFlag(x, y) {
  // 10 units tall, so keep y >= 12 to stay inside the 64x64 box.
  return `
    <rect x="${x - 0.7}" y="${y - 10}" width="1.4" height="10" rx=".7" fill="#64748B"/>
    <rect x="${x + 0.7}" y="${y - 9.6}" width="9" height="6.3" rx=".6" fill="#E2E8F0"/>
    <path d="M${x + 0.7} ${y - 9.6}h9v2.1h-9z" fill="${C.saffron}"/>
    <path d="M${x + 0.7} ${y - 7.5}h9v2.1h-9z" fill="#FFFFFF"/>
    <path d="M${x + 0.7} ${y - 5.4}h9v2.1h-9z" fill="${C.green}"/>
    <circle cx="${x + 5.2}" cy="${y - 6.45}" r=".75" fill="#1E3A8A"/>`;
}

/**
 * Each entry returns { defs, body }. Gradient ids are prefixed with the key so an icon can be
 * embedded inside another SVG (the emblem) without clashes.
 */
export const CATEGORY_ART = {
  'central-govt': () => ({
    defs: v('cg-a', '#FBBF24', '#D97706') + v('cg-b', '#FFFBEB', '#FDE68A') + v('cg-c', '#B45309', '#92400E'),
    body: `
      ${miniFlag(32, 14)}
      <path d="M21 26c0-7 5-12.5 11-12.5S43 19 43 26z" fill="url(#cg-a)"/>
      <path d="M24.5 22c1.4-3.6 4.2-6 7.5-6.4" stroke="#fff" stroke-opacity=".55" stroke-width="2" stroke-linecap="round" fill="none"/>
      <rect x="18" y="25.5" width="28" height="3.5" rx="1.5" fill="url(#cg-c)"/>
      <path d="M8 36 32 29l24 7z" fill="url(#cg-a)"/>
      <rect x="9" y="35.5" width="46" height="3.5" rx="1.5" fill="url(#cg-c)"/>
      <rect x="11" y="39" width="42" height="13" fill="url(#cg-b)"/>
      ${[15, 22, 29, 35, 42, 49].map((x) => `<rect x="${x - 1.6}" y="40" width="3.2" height="11" rx="1.2" fill="url(#cg-a)"/>`).join('')}
      <rect x="7" y="52" width="50" height="3.5" rx="1.5" fill="url(#cg-c)"/>
      <rect x="4" y="55.5" width="56" height="3" rx="1.5" fill="url(#cg-a)"/>`,
  }),

  'state-govt': () => ({
    defs: v('sg-a', '#4ADE80', '#15803D') + v('sg-b', '#F0FDF4', '#BBF7D0') + v('sg-c', '#166534', '#14532D') + v('sg-p', '#F87171', '#DC2626'),
    body: `
      <rect x="10" y="30" width="44" height="3.5" rx="1.5" fill="url(#sg-c)"/>
      <path d="M8 31 32 22l24 9z" fill="url(#sg-a)"/>
      <rect x="12" y="33.5" width="40" height="18" fill="url(#sg-b)"/>
      ${[17, 24.5, 32, 39.5, 47].map((x) => `<rect x="${x - 1.7}" y="35" width="3.4" height="15" rx="1.2" fill="url(#sg-a)"/>`).join('')}
      <rect x="8" y="51.5" width="48" height="3.5" rx="1.5" fill="url(#sg-c)"/>
      <rect x="5" y="55" width="54" height="3" rx="1.5" fill="url(#sg-a)"/>
      <path d="M44 5.5a8.5 8.5 0 0 1 8.5 8.5c0 6.2-8.5 13.5-8.5 13.5S35.5 20.2 35.5 14A8.5 8.5 0 0 1 44 5.5z" fill="url(#sg-p)"/>
      <circle cx="44" cy="14" r="3.3" fill="#fff"/>`,
  }),

  railway: () => ({
    defs: v('rw-a', '#F87171', '#B91C1C') + v('rw-b', '#E0F2FE', '#93C5FD') + v('rw-c', '#475569', '#1E293B'),
    body: `
      <path d="M20 47 13 57M44 47l7 10" stroke="url(#rw-c)" stroke-width="3.2" stroke-linecap="round"/>
      <rect x="10" y="55" width="44" height="3.2" rx="1.6" fill="url(#rw-c)"/>
      <rect x="17" y="50.5" width="30" height="2.6" rx="1.3" fill="#94A3B8"/>
      <path d="M22 6h20c6.6 0 10 3.6 10 10v24c0 5-3.6 8-8 8H20c-4.4 0-8-3-8-8V16c0-6.4 3.4-10 10-10z" fill="url(#rw-a)"/>
      <path d="M16 13c1-3.6 3.6-5 7-5h8" stroke="#fff" stroke-opacity=".45" stroke-width="2" stroke-linecap="round" fill="none"/>
      <rect x="18" y="14" width="28" height="15" rx="4" fill="url(#rw-b)"/>
      <path d="M18.5 25 38 14.5h5L19 28z" fill="#fff" opacity=".55"/>
      <rect x="27" y="9" width="10" height="2.4" rx="1.2" fill="#FECACA"/>
      <rect x="12" y="33" width="40" height="3" fill="#991B1B" opacity=".35"/>
      <circle cx="20.5" cy="41" r="3.2" fill="#FEF9C3"/><circle cx="43.5" cy="41" r="3.2" fill="#FEF9C3"/>
      <rect x="28" y="39.5" width="8" height="3" rx="1.5" fill="#7F1D1D" opacity=".45"/>`,
  }),

  banking: () => ({
    defs: v('bk-a', '#60A5FA', '#1D4ED8') + v('bk-b', '#EFF6FF', '#BFDBFE') + v('bk-c', '#1E40AF', '#1E3A8A') + d('bk-g', '#FDE68A', '#F59E0B'),
    body: `
      <path d="M32 5 58 18.5H6z" fill="url(#bk-a)"/>
      <circle cx="32" cy="13.5" r="2.8" fill="#fff" opacity=".9"/>
      <rect x="6" y="18.5" width="52" height="4" rx="1" fill="url(#bk-c)"/>
      <rect x="9" y="22.5" width="46" height="21" fill="url(#bk-b)"/>
      ${[14, 23, 32, 41, 50].map((x) => `<rect x="${x - 2.4}" y="23.5" width="4.8" height="19" rx="1.4" fill="url(#bk-a)"/>`).join('')}
      <rect x="6" y="43.5" width="52" height="4" rx="1" fill="url(#bk-c)"/>
      <rect x="3" y="47.5" width="58" height="4" rx="2" fill="url(#bk-a)"/>
      <circle cx="48" cy="48" r="10.5" fill="url(#bk-g)" stroke="#fff" stroke-width="2.2"/>
      <path d="M44 43.6h8M44 46.6h8M45.2 43.6c3.8 0 4.3 6.1 0 6.1l5 4.8" stroke="#92400E" stroke-width="1.7" fill="none" stroke-linecap="round" stroke-linejoin="round"/>`,
  }),

  defence: () => ({
    defs: v('df-a', '#84CC16', '#3F6212') + v('df-b', '#A3E635', '#4D7C0F') + d('df-g', '#FDE68A', '#F59E0B'),
    body: `
      <path d="M32 5 52 12v15.5C52 41.5 43.5 52 32 59 20.5 52 12 41.5 12 27.5V12z" fill="url(#df-a)"/>
      <path d="M32 5v54C20.5 52 12 41.5 12 27.5V12z" fill="url(#df-b)"/>
      <path d="M16 15.5 32 10l6 2" stroke="#fff" stroke-opacity=".45" stroke-width="2" stroke-linecap="round" fill="none"/>
      <path d="M32 18.5l3.7 7.5 8.3 1.2-6 5.8 1.4 8.2L32 37.4l-7.4 3.8 1.4-8.2-6-5.8 8.3-1.2z" fill="url(#df-g)"/>
      <path d="M22 46.5 32 51l10-4.5M24.5 51.5 32 55l7.5-3.5" stroke="url(#df-g)" stroke-width="2.4" fill="none" stroke-linecap="round" stroke-linejoin="round"/>`,
  }),

  police: () => ({
    defs: v('pl-a', '#3B82F6', '#1E3A8A') + v('pl-b', '#1E40AF', '#172554') + d('pl-g', '#FDE68A', '#F59E0B'),
    body: `
      <path d="M32 4c6 4.5 12.5 6.5 20 6.5V28c0 15-9 25.5-20 31-11-5.5-20-16-20-31V10.5C19.5 10.5 26 8.5 32 4z" fill="url(#pl-a)"/>
      <path d="M32 9.5c4.6 3.2 9.8 4.8 15 5.2v13.5c0 11.6-6.6 20-15 24.8-8.4-4.8-15-13.2-15-24.8V14.7c5.2-.4 10.4-2 15-5.2z" fill="url(#pl-b)"/>
      <path d="M15.5 12.5c5.5-.4 10.5-2.3 16.5-6.5" stroke="#fff" stroke-opacity=".45" stroke-width="2" stroke-linecap="round" fill="none"/>
      <circle cx="32" cy="30" r="12" fill="url(#pl-g)"/>
      <circle cx="32" cy="30" r="9" fill="#1E3A8A"/>
      <path d="M32 22.6l2.3 4.7 5.2.8-3.8 3.6.9 5.2-4.6-2.4-4.6 2.4.9-5.2-3.8-3.6 5.2-.8z" fill="url(#pl-g)"/>
      <rect x="24" y="45" width="16" height="3.2" rx="1.6" fill="url(#pl-g)"/>`,
  }),

  teaching: () => ({
    defs: v('tc-a', '#10B981', '#065F46') + v('tc-b', '#B45309', '#78350F') + v('tc-r', '#F87171', '#DC2626'),
    body: `
      <path d="M20 44 14 58M44 44l6 14M32 44v12" stroke="url(#tc-b)" stroke-width="3.2" stroke-linecap="round"/>
      <rect x="6" y="8" width="52" height="38" rx="5" fill="url(#tc-b)"/>
      <rect x="9.5" y="11.5" width="45" height="31" rx="3" fill="url(#tc-a)"/>
      <path d="M15 32 19.5 21.5 24 32M16.7 28h5.6" stroke="#ECFDF5" stroke-width="2.2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M29 26.5h6M32 23.5v6" stroke="#ECFDF5" stroke-width="2.2" stroke-linecap="round"/>
      <path d="M14 37h20" stroke="#A7F3D0" stroke-width="2" stroke-linecap="round" opacity=".8"/>
      <path d="M45.5 21.5c3.6-1.6 6.5.6 6.5 4.6 0 4.4-3 8.4-5.4 8.4-1 0-1.6-.6-2.6-.6s-1.6.6-2.6.6c-2.4 0-5.4-4-5.4-8.4 0-4 2.9-6.2 6.5-4.6z" fill="url(#tc-r)"/>
      <path d="M45.5 21.5c0-2 1-3.4 2.6-4" stroke="#78350F" stroke-width="1.6" stroke-linecap="round" fill="none"/>
      <path d="M41 25c.4-1.2 1.2-1.8 2.2-2" stroke="#fff" stroke-opacity=".6" stroke-width="1.4" stroke-linecap="round" fill="none"/>`,
  }),

  medical: () => ({
    defs: v('md-a', '#F472B6', '#DB2777') + v('md-b', '#F9A8D4', '#EC4899'),
    body: `
      <path d="M32 57C14.5 45.5 5.5 35.5 5.5 23.5 5.5 14.5 12.5 8 21 8c4.9 0 8.6 2.4 11 6.2C34.4 10.4 38.1 8 43 8c8.5 0 15.5 6.5 15.5 15.5C58.5 35.5 49.5 45.5 32 57z" fill="url(#md-a)"/>
      <path d="M32 57C14.5 45.5 5.5 35.5 5.5 23.5 5.5 14.5 12.5 8 21 8c4.9 0 8.6 2.4 11 6.2z" fill="url(#md-b)" opacity=".75"/>
      <path d="M11 20c.8-4.2 4.4-7 8.6-7" stroke="#fff" stroke-opacity=".6" stroke-width="2.2" stroke-linecap="round" fill="none"/>
      <rect x="27" y="19.5" width="10" height="25" rx="2.6" fill="#fff"/>
      <rect x="19.5" y="27" width="25" height="10" rx="2.6" fill="#fff"/>`,
  }),

  psu: () => ({
    defs: v('ps-a', '#2DD4BF', '#0F766E') + v('ps-b', '#5EEAD4', '#14B8A6') + v('ps-c', '#94A3B8', '#475569'),
    body: `
      <circle cx="15" cy="9" r="3.2" fill="#CBD5E1"/><circle cx="20" cy="7" r="2.4" fill="#E2E8F0"/>
      <rect x="11" y="13" width="7.5" height="20" rx="1.5" fill="url(#ps-c)"/>
      <rect x="22" y="19" width="7" height="14" rx="1.5" fill="url(#ps-c)"/>
      <rect x="11" y="16" width="7.5" height="2.4" fill="${C.red500}"/>
      <path d="M6 58V33l13 7.5V33l13 7.5V33l13 7.5V21h13v37z" fill="url(#ps-a)"/>
      <path d="M6 58V33l13 7.5V33l13 7.5V33l13 7.5V58z" fill="url(#ps-b)"/>
      ${[11.5, 24.5, 37.5].map((x) => `<rect x="${x}" y="46" width="7" height="6" rx="1.2" fill="#F0FDFA"/>`).join('')}
      <rect x="49" y="26" width="5" height="5" rx="1" fill="#F0FDFA"/><rect x="49" y="35" width="5" height="5" rx="1" fill="#F0FDFA" opacity=".8"/>`,
  }),

  other: () => ({
    defs: v('ot-a', '#A78BFA', '#6D28D9') + v('ot-b', '#7C3AED', '#5B21B6'),
    body: `
      <path d="M24 16v-3.5a5 5 0 0 1 5-5h6a5 5 0 0 1 5 5V16" stroke="url(#ot-b)" stroke-width="4.5" fill="none" stroke-linecap="round"/>
      <rect x="6" y="16" width="52" height="38" rx="8" fill="url(#ot-a)"/>
      <path d="M6 31.5q26 9 52 0" stroke="#fff" stroke-opacity=".3" stroke-width="3" fill="none"/>
      <path d="M11 22c.8-2 2.4-3 4.6-3H30" stroke="#fff" stroke-opacity=".45" stroke-width="2" stroke-linecap="round" fill="none"/>
      <rect x="27" y="28.5" width="10" height="9" rx="2.6" fill="#fff"/>
      <circle cx="22" cy="46" r="2.3" fill="#EDE9FE"/><circle cx="32" cy="46" r="2.3" fill="#EDE9FE"/><circle cx="42" cy="46" r="2.3" fill="#EDE9FE"/>`,
  }),

  ssc: () => ({
    defs: v('sc-a', '#60A5FA', '#1D4ED8') + v('sc-b', '#FBBF24', '#D97706'),
    body: `
      <path d="M14 6h24l12 12v37a3.5 3.5 0 0 1-3.5 3.5H14a3.5 3.5 0 0 1-3.5-3.5V9.5A3.5 3.5 0 0 1 14 6z" fill="url(#sc-a)"/>
      <path d="M38 6v8.5a3.5 3.5 0 0 0 3.5 3.5H50z" fill="#BFDBFE"/>
      <path d="M15 10.5c.4-.8 1-1 1.8-1h12" stroke="#fff" stroke-opacity=".5" stroke-width="2" stroke-linecap="round" fill="none"/>
      ${[24, 33, 42].map((y, i) => `<circle cx="18.5" cy="${y}" r="2.6" fill="${i < 2 ? '#fff' : 'none'}" stroke="#fff" stroke-width="1.6"/><rect x="24" y="${y - 1.4}" width="${i === 1 ? 12 : 17}" height="2.8" rx="1.4" fill="#fff" opacity=".9"/>`).join('')}
      <path d="M40 50 52.5 37.5a3 3 0 0 1 4.2 0l.8.8a3 3 0 0 1 0 4.2L45 55l-6.4 1.8z" fill="url(#sc-b)"/>
      <path d="M50.5 39.5l5 5" stroke="#92400E" stroke-width="1.6"/>
      <path d="M38.6 56.8 40 50l5 5z" fill="#FDE68A"/>`,
  }),

  upsc: () => ({
    defs: v('up-a', '#818CF8', '#4338CA') + v('up-b', '#EEF2FF', '#C7D2FE') + v('up-c', '#3730A3', '#312E81'),
    body: `
      ${miniFlag(32, 13)}
      <path d="M6 22 32 11l26 11z" fill="url(#up-a)"/>
      <path d="M16 19.5 32 13l9 3.8" stroke="#fff" stroke-opacity=".45" stroke-width="1.8" stroke-linecap="round" fill="none"/>
      <rect x="6" y="22" width="52" height="4" rx="1" fill="url(#up-c)"/>
      <rect x="10" y="26" width="44" height="22" fill="url(#up-b)"/>
      ${[15, 23.5, 32, 40.5, 49].map((x) => `<rect x="${x - 2.2}" y="27" width="4.4" height="20" rx="1.4" fill="url(#up-a)"/>`).join('')}
      <rect x="7" y="48" width="50" height="4" rx="1.5" fill="url(#up-c)"/>
      <rect x="4" y="52" width="56" height="3.5" rx="1.5" fill="url(#up-a)"/>
      <rect x="2" y="55.5" width="60" height="3" rx="1.5" fill="url(#up-c)"/>`,
  }),

  engineering: () => ({
    defs: v('en-a', '#38BDF8', '#0369A1') + v('en-b', '#FB923C', '#C2410C'),
    body: `
      <g transform="translate(28 29)">
        ${Array.from({ length: 8 }, (_, i) => `<rect x="-4.5" y="-24" width="9" height="10" rx="2.2" fill="url(#en-a)" transform="rotate(${i * 45})"/>`).join('')}
        <circle r="17" fill="url(#en-a)"/>
        <circle r="7" fill="#fff"/>
        <path d="M-12 -6a13 13 0 0 1 9-8" stroke="#fff" stroke-opacity=".5" stroke-width="2.2" stroke-linecap="round" fill="none"/>
      </g>
      <path d="M57.5 37.5a8 8 0 0 1-10.8 9.6L36 57.8a3.4 3.4 0 0 1-4.8-4.8L42 42.3a8 8 0 0 1 9.6-10.8l-4.9 4.9.9 4.2 4.2.9z" fill="url(#en-b)" stroke="#fff" stroke-width="1.6" stroke-linejoin="round"/>`,
  }),
};

export const CATEGORY_TONES = {
  'central-govt': { ring: C.amber600, soft: '#FFFBEB' },
  'state-govt': { ring: C.green600, soft: '#F0FDF4' },
  railway: { ring: C.red600, soft: '#FEF2F2' },
  banking: { ring: C.blue600, soft: '#EFF6FF' },
  defence: { ring: '#4D7C0F', soft: '#F7FEE7' },
  police: { ring: C.blue800, soft: '#EEF2FF' },
  teaching: { ring: '#047857', soft: '#ECFDF5' },
  medical: { ring: '#DB2777', soft: '#FDF2F8' },
  psu: { ring: C.teal600, soft: '#F0FDFA' },
  other: { ring: C.purple600, soft: '#F5F3FF' },
  ssc: { ring: C.blue600, soft: '#EFF6FF' },
  upsc: { ring: C.indigo600, soft: '#EEF2FF' },
  engineering: { ring: '#0369A1', soft: '#F0F9FF' },
};

export function categoryIcon(key) {
  const { defs, body } = CATEGORY_ART[key]();
  return svg(64, 64, body, defs);
}

/**
 * Circular seal: thin gold rim, coloured band with fine ticks, white face and the category
 * art in the centre. Generic artwork, not an official logo.
 */
export function emblem(key) {
  const { ring } = CATEGORY_TONES[key];
  const art = CATEGORY_ART[key]();
  const ticks = arcPoints(40, 40, 33.6, 0, 355, 36)
    .map((p) => `<circle cx="${p.x.toFixed(2)}" cy="${p.y.toFixed(2)}" r=".85" fill="#fff" opacity=".7"/>`)
    .join('');
  const defs =
    art.defs +
    linear('em-ring', [
      [0, ring, 0.85],
      [1, ring],
    ]) +
    d('em-gold', '#FEF3C7', '#F59E0B') +
    linear('em-face', [
      [0, '#FFFFFF'],
      [1, '#F8FAFC'],
    ]) +
    `<filter id="em-s" x="-20%" y="-20%" width="140%" height="140%"><feDropShadow dx="0" dy="1" stdDeviation="1" flood-color="#0F172A" flood-opacity=".18"/></filter>`;
  const body = `
    <circle cx="40" cy="40" r="38.5" fill="url(#em-gold)" filter="url(#em-s)"/>
    <circle cx="40" cy="40" r="36.6" fill="url(#em-ring)"/>
    ${ticks}
    <circle cx="40" cy="40" r="30.4" fill="url(#em-gold)"/>
    <circle cx="40" cy="40" r="29" fill="url(#em-face)"/>
    <g transform="translate(19 19) scale(0.65625)">${art.body}</g>`;
  return svg(80, 80, body, defs);
}
