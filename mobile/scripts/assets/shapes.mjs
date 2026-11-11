// Shared SVG building blocks for the RozgarOne asset set.
// Everything is drawn in code so the artwork stays original and can be regenerated.

export const C = {
  blue900: '#1E3A8A',
  blue800: '#1E40AF',
  blue700: '#1D4ED8',
  blue600: '#2563EB',
  blue500: '#3B82F6',
  blue400: '#60A5FA',
  blue200: '#BFDBFE',
  blue100: '#DBEAFE',
  blue50: '#EFF6FF',
  saffron: '#FF9933',
  saffronDark: '#EA580C',
  green: '#138808',
  green600: '#16A34A',
  green500: '#22C55E',
  green100: '#DCFCE7',
  red600: '#DC2626',
  red500: '#EF4444',
  red100: '#FEE2E2',
  rose600: '#E11D48',
  orange500: '#F97316',
  orange100: '#FFEDD5',
  amber600: '#D97706',
  amber500: '#F59E0B',
  amber300: '#FCD34D',
  amber100: '#FEF3C7',
  purple600: '#7C3AED',
  purple500: '#8B5CF6',
  purple100: '#EDE9FE',
  indigo600: '#4F46E5',
  sky500: '#0EA5E9',
  teal600: '#0D9488',
  teal500: '#14B8A6',
  pink500: '#EC4899',
  slate900: '#0F172A',
  slate700: '#334155',
  slate500: '#64748B',
  slate300: '#CBD5E1',
  slate200: '#E2E8F0',
  slate100: '#F1F5F9',
  white: '#FFFFFF',
  skin: '#F5C9A5',
  skinShade: '#E8AE85',
  hair: '#1F2937',
};

export function svg(width, height, body, defs = '') {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">${
    defs ? `<defs>${defs}</defs>` : ''
  }${body}</svg>`;
}

export function linear(id, stops, { x1 = 0, y1 = 0, x2 = 0, y2 = 1 } = {}) {
  const s = stops.map(([o, c, op = 1]) => `<stop offset="${o}" stop-color="${c}" stop-opacity="${op}"/>`).join('');
  return `<linearGradient id="${id}" x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}">${s}</linearGradient>`;
}

export function radial(id, stops, { cx = 0.5, cy = 0.5, r = 0.5 } = {}) {
  const s = stops.map(([o, c, op = 1]) => `<stop offset="${o}" stop-color="${c}" stop-opacity="${op}"/>`).join('');
  return `<radialGradient id="${id}" cx="${cx}" cy="${cy}" r="${r}">${s}</radialGradient>`;
}

export function shadow(id, { dy = 2, blur = 3, opacity = 0.18, color = '#0F172A' } = {}) {
  return `<filter id="${id}" x="-30%" y="-30%" width="160%" height="160%"><feDropShadow dx="0" dy="${dy}" stdDeviation="${blur}" flood-color="${color}" flood-opacity="${opacity}"/></filter>`;
}

/** Places a 24x24 stroke glyph inside a box. */
export function glyph(name, { x = 0, y = 0, size = 24, color = C.white, width = 2 } = {}) {
  const s = size / 24;
  return `<g transform="translate(${x} ${y}) scale(${s})" color="${color}" fill="none" stroke="${color}" stroke-width="${width}" stroke-linecap="round" stroke-linejoin="round">${GLYPHS[name]}</g>`;
}

/** Simple line glyphs on a 24x24 grid. */
export const GLYPHS = {
  briefcase:
    '<rect x="3" y="7.5" width="18" height="12.5" rx="2.5"/><path d="M8.5 7.5V5.8A1.8 1.8 0 0 1 10.3 4h3.4a1.8 1.8 0 0 1 1.8 1.8v1.7"/><path d="M3 12.8h18"/><path d="M11 12.8v1.6h2v-1.6"/>',
  results: '<path d="M13.5 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h3.5"/><path d="M13.5 3 19 8.5"/><path d="M8.5 8h4M8.5 11.5h3"/><circle cx="15.5" cy="15.5" r="3.6"/><path d="m18.2 18.2 2.8 2.8"/>',
  idCard:
    '<rect x="3" y="5" width="18" height="14" rx="2.5"/><circle cx="8.5" cy="10.8" r="2"/><path d="M5.6 15.8c.6-1.5 1.6-2.3 2.9-2.3s2.3.8 2.9 2.3"/><path d="M14 10h4M14 13.5h3"/>',
  answerKey:
    '<rect x="4.5" y="3" width="15" height="18" rx="2.5"/><path d="m8 8.2 1.4 1.4 2.6-2.6"/><path d="M14 8.5h2"/><path d="m8 14 1.4 1.4 2.6-2.6"/><path d="M14 14.3h2"/>',
  book: '<path d="M4 19.5v-14A2.5 2.5 0 0 1 6.5 3H20v14H6.5A2.5 2.5 0 0 0 4 19.5 2.5 2.5 0 0 0 6.5 22H20"/><path d="M9 7.5h6M9 11h4"/>',
  calendar:
    '<rect x="3" y="5" width="18" height="16" rx="2.5"/><path d="M3 10h18M8 3v4M16 3v4"/><circle cx="8" cy="14.2" r=".9" fill="currentColor" stroke="none"/><circle cx="12" cy="14.2" r=".9" fill="currentColor" stroke="none"/><circle cx="16" cy="14.2" r=".9" fill="currentColor" stroke="none"/><circle cx="8" cy="17.6" r=".9" fill="currentColor" stroke="none"/><circle cx="12" cy="17.6" r=".9" fill="currentColor" stroke="none"/>',
  calendarAlert:
    '<rect x="3" y="5" width="18" height="16" rx="2.5"/><path d="M3 10h18M8 3v4M16 3v4"/><path d="M12 12.5v3.5"/><path d="M12 18.6v.1"/>',
  bell: '<path d="M6 16.5v-5a6 6 0 1 1 12 0v5l1.6 2H4.4z"/><path d="M10 20.8a2.1 2.1 0 0 0 4 0"/>',
  checkCircle: '<circle cx="12" cy="12" r="9"/><path d="m8 12.4 2.7 2.7 5.6-5.8"/>',
  megaphone: '<path d="M4 10v4a1 1 0 0 0 1 1h2l6.5 4.5v-15L7 9H5a1 1 0 0 0-1 1z"/><path d="M17 9a4.2 4.2 0 0 1 0 6"/><path d="m8 15 1.2 5"/>',
  trophy:
    '<path d="M8 4h8v5.5a4 4 0 0 1-8 0z"/><path d="M8 6.2H5.2a3 3 0 0 0 3 4.2M16 6.2h2.8a3 3 0 0 1-3 4.2"/><path d="M12 13.5V17M8.5 20.5h7M9.8 17h4.4"/>',
  pdf: '<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5"/><path d="M8.5 12.5h7M8.5 16h5"/>',
  cap: '<path d="m2.5 9.5 9.5-5 9.5 5-9.5 5z"/><path d="M6.2 11.5v4.7c3.2 2.6 8.4 2.6 11.6 0v-4.7"/><path d="M21.5 9.5v5.5"/>',
  sparkle:
    '<path d="M11 3c.6 4.3 2.3 6.4 6.6 7.2-4.3.8-6 2.9-6.6 7.2-.6-4.3-2.3-6.4-6.6-7.2C8.7 9.4 10.4 7.3 11 3z"/><path d="M18.5 14.5c.3 1.9 1 2.8 2.9 3.1-1.9.3-2.6 1.2-2.9 3.1-.3-1.9-1-2.8-2.9-3.1 1.9-.3 2.6-1.2 2.9-3.1z"/>',
  download: '<path d="M12 4v11"/><path d="m7.5 10.5 4.5 4.5 4.5-4.5"/><path d="M5 19.5h14"/>',
  megaphoneSmall: '<path d="M5 10v4h2.5l6 4V6l-6 4z"/>',
};

/** Rounded square tile with a gradient and a white glyph (used by Home quick links). */
export function tile(id, from, to, glyphName, size = 48) {
  const g = linear(`${id}g`, [
    [0, from],
    [1, to],
  ], { x1: 0, y1: 0, x2: 1, y2: 1 });
  const body = `
    <rect x="2" y="3" width="${size - 4}" height="${size - 4}" rx="${size * 0.28}" fill="url(#${id}g)" filter="url(#${id}s)"/>
    ${glyph(glyphName, { x: size * 0.25, y: size * 0.26, size: size * 0.5, width: 2.1 })}`;
  return svg(size, size, body, g + shadow(`${id}s`, { dy: 1.5, blur: 1.6, opacity: 0.22, color: to }));
}

/** Circle with a soft background and a coloured glyph (notifications). */
export function circleIcon(soft, strong, glyphName, size = 44) {
  return svg(
    size,
    size,
    `<circle cx="${size / 2}" cy="${size / 2}" r="${size / 2}" fill="${soft}"/>${glyph(glyphName, {
      x: size * 0.25,
      y: size * 0.25,
      size: size * 0.5,
      color: strong,
      width: 2.2,
    })}`,
  );
}

/** Glyph-only icon in black; the app tints it with `tintColor`. */
export function tintable(glyphName, size = 32) {
  return svg(size, size, glyph(glyphName, { x: 2, y: 2, size: size - 4, color: '#000', width: 2 }));
}

/** Points along an arc, used for laurels and seal rings. */
export function arcPoints(cx, cy, r, fromDeg, toDeg, n) {
  const out = [];
  for (let i = 0; i < n; i++) {
    const a = ((fromDeg + ((toDeg - fromDeg) * i) / (n - 1)) * Math.PI) / 180;
    out.push({ x: cx + r * Math.cos(a), y: cy + r * Math.sin(a), deg: (a * 180) / Math.PI });
  }
  return out;
}

/** Ashoka-style 24-spoke wheel. */
export function wheel(cx, cy, r, color, width = 1.2) {
  let spokes = '';
  for (let i = 0; i < 24; i++) {
    const a = (i * 15 * Math.PI) / 180;
    spokes += `<line x1="${cx}" y1="${cy}" x2="${(cx + r * Math.cos(a)).toFixed(2)}" y2="${(cy + r * Math.sin(a)).toFixed(2)}"/>`;
  }
  return `<g stroke="${color}" stroke-width="${width}" fill="none"><circle cx="${cx}" cy="${cy}" r="${r}"/>${spokes}</g><circle cx="${cx}" cy="${cy}" r="${r * 0.18}" fill="${color}"/>`;
}

/** Indian tricolour flag on a pole. */
export function flag(x, y, w, h, { wave = true } = {}) {
  const band = h / 3;
  const d = (yy) =>
    wave
      ? `M${x} ${yy} q${w * 0.25} ${-h * 0.12} ${w * 0.5} 0 t${w * 0.5} 0 v${band} q${-w * 0.25} ${h * 0.12} ${-w * 0.5} 0 t${-w * 0.5} 0z`
      : `M${x} ${yy}h${w}v${band}h${-w}z`;
  return `
    <rect x="${x - 2.2}" y="${y - 4}" width="2.2" height="${h * 3.2}" rx="1.1" fill="#94A3B8"/>
    <circle cx="${x - 1.1}" cy="${y - 4.5}" r="1.8" fill="#CBD5E1"/>
    <path d="${d(y)}" fill="${C.saffron}"/>
    <path d="${d(y + band)}" fill="#FFFFFF"/>
    <path d="${d(y + band * 2)}" fill="${C.green}"/>
    ${wheel(x + w / 2, y + band * 1.5 - (wave ? 0 : 0), band * 0.36, '#1E3A8A', 0.45)}`;
}

/** Parliament-style rotunda building, drawn in the given tones. */
export function parliament(x, y, w, { body = '#E2E8F0', shade = '#CBD5E1', dark = '#94A3B8', dome = '#E2E8F0' } = {}) {
  const s = w / 200;
  const cols = [];
  for (let i = 0; i < 17; i++) cols.push(`<rect x="${12 + i * 10.6}" y="78" width="4.4" height="30" rx="1" fill="${shade}"/>`);
  const drumCols = [];
  for (let i = 0; i < 9; i++) drumCols.push(`<rect x="${66 + i * 8.2}" y="46" width="3" height="20" fill="${shade}"/>`);
  return `<g transform="translate(${x} ${y}) scale(${s})">
    <path d="M70 46 Q100 6 130 46 Z" fill="${dome}"/>
    <path d="M86 26 Q100 16 114 26" stroke="${dark}" stroke-width="1.4" fill="none" opacity=".55"/>
    <rect x="97.5" y="6" width="5" height="8" rx="1.5" fill="${dark}"/>
    <rect x="60" y="44" width="80" height="4" rx="1.5" fill="${dark}"/>
    <rect x="62" y="48" width="76" height="20" fill="${body}"/>
    ${drumCols.join('')}
    <rect x="4" y="68" width="192" height="6" rx="2" fill="${dark}"/>
    <rect x="6" y="74" width="188" height="36" fill="${body}"/>
    ${cols.join('')}
    <rect x="0" y="108" width="200" height="8" rx="2" fill="${dark}"/>
    <rect x="-8" y="116" width="216" height="6" rx="2" fill="${shade}"/>
  </g>`;
}

/** Briefcase mark used by the logo and app icons. */
export function briefcaseMark(cx, cy, size, fill, cut = '#FFFFFF') {
  const s = size / 100;
  return `<g transform="translate(${cx - size / 2} ${cy - size / 2}) scale(${s})">
    <path d="M36 24v-6a8 8 0 0 1 8-8h12a8 8 0 0 1 8 8v6" stroke="${fill}" stroke-width="8" fill="none" stroke-linecap="round"/>
    <rect x="6" y="24" width="88" height="64" rx="13" fill="${fill}"/>
    <path d="M6 50 Q50 62 94 50" stroke="${cut}" stroke-opacity=".38" stroke-width="5" fill="none"/>
    <rect x="42" y="45" width="16" height="16" rx="4" fill="${cut}"/>
  </g>`;
}
