// Multi-colour category illustrations (64x64) and the circular emblems built from them.
import { C, linear, svg, wheel, arcPoints } from './shapes.mjs';

/** Inner artwork for each category, drawn on a 64x64 grid. */
export const CATEGORY_ART = {
  'central-govt': () => `
    <rect x="14" y="54" width="36" height="6" rx="2" fill="#92400E"/>
    <rect x="18" y="48" width="28" height="6" rx="2" fill="${C.amber600}"/>
    <rect x="27" y="27" width="10" height="21" fill="${C.amber500}"/>
    <rect x="29.5" y="27" width="2" height="21" fill="#FDE68A" opacity=".7"/>
    <path d="M19 27h26l-3.5-5h-19z" fill="${C.amber600}"/>
    <rect x="21" y="19" width="22" height="3.2" rx="1.6" fill="#92400E"/>
    ${wheel(32, 10.5, 7.2, C.blue800, 1)}`,
  'state-govt': () => `
    <path d="M22 26 Q32 8 42 26z" fill="${C.green600}"/>
    <rect x="31" y="6" width="2" height="7" fill="${C.green600}"/>
    <rect x="18" y="25" width="28" height="3" rx="1.5" fill="#166534"/>
    <rect x="20" y="28" width="24" height="8" fill="#BBF7D0"/>
    <rect x="6" y="36" width="52" height="4" rx="2" fill="#166534"/>
    <rect x="8" y="40" width="48" height="15" fill="#DCFCE7"/>
    ${[12, 20, 28, 36, 44, 52].map((x) => `<rect x="${x - 1.5}" y="41" width="3" height="13" rx="1" fill="${C.green600}"/>`).join('')}
    <rect x="4" y="55" width="56" height="4" rx="2" fill="#166534"/>`,
  railway: () => `
    <rect x="15" y="6" width="34" height="42" rx="9" fill="${C.red600}"/>
    <rect x="15" y="34" width="34" height="14" rx="0" fill="#B91C1C"/>
    <rect x="15" y="40" width="34" height="8" rx="6" fill="#B91C1C"/>
    <rect x="20" y="12" width="24" height="15" rx="3" fill="#E0F2FE"/>
    <path d="M20 21 L44 14 V12 H20z" fill="#fff" opacity=".6"/>
    <circle cx="23" cy="38" r="3" fill="#FEF08A"/><circle cx="41" cy="38" r="3" fill="#FEF08A"/>
    <rect x="28" y="8.5" width="8" height="2" rx="1" fill="#FECACA"/>
    <path d="M22 48 16 58M42 48l6 10" stroke="${C.slate700}" stroke-width="3" stroke-linecap="round"/>
    <path d="M12 58h40" stroke="${C.slate700}" stroke-width="3" stroke-linecap="round"/>
    <path d="M18 54h28" stroke="${C.slate500}" stroke-width="2.4" stroke-linecap="round"/>`,
  banking: () => `
    <path d="M32 5 58 18H6z" fill="${C.blue600}"/>
    <circle cx="32" cy="13.5" r="3" fill="#fff"/>
    <rect x="6" y="18" width="52" height="4" fill="${C.blue800}"/>
    ${[12, 22, 32, 42, 52].map((x) => `<rect x="${x - 2.5}" y="23" width="5" height="22" rx="1" fill="${C.blue400}"/>`).join('')}
    <rect x="6" y="45" width="52" height="5" rx="1" fill="${C.blue800}"/>
    <rect x="3" y="50" width="58" height="5" rx="2" fill="${C.blue600}"/>
    <circle cx="48" cy="48" r="10" fill="${C.amber500}" stroke="#fff" stroke-width="2"/>
    <path d="M44 43.5h8M44 46.5h8M45 43.5c4 0 4.5 6 0 6l5 5" stroke="#fff" stroke-width="1.6" fill="none" stroke-linecap="round" stroke-linejoin="round"/>`,
  defence: () => `
    <path d="M32 4 52 11v16c0 14-8.5 24-20 30C20.5 51 12 41 12 27V11z" fill="#4D7C0F"/>
    <path d="M32 4v53c-11.5-6-20-16-20-30V11z" fill="#65A30D"/>
    <path d="M32 18l3.6 7.4 8.1 1.2-5.9 5.7 1.4 8.1L32 36.6l-7.2 3.8 1.4-8.1-5.9-5.7 8.1-1.2z" fill="${C.amber300}"/>
    <path d="M4 30c4 0 8 2 10 6M60 30c-4 0-8 2-10 6" stroke="${C.amber500}" stroke-width="3" stroke-linecap="round" fill="none"/>`,
  police: () => `
    <path d="M32 3l6 6 8-1.5 1.5 8 6.5 5-4 7 4 7-6.5 5-1.5 8-8-1.5-6 6-6-6-8 1.5-1.5-8-6.5-5 4-7-4-7 6.5-5 1.5-8 8 1.5z" fill="${C.blue800}"/>
    <circle cx="32" cy="31" r="15" fill="${C.amber500}"/>
    <circle cx="32" cy="31" r="11.5" fill="${C.blue700}"/>
    <path d="M32 22.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8-5.2-2.7-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z" fill="${C.amber300}"/>
    <rect x="22" y="54" width="20" height="6" rx="3" fill="${C.red600}"/>`,
  teaching: () => `
    <rect x="6" y="6" width="52" height="34" rx="4" fill="#065F46"/>
    <rect x="9" y="9" width="46" height="28" rx="2.5" fill="#047857"/>
    <path d="M14 17h18M14 23h12M14 29h22" stroke="#D1FAE5" stroke-width="2" stroke-linecap="round"/>
    <path d="M40 30l8-12" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
    <path d="M10 46c7-3 15-3 22 1 7-4 15-4 22-1v13c-7-3-15-3-22 1-7-4-15-4-22-1z" fill="${C.orange500}"/>
    <path d="M32 47v13" stroke="#C2410C" stroke-width="2"/>
    <path d="M15 51c4-1 8-1 12 1M37 52c4-2 8-2 12-1" stroke="#FFEDD5" stroke-width="1.6" stroke-linecap="round"/>`,
  medical: () => `
    <path d="M32 58C14 46 5 36 5 23.5 5 14.5 12 8 20.5 8c5 0 9 2.5 11.5 6.5C34.5 10.5 38.5 8 43.5 8 52 8 59 14.5 59 23.5 59 36 50 46 32 58z" fill="${C.pink500}"/>
    <path d="M32 58C14 46 5 36 5 23.5 5 14.5 12 8 20.5 8c5 0 9 2.5 11.5 6.5z" fill="#F472B6"/>
    <rect x="27" y="20" width="10" height="26" rx="2.5" fill="#fff"/>
    <rect x="19" y="28" width="26" height="10" rx="2.5" fill="#fff"/>`,
  psu: () => `
    <rect x="10" y="10" width="7" height="22" rx="1.5" fill="${C.slate500}"/>
    <rect x="21" y="16" width="7" height="16" rx="1.5" fill="${C.slate500}"/>
    <path d="M12 7c2-3 6-3 7 0" stroke="${C.slate300}" stroke-width="3" fill="none" stroke-linecap="round"/>
    <path d="M6 58V32l14 8v-8l14 8v-8l14 8V22h10v36z" fill="${C.teal600}"/>
    <path d="M6 58V32l14 8v-8l14 8v-8l14 8v18z" fill="${C.teal500}"/>
    ${[13, 25, 37].map((x) => `<rect x="${x}" y="46" width="6" height="6" rx="1" fill="#CCFBF1"/>`).join('')}
    <rect x="50" y="28" width="5" height="5" rx="1" fill="#CCFBF1"/>`,
  other: () => `
    <path d="M24 16v-4a5 5 0 0 1 5-5h6a5 5 0 0 1 5 5v4" stroke="${C.purple600}" stroke-width="4.5" fill="none" stroke-linecap="round"/>
    <rect x="6" y="16" width="52" height="38" rx="8" fill="${C.purple500}"/>
    <path d="M6 31q26 9 52 0" stroke="#fff" stroke-opacity=".35" stroke-width="3" fill="none"/>
    <rect x="27.5" y="28" width="9" height="9" rx="2.5" fill="#fff"/>
    <circle cx="18" cy="46" r="2.2" fill="#EDE9FE"/><circle cx="25" cy="46" r="2.2" fill="#EDE9FE"/><circle cx="32" cy="46" r="2.2" fill="#EDE9FE"/>`,
  ssc: () => `
    <path d="M14 6h26l12 12v38a3 3 0 0 1-3 3H14a3 3 0 0 1-3-3V9a3 3 0 0 1 3-3z" fill="${C.blue500}"/>
    <path d="M40 6v9a3 3 0 0 0 3 3h9z" fill="${C.blue200}"/>
    <path d="M18 26h22M18 33h18M18 40h14" stroke="#fff" stroke-width="2.6" stroke-linecap="round"/>
    <path d="M44 34l10-10 4 4-10 10-5.5 1.5z" fill="${C.amber500}"/>
    <path d="M54 24l4 4" stroke="#B45309" stroke-width="2"/>`,
  upsc: () => `
    <path d="M18 26 Q32 4 46 26z" fill="${C.indigo600}"/>
    <rect x="31" y="3" width="2" height="7" fill="${C.indigo600}"/>
    <rect x="14" y="25" width="36" height="4" rx="2" fill="#312E81"/>
    ${[18, 25, 32, 39, 46].map((x) => `<rect x="${x - 2}" y="29" width="4" height="22" rx="1" fill="#A5B4FC"/>`).join('')}
    <rect x="10" y="51" width="44" height="4" rx="2" fill="#312E81"/>
    <rect x="6" y="55" width="52" height="4" rx="2" fill="${C.indigo600}"/>`,
  engineering: () => `
    <g transform="translate(32 32)">
      ${Array.from({ length: 8 }, (_, i) => `<rect x="-5" y="-27" width="10" height="12" rx="2.5" fill="${C.slate500}" transform="rotate(${i * 45})"/>`).join('')}
      <circle r="18" fill="${C.slate500}"/>
      <circle r="8" fill="#fff"/>
      <circle r="18" fill="none" stroke="${C.slate700}" stroke-width="2" opacity=".25"/>
    </g>`,
};

export const CATEGORY_TONES = {
  'central-govt': { ring: C.amber600, soft: '#FFFBEB' },
  'state-govt': { ring: C.green600, soft: '#F0FDF4' },
  railway: { ring: C.red600, soft: '#FEF2F2' },
  banking: { ring: C.blue600, soft: '#EFF6FF' },
  defence: { ring: '#4D7C0F', soft: '#F7FEE7' },
  police: { ring: C.blue800, soft: '#EEF2FF' },
  teaching: { ring: '#047857', soft: '#ECFDF5' },
  medical: { ring: C.pink500, soft: '#FDF2F8' },
  psu: { ring: C.teal600, soft: '#F0FDFA' },
  other: { ring: C.purple600, soft: '#F5F3FF' },
  ssc: { ring: C.blue600, soft: '#EFF6FF' },
  upsc: { ring: C.indigo600, soft: '#EEF2FF' },
  engineering: { ring: C.slate700, soft: '#F8FAFC' },
};

export function categoryIcon(key) {
  return svg(64, 64, CATEGORY_ART[key]());
}

/** Circular emblem with a dotted ring and the category art in the centre. */
export function emblem(key) {
  const { ring } = CATEGORY_TONES[key];
  const dots = arcPoints(40, 40, 33.5, 0, 345, 24)
    .map((p) => `<circle cx="${p.x.toFixed(2)}" cy="${p.y.toFixed(2)}" r="1.25" fill="#fff" opacity=".85"/>`)
    .join('');
  const defs = linear('ring', [
    [0, ring],
    [1, ring, 0.78],
  ]) + linear('gold', [
    [0, '#FDE68A'],
    [1, '#F59E0B'],
  ]);
  const body = `
    <circle cx="40" cy="40" r="39" fill="url(#gold)"/>
    <circle cx="40" cy="40" r="37" fill="url(#ring)"/>
    ${dots}
    <circle cx="40" cy="40" r="29" fill="#fff"/>
    <circle cx="40" cy="40" r="29" fill="none" stroke="url(#gold)" stroke-width="2"/>
    <g transform="translate(22 22) scale(0.5625)">${CATEGORY_ART[key]()}</g>`;
  return svg(80, 80, body, defs);
}
