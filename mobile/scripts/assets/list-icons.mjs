// Full-colour list icons for the Results, Admit Cards and Syllabus screens.
// Each kind comes in the five list tones (same order as LIST_TINTS in the app), so rows
// alternate red, green, purple, blue and orange like the mockup.
import { linear, shadow, svg } from './shapes.mjs';

/** [name, soft background, light fill, dark fill] in the LIST_TINTS order. */
export const LIST_TONES = [
  ['red', '#FEE2E2', '#F87171', '#DC2626'],
  ['green', '#DCFCE7', '#4ADE80', '#16A34A'],
  ['purple', '#EDE9FE', '#A78BFA', '#7C3AED'],
  ['blue', '#DBEAFE', '#60A5FA', '#2563EB'],
  ['orange', '#FFEDD5', '#FB923C', '#EA580C'],
];

const SIZE = 56;

function frame(id, soft, body, defs) {
  return svg(SIZE, SIZE, `<rect width="${SIZE}" height="${SIZE}" rx="15" fill="${soft}"/>${body}`, defs);
}

/** Result: certificate sheet with a rosette medal. */
export function resultIcon([name, soft, light, dark]) {
  const id = `rs-${name}`;
  const defs =
    linear(`${id}g`, [
      [0, light],
      [1, dark],
    ]) + shadow(`${id}s`, { dy: 1.2, blur: 1.2, opacity: 0.25, color: dark });
  const body = `
    <g filter="url(#${id}s)">
      <rect x="12" y="10" width="26" height="33" rx="4" fill="#FFFFFF"/>
      <rect x="12" y="10" width="26" height="7" rx="3" fill="url(#${id}g)"/>
      <rect x="12" y="14" width="26" height="3" fill="url(#${id}g)"/>
    </g>
    <rect x="16.5" y="21.5" width="15" height="2.6" rx="1.3" fill="${light}" opacity=".7"/>
    <rect x="16.5" y="27" width="10" height="2.6" rx="1.3" fill="${light}" opacity=".5"/>
    <path d="M33 37l-3.2 9 3.6-1.6 2.4 3 1.6-8.6z" fill="${dark}"/>
    <path d="M41 37l3.2 9-3.6-1.6-2.4 3-1.6-8.6z" fill="${dark}"/>
    <circle cx="37" cy="35" r="7.6" fill="url(#${id}g)" stroke="#FFFFFF" stroke-width="1.6"/>
    <path d="M37 31.2l1.15 2.35 2.6.38-1.88 1.83.44 2.58L37 37.12l-2.31 1.22.44-2.58-1.88-1.83 2.6-.38z" fill="#FFFFFF"/>`;
  return frame(id, soft, body, defs);
}

/** Admit card: ID card with photo, lines and a tick badge. */
export function admitCardIcon([name, soft, light, dark]) {
  const id = `ac-${name}`;
  const defs =
    linear(`${id}g`, [
      [0, light],
      [1, dark],
    ], { x1: 0, y1: 0, x2: 1, y2: 1 }) + shadow(`${id}s`, { dy: 1.4, blur: 1.4, opacity: 0.28, color: dark });
  const body = `
    <rect x="9" y="15" width="38" height="27" rx="5" fill="url(#${id}g)" filter="url(#${id}s)"/>
    <rect x="23" y="11" width="10" height="7" rx="2.5" fill="${dark}"/>
    <rect x="25.5" y="13" width="5" height="2" rx="1" fill="#FFFFFF" opacity=".8"/>
    <rect x="13" y="21" width="12" height="15" rx="3" fill="#FFFFFF"/>
    <circle cx="19" cy="26.3" r="2.8" fill="${light}"/>
    <path d="M14.6 34.5c.8-2.6 2.4-3.9 4.4-3.9s3.6 1.3 4.4 3.9" fill="${light}"/>
    <rect x="28" y="23" width="14" height="2.8" rx="1.4" fill="#FFFFFF"/>
    <rect x="28" y="28.5" width="10" height="2.8" rx="1.4" fill="#FFFFFF" opacity=".75"/>
    <circle cx="44" cy="40" r="6" fill="#FFFFFF"/>
    <circle cx="44" cy="40" r="4.6" fill="${dark}"/>
    <path d="m41.8 40 1.5 1.5 2.9-3" stroke="#FFFFFF" stroke-width="1.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>`;
  return frame(id, soft, body, defs);
}

/** Syllabus: PDF document with a folded corner and a PDF label band. */
export function syllabusIcon([name, soft, light, dark]) {
  const id = `sy-${name}`;
  const defs =
    linear(`${id}g`, [
      [0, light],
      [1, dark],
    ]) + shadow(`${id}s`, { dy: 1.4, blur: 1.3, opacity: 0.28, color: dark });
  const body = `
    <path d="M15 9h18l9 9v27a3.5 3.5 0 0 1-3.5 3.5h-23.5a3.5 3.5 0 0 1-3.5-3.5v-32.5a3.5 3.5 0 0 1 3.5-3.5z" fill="url(#${id}g)" filter="url(#${id}s)"/>
    <path d="M33 9v6.5a2.5 2.5 0 0 0 2.5 2.5H42z" fill="#FFFFFF" opacity=".55"/>
    <rect x="16.5" y="18" width="12" height="2.6" rx="1.3" fill="#FFFFFF" opacity=".85"/>
    <rect x="16.5" y="23" width="18" height="2.6" rx="1.3" fill="#FFFFFF" opacity=".6"/>
    <rect x="8" y="30" width="28" height="12" rx="3" fill="#FFFFFF"/>
    <text x="22" y="39.3" text-anchor="middle" font-family="DejaVu Sans, Arial, sans-serif" font-size="8.6" font-weight="700" fill="${dark}">PDF</text>`;
  return frame(id, soft, body, defs);
}
