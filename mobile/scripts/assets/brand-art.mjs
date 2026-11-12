// Full-resolution redraws of the uploaded brand artwork (assets/source/reference/).
// The uploads were 42–175 px, too small for 3x screens and the 1024 px store icon, so the same
// designs are drawn here as vectors and rendered at any size.
import { linear, radial } from './shapes.mjs';

const SAFFRON = ['#FFB547', '#F7931E'];
const WHITE = ['#FFFFFF', '#E7EBF2'];
const GREEN = ['#2DB34A', '#138808'];

/** SVG with a drawing grid (vw×vh) shown at an output size in dp (w×h). */
export function art(w, h, vw, vh, body, defs = '') {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${vw} ${vh}">${defs ? `<defs>${defs}</defs>` : ''}${body}</svg>`;
}

function bezier([p0, p1, p2, p3], t) {
  const u = 1 - t;
  const pt = (i) => u * u * u * p0[i] + 3 * u * u * t * p1[i] + 3 * u * t * t * p2[i] + t * t * t * p3[i];
  const d = (i) => 3 * u * u * (p1[i] - p0[i]) + 6 * u * t * (p2[i] - p1[i]) + 3 * t * t * (p3[i] - p2[i]);
  const dx = d(0);
  const dy = d(1);
  const len = Math.hypot(dx, dy) || 1;
  return { x: pt(0), y: pt(1), nx: -dy / len, ny: dx / len };
}

/**
 * Tricolour ribbon along a cubic bezier: saffron on top, white, green below.
 * `band(t)` is the width of one band at position t (0..1).
 */
export function ribbon(id, curve, band, { steps = 120, sheen = true } = {}) {
  const pts = Array.from({ length: steps + 1 }, (_, i) => {
    const t = i / steps;
    return { ...bezier(curve, t), b: band(t) };
  });
  const edge = (k) => pts.map((p) => [p.x - p.nx * p.b * k, p.y - p.ny * p.b * k]);
  const shape = (k1, k2) => {
    const a = edge(k1);
    const b = edge(k2).reverse();
    return `M${[...a, ...b].map(([x, y]) => `${x.toFixed(1)} ${y.toFixed(1)}`).join('L')}Z`;
  };
  const defs =
    linear(`${id}-s`, [
      [0, SAFFRON[0]],
      [1, SAFFRON[1]],
    ]) +
    linear(`${id}-w`, [
      [0, WHITE[0]],
      [1, WHITE[1]],
    ]) +
    linear(`${id}-g`, [
      [0, GREEN[0]],
      [1, GREEN[1]],
    ]);
  const body = `
    <path d="${shape(1.5, 0.5)}" fill="url(#${id}-s)"/>
    <path d="${shape(0.5, -0.5)}" fill="url(#${id}-w)"/>
    <path d="${shape(-0.5, -1.5)}" fill="url(#${id}-g)"/>
    ${sheen ? `<path d="${shape(1.45, 1.05)}" fill="#FFFFFF" opacity=".28"/>` : ''}`;
  return { defs, body };
}

// ---- App icon -------------------------------------------------------------------------

/** Pieces of the app icon on a 1024 grid: blue background, tricolour sweep, white briefcase. */
function iconParts() {
  const r = ribbon('ai', [[-80, 900], [380, 760], [620, 820], [1100, 360]], (t) => 62 + 26 * Math.sin(Math.PI * t), { steps: 160 });
  const defs =
    linear('ai-bg', [
      [0, '#1B8CFF'],
      [0.55, '#0B5FD8'],
      [1, '#0039A6'],
    ]) +
    radial('ai-glow', [
      [0, '#FFFFFF', 0.28],
      [1, '#FFFFFF', 0],
    ], { cx: 0.3, cy: 0.12, r: 0.6 }) +
    linear('ai-case', [
      [0, '#FFFFFF'],
      [1, '#D9E4F7'],
    ]) +
    linear('ai-clasp', [
      [0, '#2F7BF0'],
      [1, '#0B4FC4'],
    ]) +
    `<filter id="ai-sh" x="-20%" y="-20%" width="140%" height="140%"><feDropShadow dx="0" dy="18" stdDeviation="22" flood-color="#00194D" flood-opacity=".35"/></filter>` +
    r.defs;
  const background = `<rect width="1024" height="1024" fill="url(#ai-bg)"/><rect width="1024" height="1024" fill="url(#ai-glow)"/>`;
  const briefcase = (fill = 'url(#ai-case)', clasp = 'url(#ai-clasp)', detail = true) => `
    <path d="M382 270v-66a56 56 0 0 1 56-56h148a56 56 0 0 1 56 56v66" stroke="${fill}" stroke-width="58" fill="none" stroke-linecap="round"/>
    <rect x="160" y="262" width="704" height="500" rx="80" fill="${fill}"/>
    ${detail ? '<path d="M160 470 Q512 560 864 470" stroke="#B9C9E6" stroke-width="12" fill="none" opacity=".8"/>' : ''}
    <rect x="456" y="436" width="112" height="100" rx="28" fill="${clasp}"/>
    ${detail ? '<rect x="480" y="454" width="64" height="16" rx="8" fill="#FFFFFF" opacity=".35"/>' : ''}`;
  return { defs, background, sweep: r.body, briefcase };
}

/** Store / iOS icon: full bleed 1024×1024; the stores apply their own corner mask. */
export function appIcon() {
  const p = iconParts();
  return art(1024, 1024, 1024, 1024, `${p.background}<g filter="url(#ai-sh)">${p.briefcase()}</g>${p.sweep}`, p.defs);
}

/**
 * Android adaptive icon layers (432×432, the launcher crops to a shape inside the middle 66%).
 * The sweep lives in the background layer so the crop never cuts it off mid-band.
 */
export function adaptiveBackground() {
  const p = iconParts();
  return art(432, 432, 1024, 1024, `${p.background}<g transform="translate(512 512) scale(.8) translate(-512 -512)">${p.sweep}</g>`, p.defs);
}

export function adaptiveForeground() {
  const p = iconParts();
  // Briefcase scaled into the 66% safe zone.
  return art(432, 432, 1024, 1024, `<g transform="translate(512 500) scale(.5) translate(-512 -455)" filter="url(#ai-sh)">${p.briefcase()}</g>`, p.defs);
}

/** Android 13 themed icon: single-colour silhouette; the clasp is cut out. */
export function adaptiveMonochrome() {
  const p = iconParts();
  return art(432, 432, 1024, 1024, `<g transform="translate(512 500) scale(.5) translate(-512 -455)">${p.briefcase('#FFFFFF', '#000000', false)}</g>`, p.defs);
}

/** App icon with rounded corners at `dp` size (favicon 48, in-app brand mark 64). */
export function roundedIcon(dp = 48) {
  const p = iconParts();
  return art(dp, dp, 1024, 1024, `<clipPath id="fv"><rect width="1024" height="1024" rx="230"/></clipPath><g clip-path="url(#fv)">${p.background}${p.briefcase()}${p.sweep}</g>`, p.defs);
}

// ---- Logo (briefcase + wordmark) ------------------------------------------------------

/** Briefcase mark of the logo, drawn in a 180×190 box at (x, y). */
function logoCase(x, y) {
  const r = ribbon('lc', [[-30, 190], [60, 150], [110, 165], [205, 70]], (t) => 15 + 6 * Math.sin(Math.PI * t), { steps: 90 });
  const defs =
    linear('lc-body', [
      [0, '#3D8BFF'],
      [1, '#0F56D9'],
    ]) +
    linear('lc-handle', [
      [0, '#4C95FF'],
      [1, '#1D63E0'],
    ]) +
    `<clipPath id="lc-clip"><rect x="0" y="38" width="180" height="150" rx="30"/></clipPath>` +
    `<filter id="lc-sh" x="-20%" y="-20%" width="140%" height="140%"><feDropShadow dx="0" dy="6" stdDeviation="6" flood-color="#0B2A6F" flood-opacity=".25"/></filter>` +
    r.defs;
  const body = `<g transform="translate(${x} ${y})">
    <path d="M58 42V24a16 16 0 0 1 16-16h32a16 16 0 0 1 16 16v18" stroke="url(#lc-handle)" stroke-width="15" fill="none" stroke-linecap="round"/>
    <g filter="url(#lc-sh)">
      <rect x="0" y="38" width="180" height="150" rx="30" fill="url(#lc-body)"/>
      <g clip-path="url(#lc-clip)">
        ${r.body}
        <path d="M0 48 Q90 30 180 48" stroke="#FFFFFF" stroke-opacity=".25" stroke-width="6" fill="none"/>
      </g>
    </g>
    <path d="M26 80 Q90 98 154 80" stroke="#FFFFFF" stroke-width="10" fill="none" stroke-linecap="round"/>
    <rect x="72" y="72" width="36" height="30" rx="9" fill="#FFFFFF"/>
    <rect x="81" y="80" width="18" height="14" rx="4" fill="#1D63E0"/>
  </g>`;
  return { defs, body };
}

/** Wordmark: "Rozgar" in navy, "One" in saffron. Centred on x, baseline at y. */
function wordmark(x, y, size) {
  return `<text x="${x}" y="${y}" text-anchor="middle" font-family="Noto Sans, DejaVu Sans, Arial, sans-serif" font-weight="700" font-size="${size}" letter-spacing="-2"><tspan fill="#1B3A8C">Rozgar</tspan><tspan fill="#F7931E">One</tspan></text>`;
}

function logoBody() {
  const c = logoCase(195, 0);
  return { defs: c.defs, body: c.body + wordmark(285, 330, 102) };
}

/** Splash logo: 570×384 grid, shown at 190×128 dp. */
export function logo() {
  const { defs, body } = logoBody();
  return art(190, 128, 570, 384, body, defs);
}

/** Native splash icon (PNG, 400×400): the logo on a white rounded card. */
export function splashIcon() {
  const { defs, body } = logoBody();
  const card = `<rect x="12" y="12" width="376" height="376" rx="88" fill="#FFFFFF"/>`;
  return art(400, 400, 400, 400, `${card}<g transform="translate(46 85) scale(0.537)">${body}</g>`, defs);
}

// ---- Laurel ---------------------------------------------------------------------------

/** Gold laurel branch curving like "(" with the tip at the top. Right side is mirrored. */
export function laurel(side) {
  const W = 138;
  const H = 246;
  const cx = 150;
  const cy = 125;
  const R = 112;
  const from = 118;
  const to = 236;
  const n = 11;
  const leaves = [];
  for (let i = 0; i < n; i++) {
    const a = ((from + ((to - from) * i) / (n - 1)) * Math.PI) / 180;
    const px = cx + R * Math.cos(a);
    const py = cy + R * Math.sin(a);
    // Tangent towards the tip (increasing angle).
    const tx = -Math.sin(a);
    const ty = Math.cos(a);
    const base = (Math.atan2(tx, -ty) * 180) / Math.PI;
    const k = 1 - i * 0.04;
    const l = 44 * k;
    const w = 15 * k;
    const leaf = (rot, fill) =>
      `<path d="M0 0Q${w} ${-l * 0.45} 0 ${-l}Q${-w} ${-l * 0.45} 0 0Z" fill="${fill}" transform="translate(${px.toFixed(1)} ${py.toFixed(1)}) rotate(${rot.toFixed(1)})"/>` +
      `<path d="M0 -2L0 ${(-l * 0.8).toFixed(1)}" stroke="#A8620D" stroke-width="1.3" stroke-linecap="round" opacity=".55" transform="translate(${px.toFixed(1)} ${py.toFixed(1)}) rotate(${rot.toFixed(1)})"/>`;
    leaves.push(leaf(base - 38, 'url(#lr-a)') + leaf(base + 34, 'url(#lr-b)'));
  }
  const tipA = (to * Math.PI) / 180;
  const tip = `<path d="M0 0Q8 -16 0 -34Q-8 -16 0 0Z" fill="url(#lr-a)" transform="translate(${(cx + R * Math.cos(tipA)).toFixed(1)} ${(cy + R * Math.sin(tipA)).toFixed(1)}) rotate(${((Math.atan2(-Math.sin(tipA), -Math.cos(tipA)) * 180) / Math.PI).toFixed(1)})"/>`;
  const a0 = (from * Math.PI) / 180;
  const stem = `<path d="M${(cx + R * Math.cos(a0)).toFixed(1)} ${(cy + R * Math.sin(a0)).toFixed(1)} A${R} ${R} 0 0 1 ${(cx + R * Math.cos(tipA)).toFixed(1)} ${(cy + R * Math.sin(tipA)).toFixed(1)}" stroke="url(#lr-b)" stroke-width="5" fill="none" stroke-linecap="round"/>`;
  const defs =
    linear('lr-a', [
      [0, '#FFD66B'],
      [1, '#D98A12'],
    ]) +
    linear('lr-b', [
      [0, '#F7B733'],
      [1, '#B86E0B'],
    ]);
  const flip = side === 'right' ? ` transform="translate(${W} 0) scale(-1 1)"` : '';
  const dp = side === 'right' ? [42, 82] : [46, 82];
  return art(dp[0], dp[1], W, H, `<g${flip}>${stem}${leaves.join('')}${tip}</g>`, defs);
}

// ---- Government building --------------------------------------------------------------

/** Domed sandstone building with a colonnade, flag and trees (900×520 grid, 300×174 dp). */
export function governmentBuilding() {
  const cols = (x0, x1, y0, y1, step, w, fill) => {
    let out = '';
    for (let x = x0; x <= x1 + 0.1; x += step) out += `<rect x="${(x - w / 2).toFixed(1)}" y="${y0}" width="${w}" height="${y1 - y0}" rx="${w / 3}" fill="${fill}"/>`;
    return out;
  };
  const recess = (x0, x1, y0, y1, step, w) => {
    let out = '';
    for (let x = x0 + step / 2; x < x1; x += step) out += `<rect x="${(x - (step - w) / 2).toFixed(1)}" y="${y0 + 6}" width="${step - w}" height="${y1 - y0 - 6}" fill="url(#gb-recess)"/>`;
    return out;
  };
  // Bushes: overlapping blobs on a dark base, highlight on the upper-left of each blob.
  const blob = (x, y, r, k) =>
    `<circle cx="${x}" cy="${y}" r="${r}" fill="url(#gb-tree${k})"/><ellipse cx="${x - r * 0.3}" cy="${y - r * 0.38}" rx="${r * 0.42}" ry="${r * 0.28}" fill="#9BE58A" opacity=".35"/>`;
  const trees = (dir) => {
    const s = dir < 0 ? '' : ' transform="translate(900 0) scale(-1 1)"';
    return `<g${s}>
      <path d="M0 520V430Q60 380 140 420Q220 420 290 490L300 520Z" fill="#14612A"/>
      ${blob(28, 440, 40, 2)}${blob(80, 420, 44, 1)}${blob(140, 440, 40, 2)}${blob(55, 480, 42, 1)}${blob(120, 485, 40, 2)}${blob(190, 470, 34, 1)}${blob(240, 495, 28, 2)}${blob(180, 505, 26, 1)}
    </g>`;
  };
  const defs =
    linear('gb-wall', [
      [0, '#F8DDB0'],
      [1, '#E2B271'],
    ]) +
    linear('gb-col', [
      [0, '#FFF1D6'],
      [1, '#EBC48A'],
    ], { x1: 0, y1: 0, x2: 1, y2: 0 }) +
    linear('gb-recess', [
      [0, '#B9824A'],
      [1, '#D6A262'],
    ]) +
    linear('gb-band', [
      [0, '#E9BF83'],
      [1, '#C48A48'],
    ]) +
    linear('gb-dome', [
      [0, '#F6CF8A'],
      [0.6, '#D9A352'],
      [1, '#B47A34'],
    ], { x1: 0, y1: 0, x2: 1, y2: 1 }) +
    linear('gb-tree1', [
      [0, '#4CC25B'],
      [1, '#1D7A2E'],
    ]) +
    linear('gb-tree2', [
      [0, '#3BAF4C'],
      [1, '#166A27'],
    ]);
  const flagW = 92;
  const flagH = 60;
  const fx = 452;
  const fy = 18;
  const band = flagH / 3;
  const wave = (yy, fill) =>
    `<path d="M${fx} ${yy}q${flagW / 4} ${-9} ${flagW / 2} 0t${flagW / 2} 0v${band}q${-flagW / 4} 9 ${-flagW / 2} 0t${-flagW / 2} 0z" fill="${fill}"/>`;
  const chakra = Array.from({ length: 12 }, (_, i) => {
    const a = (i * 15 * Math.PI) / 180;
    const cxk = fx + flagW / 2;
    const cyk = fy + band * 1.5;
    return `<line x1="${(cxk - 7 * Math.cos(a)).toFixed(1)}" y1="${(cyk - 7 * Math.sin(a)).toFixed(1)}" x2="${(cxk + 7 * Math.cos(a)).toFixed(1)}" y2="${(cyk + 7 * Math.sin(a)).toFixed(1)}"/>`;
  }).join('');
  const body = `
    <!-- flag -->
    <rect x="${fx - 4}" y="${fy - 6}" width="5" height="128" rx="2.5" fill="#8A6A45"/>
    <circle cx="${fx - 1.5}" cy="${fy - 8}" r="5" fill="#E8B86A"/>
    ${wave(fy, '#FF9933')}${wave(fy + band, '#FFFFFF')}${wave(fy + band * 2, '#138808')}
    <g stroke="#1E3A8A" stroke-width="1.4"><circle cx="${fx + flagW / 2}" cy="${fy + band * 1.5}" r="7" fill="none"/>${chakra}</g>
    <!-- dome -->
    <rect x="444" y="118" width="12" height="22" rx="3" fill="#C99252"/>
    <circle cx="450" cy="116" r="7" fill="#D9A352"/>
    <path d="M330 240 Q330 132 450 132 Q570 132 570 240 Z" fill="url(#gb-dome)"/>
    ${[365, 400, 450, 500, 535].map((x) => `<path d="M450 134 Q${x} 180 ${x + (x - 450) * 0.2} 240" stroke="#A86F2F" stroke-width="2.4" fill="none" opacity=".45"/>`).join('')}
    <path d="M352 200 Q385 158 436 146" stroke="#FFFFFF" stroke-width="8" fill="none" opacity=".3" stroke-linecap="round"/>
    <rect x="324" y="236" width="252" height="10" rx="4" fill="#C48A48"/>
    <!-- drum -->
    <rect x="336" y="244" width="228" height="8" rx="3" fill="url(#gb-band)"/>
    <rect x="350" y="244" width="200" height="44" fill="url(#gb-wall)"/>
    ${recess(350, 550, 250, 288, 18, 10)}${cols(359, 541, 252, 288, 18, 10, 'url(#gb-col)')}
    <!-- wings -->
    <rect x="40" y="300" width="820" height="16" rx="4" fill="url(#gb-band)"/>
    <rect x="48" y="292" width="804" height="10" rx="3" fill="#F3D3A0"/>
    ${Array.from({ length: 40 }, (_, i) => `<rect x="${52 + i * 20}" y="282" width="8" height="12" rx="2" fill="#E9C086"/>`).join('')}
    <rect x="56" y="316" width="788" height="118" fill="url(#gb-wall)"/>
    ${recess(56, 844, 316, 434, 34, 14)}${cols(73, 827, 320, 434, 34, 14, 'url(#gb-col)')}
    <!-- central portico -->
    <rect x="290" y="286" width="320" height="20" rx="5" fill="url(#gb-band)"/>
    <rect x="300" y="306" width="300" height="128" fill="url(#gb-wall)"/>
    ${recess(300, 600, 306, 434, 30, 16)}${cols(315, 585, 310, 434, 30, 16, 'url(#gb-col)')}
    <path d="M430 434v-60a20 20 0 0 1 40 0v60z" fill="#8C5A2B" opacity=".75"/>
    <!-- plinth and steps -->
    <rect x="30" y="432" width="840" height="20" rx="4" fill="url(#gb-band)"/>
    <rect x="18" y="450" width="864" height="14" rx="4" fill="#D7A766"/>
    <rect x="300" y="462" width="300" height="10" rx="3" fill="#E9C086"/>
    <rect x="285" y="470" width="330" height="10" rx="3" fill="#D7A766"/>
    ${trees(-1)}${trees(1)}`;
  return art(300, 174, 900, 520, body, defs);
}

// ---- Tricolour wave (splash bottom) ---------------------------------------------------

/** Wide tricolour ribbon that rises to the right (1170×516 grid, 390×172 dp). */
export function tricolorWave() {
  const r = ribbon('tw', [[-20, 360], [380, 380], [640, 40], [1150, 170]], (t) => (58 + 22 * Math.sin(Math.PI * t)) * (t > 0.85 ? 1 - (t - 0.85) * 2.2 : 1), { steps: 180, sheen: false });
  // Right end fades out, like the uploaded artwork.
  const defs =
    r.defs +
    linear('tw-fade', [
      [0, '#FFFFFF'],
      [0.82, '#FFFFFF'],
      [1, '#FFFFFF', 0],
    ], { x1: 0, y1: 0, x2: 1, y2: 0 }) +
    `<mask id="tw-mask"><rect width="1170" height="516" fill="url(#tw-fade)"/></mask>`;
  return art(390, 172, 1170, 516, `<g mask="url(#tw-mask)">${r.body}</g>`, defs);
}
