// Larger illustrations: brand, splash, onboarding, home header, AI robot, eligibility, avatar.
import { C, arcPoints, briefcaseMark, flag, glyph, linear, parliament, radial, shadow, svg, wheel } from './shapes.mjs';

const brandGradient = (id) =>
  linear(id, [
    [0, C.blue500],
    [1, C.blue800],
  ], { x1: 0, y1: 0, x2: 1, y2: 1 });

/** White rounded tile with the gradient briefcase (splash + about screen). */
export function logoTile() {
  return svg(
    120,
    120,
    `<rect x="6" y="6" width="108" height="108" rx="30" fill="#fff" filter="url(#s)"/>
     <rect x="6" y="6" width="108" height="54" rx="30" fill="#fff"/>
     ${briefcaseMark(60, 62, 64, 'url(#b)')}`,
    brandGradient('b') + shadow('s', { dy: 4, blur: 4, opacity: 0.25, color: '#0B2A6F' }),
  );
}

/** Gradient tile with a white briefcase (header + small brand mark). */
export function logoMark() {
  return svg(64, 64, `<rect width="64" height="64" rx="18" fill="url(#b)"/>${briefcaseMark(32, 33.5, 36, '#fff', C.blue700)}`, brandGradient('b'));
}

/** Two laurel branches that frame the splash title. */
export function laurel() {
  // Each branch follows an arc around the title; leaves sit in pairs on both sides of the stem.
  const branch = (side) => {
    const left = side === 'left';
    const cx = 160;
    const cy = 78;
    const r = 118;
    const from = left ? 160 : 20;
    const to = left ? 228 : -48;
    const pts = arcPoints(cx, cy, r, from, to, 9);
    const stem = `<path d="M${pts[0].x.toFixed(1)} ${pts[0].y.toFixed(1)} A${r} ${r} 0 0 ${left ? 1 : 0} ${pts[8].x.toFixed(1)} ${pts[8].y.toFixed(1)}" stroke="#fff" stroke-width="2.4" fill="none" stroke-linecap="round" opacity=".9"/>`;
    const leaves = pts
      .slice(0, 8)
      .map((p, i) => {
        // Tangent pointing towards the tip of the branch.
        const tangent = p.deg + (left ? 90 : -90);
        const size = 1 - i * 0.06;
        const ry = 10 * size;
        const rx = 4.2 * size;
        const leaf = (angle, op) =>
          `<ellipse cx="0" cy="${-ry}" rx="${rx.toFixed(1)}" ry="${ry.toFixed(1)}" fill="#fff" opacity="${op}" transform="translate(${p.x.toFixed(1)} ${p.y.toFixed(1)}) rotate(${(angle + 90).toFixed(1)})"/>`;
        return leaf(tangent - 38, 0.95) + leaf(tangent + 38, 0.75);
      })
      .join('');
    return stem + leaves;
  };
  return svg(320, 150, branch('left') + branch('right'));
}

/** Full-screen splash background: blue gradient, light rays, Parliament silhouette and a tricolour wave. */
export function splashBackground() {
  const w = 390;
  const h = 844;
  const defs =
    linear('bg', [
      [0, '#1E4FD8'],
      [0.45, '#1D4ED8'],
      [1, '#172E7A'],
    ]) +
    radial('glow', [
      [0, '#60A5FA', 0.55],
      [1, '#60A5FA', 0],
    ], { cx: 0.5, cy: 0.28, r: 0.55 }) +
    linear('sil', [
      [0, '#fff', 0.2],
      [1, '#fff', 0.05],
    ]);
  const rays = Array.from({ length: 14 }, (_, i) => {
    const a = (-80 + i * 12) * (Math.PI / 180);
    const x2 = w / 2 + Math.cos(a) * 700;
    const y2 = 250 + Math.sin(a) * 700;
    return `<path d="M${w / 2} 250 L${x2.toFixed(0)} ${y2.toFixed(0)} L${(x2 + 40).toFixed(0)} ${(y2 + 20).toFixed(0)}z" fill="#fff" opacity=".035"/>`;
  }).join('');
  const body = `
    <rect width="${w}" height="${h}" fill="url(#bg)"/>
    <rect width="${w}" height="${h}" fill="url(#glow)"/>
    ${rays}
    <g opacity=".9">${parliament(35, 560, 320, { body: 'url(#sil)', shade: 'rgba(255,255,255,0.10)', dark: 'rgba(255,255,255,0.16)', dome: 'url(#sil)' })}</g>
    <path d="M0 740 C110 690 220 760 390 700 V844 H0z" fill="${C.saffron}"/>
    <path d="M0 760 C120 712 230 782 390 724 V844 H0z" fill="#fff"/>
    <path d="M0 784 C130 738 240 806 390 752 V844 H0z" fill="${C.green}"/>
    <path d="M0 804 C140 762 250 828 390 780 V844 H0z" fill="#0F6E07"/>
    ${wheel(300, 742, 9, '#1E3A8A', 0.7)}`;
  return svg(w, h, body, defs);
}

/** Header background for the Home screen. */
export function homeHeader() {
  const w = 390;
  const h = 230;
  const defs =
    linear('bg', [
      [0, '#1D4ED8'],
      [1, '#2563EB'],
    ], { x1: 0, y1: 0, x2: 1, y2: 1 }) +
    radial('glow', [
      [0, '#93C5FD', 0.45],
      [1, '#93C5FD', 0],
    ], { cx: 0.85, cy: 0.1, r: 0.6 });
  const rings = [60, 95, 130].map((r) => `<circle cx="350" cy="10" r="${r}" fill="none" stroke="#fff" stroke-opacity=".06" stroke-width="18"/>`).join('');
  return svg(w, h, `<rect width="${w}" height="${h}" fill="url(#bg)"/><rect width="${w}" height="${h}" fill="url(#glow)"/>${rings}`, defs);
}

/** Hero banner artwork; the app draws the heading text on top. */
export function heroBanner() {
  const w = 342;
  const h = 128;
  const defs =
    linear('bg', [
      [0, '#3B82F6'],
      [1, '#1D4ED8'],
    ], { x1: 0, y1: 0, x2: 1, y2: 1 }) +
    radial('sun', [
      [0, '#FDE68A', 0.55],
      [1, '#FDE68A', 0],
    ], { cx: 0.78, cy: 0.35, r: 0.4 });
  const body = `
    <rect width="${w}" height="${h}" rx="18" fill="url(#bg)"/>
    <rect width="${w}" height="${h}" rx="18" fill="url(#sun)"/>
    <path d="M150 128 C210 96 270 104 342 88 V110 a18 18 0 0 1 -18 18z" fill="#fff" opacity=".08"/>
    ${parliament(176, 26, 160, { body: '#F8FAFC', shade: '#CBD5E1', dark: '#94A3B8', dome: '#F1F5F9' })}
    ${flag(300, 8, 30, 20)}`;
  return svg(w, h, body, defs);
}

function floatingCard(x, y, color, glyphName, rot = 0, size = 40) {
  return `<g transform="rotate(${rot} ${x + size / 2} ${y + size / 2})" filter="url(#cs)">
    <rect x="${x}" y="${y}" width="${size}" height="${size}" rx="${size * 0.26}" fill="${color}"/>
    ${glyph(glyphName, { x: x + size * 0.22, y: y + size * 0.22, size: size * 0.56, width: 2.2 })}
  </g>`;
}

/** Head with hair, ears, eyes, brows and smile, centred on (cx, cy), unit = head half-width. */
function face(cx, cy, r) {
  const k = r / 31;
  return `<g transform="translate(${cx} ${cy}) scale(${k})">
    <ellipse cx="-30" cy="4" rx="6" ry="9" fill="${C.skinShade}"/>
    <ellipse cx="30" cy="4" rx="6" ry="9" fill="${C.skinShade}"/>
    <ellipse cx="0" cy="0" rx="30" ry="34" fill="url(#skin)"/>
    <path d="M-31 -2 C-35 -32 -14 -44 4 -42 C24 -40 36 -26 31 -2 C29 -12 24 -20 16 -22 C6 -16 -12 -14 -24 -20 C-27 -14 -29 -8 -31 -2z" fill="${C.hair}"/>
    <path d="M-14 -36 C-6 -40 6 -40 14 -36" stroke="#475569" stroke-width="2" fill="none" stroke-linecap="round" opacity=".6"/>
    <path d="M-17 -6 q6 -4 11 0M6 -6 q6 -4 11 0" stroke="${C.hair}" stroke-width="2.6" fill="none" stroke-linecap="round"/>
    <ellipse cx="-11" cy="3" rx="3.2" ry="3.8" fill="${C.hair}"/><ellipse cx="11" cy="3" rx="3.2" ry="3.8" fill="${C.hair}"/>
    <circle cx="-10" cy="1.8" r="1.1" fill="#fff"/><circle cx="12" cy="1.8" r="1.1" fill="#fff"/>
    <path d="M-2 8 q2 4 4 0" stroke="${C.skinShade}" stroke-width="2" fill="none" stroke-linecap="round"/>
    <path d="M-10 17 Q0 25 10 17" stroke="#9A3412" stroke-width="3" fill="none" stroke-linecap="round"/>
    <ellipse cx="-19" cy="13" rx="5" ry="3" fill="#FB7185" opacity=".25"/><ellipse cx="19" cy="13" rx="5" ry="3" fill="#FB7185" opacity=".25"/>
  </g>`;
}

const SKIN_DEFS = linear('skin', [
  [0, '#F8D5B8'],
  [1, C.skin],
]);

function person({ shirt = C.blue600, shirtShade = C.blue800 } = {}) {
  // Clipped to the backdrop circle so the torso ends in a curve, not a flat cut.
  return `<g clip-path="url(#stage)">
    <path d="M74 300 C74 222 100 196 140 196 C180 196 206 222 206 300z" fill="url(#shirt)"/>
    <path d="M124 196 L140 222 L156 196z" fill="#fff"/>
    <path d="M118 194 L140 222 L128 230 L112 200z" fill="${shirtShade}" opacity=".9"/>
    <path d="M162 194 L140 222 L152 230 L168 200z" fill="${shirtShade}" opacity=".9"/>
    <path d="M140 230 V300" stroke="${shirtShade}" stroke-width="2" opacity=".35"/>
    <circle cx="140" cy="248" r="2.4" fill="#fff" opacity=".7"/><circle cx="140" cy="270" r="2.4" fill="#fff" opacity=".7"/>
    <rect x="129" y="170" width="22" height="30" rx="9" fill="${C.skinShade}"/>
  </g>
  ${face(140, 140, 31)}
  <path d="M188 222 C206 210 214 194 214 176" stroke="url(#shirt)" stroke-width="21" fill="none" stroke-linecap="round"/>
  <g transform="rotate(12 214 150)">
    <rect x="198" y="114" width="34" height="60" rx="8" fill="${C.slate900}"/>
    <rect x="201.5" y="120" width="27" height="47" rx="4.5" fill="url(#screen)"/>
    <rect x="210" y="116.5" width="10" height="2" rx="1" fill="#334155"/>
    <rect x="205" y="126" width="19" height="9" rx="2.5" fill="#fff" opacity=".95"/>
    <rect x="207" y="128.5" width="8" height="1.8" rx=".9" fill="${C.blue600}"/>
    <rect x="207" y="131.5" width="12" height="1.6" rx=".8" fill="${C.slate300}"/>
    <rect x="205" y="139" width="19" height="9" rx="2.5" fill="#fff" opacity=".8"/>
    <rect x="205" y="152" width="19" height="9" rx="2.5" fill="#fff" opacity=".65"/>
  </g>
  <path d="M200 172 c-2 -8 6 -12 10 -8 l8 -2 c5 0 6 6 2 8 l-1 6 c-1 6 -9 8 -14 5z" fill="url(#skin)"/>
  <path d="M204 164 h10" stroke="${C.skinShade}" stroke-width="1.6" stroke-linecap="round"/>`;
}

const ONBOARDING_DEFS =
  radial('blob', [
    [0, '#DBEAFE'],
    [1, '#EFF6FF'],
  ]) +
  shadow('cs', { dy: 3, blur: 3, opacity: 0.2 }) +
  SKIN_DEFS +
  linear('shirt', [
    [0, C.blue500],
    [1, C.blue700],
  ]) +
  linear('screen', [
    [0, C.blue400],
    [1, C.blue600],
  ]) +
  '<clipPath id="stage"><circle cx="140" cy="150" r="118"/></clipPath>';

export function onboardingJobs() {
  const body = `
    <circle cx="140" cy="150" r="118" fill="url(#blob)"/>
    <circle cx="140" cy="150" r="96" fill="#fff" opacity=".55"/>
    ${person()}
    ${floatingCard(38, 58, C.purple500, 'results', -10)}
    ${floatingCard(186, 30, C.purple600, 'idCard', 8, 44)}
    ${floatingCard(214, 196, C.amber500, 'bell', 10, 38)}
    ${floatingCard(30, 168, C.green500, 'checkCircle', -8, 38)}
    ${floatingCard(100, 24, C.blue500, 'briefcase', -4, 34)}
    <circle cx="70" cy="122" r="4" fill="${C.blue400}"/><circle cx="232" cy="96" r="3" fill="${C.amber500}"/><circle cx="58" cy="232" r="3" fill="${C.purple500}"/>`;
  return svg(280, 280, body, ONBOARDING_DEFS);
}

export function onboardingEligibility() {
  const defs = ONBOARDING_DEFS + linear('clip', [
    [0, C.blue500],
    [1, C.blue700],
  ]) + linear('lens', [
    [0, C.blue500],
    [1, C.blue700],
  ]);
  const body = `
    <circle cx="140" cy="150" r="118" fill="url(#blob)"/>
    <g filter="url(#cs)">
      <rect x="74" y="62" width="132" height="168" rx="16" fill="#fff"/>
      <rect x="74" y="62" width="132" height="168" rx="16" fill="none" stroke="${C.blue100}" stroke-width="2"/>
      <rect x="110" y="50" width="60" height="24" rx="10" fill="url(#clip)"/>
      <circle cx="140" cy="62" r="5" fill="#fff"/>
    </g>
    ${[96, 136, 176]
      .map(
        (y, i) => `<circle cx="102" cy="${y}" r="11" fill="${i === 2 ? C.slate200 : C.green500}"/>
      ${i === 2 ? '' : `<path d="M96.5 ${y} l4 4 7-8" stroke="#fff" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round"/>`}
      <rect x="122" y="${y - 7}" width="64" height="6" rx="3" fill="${C.slate300}"/>
      <rect x="122" y="${y + 3}" width="42" height="5" rx="2.5" fill="${C.slate200}"/>`,
      )
      .join('')}
    <g filter="url(#cs)">
      <circle cx="210" cy="194" r="32" fill="url(#lens)" stroke="#fff" stroke-width="4"/>
      <circle cx="205" cy="189" r="13" fill="none" stroke="#fff" stroke-width="5"/>
      <path d="M215 199 l10 10" stroke="#fff" stroke-width="6" stroke-linecap="round"/>
    </g>
    ${floatingCard(30, 70, C.amber500, 'cap', -10, 40)}`;
  return svg(280, 280, body, defs);
}

export function onboardingAlerts() {
  const body = `
    <circle cx="140" cy="150" r="118" fill="url(#blob)"/>
    <g filter="url(#cs)">
      <rect x="58" y="90" width="132" height="132" rx="18" fill="#fff"/>
      <rect x="58" y="90" width="132" height="34" rx="18" fill="${C.rose600}"/>
      <rect x="58" y="108" width="132" height="16" fill="${C.rose600}"/>
      <rect x="84" y="80" width="8" height="22" rx="4" fill="${C.slate700}"/><rect x="156" y="80" width="8" height="22" rx="4" fill="${C.slate700}"/>
    </g>
    ${Array.from({ length: 12 }, (_, i) => {
      const x = 74 + (i % 4) * 28;
      const y = 136 + Math.floor(i / 4) * 26;
      const hot = i === 6;
      return `<rect x="${x}" y="${y}" width="20" height="18" rx="5" fill="${hot ? C.rose600 : C.slate100}"/>`;
    }).join('')}
    <g filter="url(#cs)" transform="translate(-6 8) rotate(14 206 92)">
      <path d="M182 108 v-18 a24 24 0 1 1 48 0 v18 l7 9 h-62z" fill="${C.amber500}"/>
      <circle cx="206" cy="124" r="8" fill="${C.amber600}"/>
      <circle cx="226" cy="62" r="10" fill="${C.rose600}"/>
    </g>
    <path d="M234 58 q9 8 7 20M245 50 q14 13 10 32" stroke="${C.amber500}" stroke-width="3.5" fill="none" stroke-linecap="round"/>`;
  return svg(280, 280, body, ONBOARDING_DEFS);
}

/** Friendly robot mascot for the AI assistant. */
export function robot() {
  const defs =
    linear('body', [
      [0, '#FFFFFF'],
      [1, '#E2E8F0'],
    ]) +
    linear('visor', [
      [0, '#1E3A8A'],
      [1, '#2563EB'],
    ]) +
    radial('halo', [
      [0, '#BFDBFE', 0.9],
      [1, '#DBEAFE', 0],
    ]) +
    shadow('rs', { dy: 4, blur: 4, opacity: 0.22, color: '#1E3A8A' });
  const body = `
    <circle cx="90" cy="92" r="86" fill="url(#halo)"/>
    <g filter="url(#rs)">
      <rect x="88" y="14" width="4" height="18" rx="2" fill="${C.slate300}"/>
      <circle cx="90" cy="13" r="7" fill="${C.blue500}"/>
      <rect x="40" y="30" width="100" height="78" rx="34" fill="url(#body)"/>
      <rect x="30" y="58" width="14" height="28" rx="7" fill="${C.blue600}"/>
      <rect x="136" y="58" width="14" height="28" rx="7" fill="${C.blue600}"/>
      <rect x="52" y="44" width="76" height="50" rx="24" fill="url(#visor)"/>
      <ellipse cx="74" cy="67" rx="8" ry="10" fill="#7DD3FC"/><ellipse cx="106" cy="67" rx="8" ry="10" fill="#7DD3FC"/>
      <ellipse cx="76" cy="64" rx="3" ry="3.5" fill="#fff"/><ellipse cx="108" cy="64" rx="3" ry="3.5" fill="#fff"/>
      <path d="M80 82 Q90 89 100 82" stroke="#7DD3FC" stroke-width="3.4" fill="none" stroke-linecap="round"/>
      <rect x="58" y="112" width="64" height="48" rx="22" fill="url(#body)"/>
      <circle cx="90" cy="134" r="10" fill="${C.blue100}"/>
      ${glyph('sparkle', { x: 81, y: 125, size: 18, color: C.blue600, width: 2 })}
      <path d="M58 124 C40 118 30 104 34 92" stroke="#E2E8F0" stroke-width="12" fill="none" stroke-linecap="round"/>
      <circle cx="34" cy="88" r="9" fill="#fff"/>
      <path d="M122 126 C136 132 142 142 140 152" stroke="#E2E8F0" stroke-width="12" fill="none" stroke-linecap="round"/>
    </g>
    ${glyph('sparkle', { x: 138, y: 18, size: 26, color: C.amber500, width: 2.2 })}`;
  return svg(180, 176, body, defs);
}

/** Clipboard with a tick for the eligibility checker. */
export function checklist() {
  const defs = linear('cb', [
    [0, C.blue500],
    [1, C.blue700],
  ]) + shadow('ks', { dy: 3, blur: 3, opacity: 0.2 });
  const body = `
    <g filter="url(#ks)">
      <rect x="20" y="16" width="64" height="80" rx="10" fill="url(#cb)"/>
      <rect x="27" y="24" width="50" height="66" rx="6" fill="#fff"/>
      <rect x="38" y="10" width="28" height="14" rx="5" fill="${C.blue800}"/>
    </g>
    ${[38, 52, 66].map((y) => `<path d="M33 ${y} l3 3 5-6" stroke="${C.green600}" stroke-width="2.6" fill="none" stroke-linecap="round" stroke-linejoin="round"/><rect x="46" y="${y - 3}" width="24" height="4" rx="2" fill="${C.slate300}"/>`).join('')}
    <circle cx="82" cy="82" r="16" fill="${C.green500}" stroke="#fff" stroke-width="3"/>
    <path d="M75 82 l5 5 9-10" stroke="#fff" stroke-width="3.4" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
    ${glyph('sparkle', { x: 2, y: 6, size: 18, color: C.amber500, width: 2 })}`;
  return svg(104, 104, body, defs);
}

/** Default profile avatar. */
export function avatar() {
  const defs =
    linear('av', [
      [0, C.blue100],
      [1, C.blue200],
    ]) +
    SKIN_DEFS +
    linear('shirt', [
      [0, C.blue500],
      [1, C.blue700],
    ]) +
    '<clipPath id="clip"><circle cx="48" cy="48" r="48"/></clipPath>';
  const body = `
    <circle cx="48" cy="48" r="48" fill="url(#av)"/>
    <g clip-path="url(#clip)">
      <path d="M10 110 C10 80 26 68 48 68 C70 68 86 80 86 110z" fill="url(#shirt)"/>
      <path d="M40 68 L48 80 L56 68z" fill="#fff"/>
      <path d="M37 67 L48 80 L42 84 L33 70z" fill="${C.blue800}"/>
      <path d="M59 67 L48 80 L54 84 L63 70z" fill="${C.blue800}"/>
      <rect x="41.5" y="56" width="13" height="14" rx="5" fill="${C.skinShade}"/>
    </g>
    ${face(48, 41, 15.5)}`;
  return svg(96, 96, body, defs);
}

/** Empty-state illustration. */
export function emptyState() {
  const body = `
    <ellipse cx="80" cy="108" rx="58" ry="7" fill="url(#es)"/>
    <rect x="44" y="22" width="64" height="80" rx="10" fill="#fff" stroke="${C.slate200}" stroke-width="2"/>
    <rect x="54" y="36" width="36" height="6" rx="3" fill="${C.blue200}"/>
    <rect x="54" y="50" width="44" height="5" rx="2.5" fill="${C.slate200}"/>
    <rect x="54" y="62" width="30" height="5" rx="2.5" fill="${C.slate200}"/>
    <circle cx="106" cy="76" r="20" fill="#fff" stroke="${C.blue500}" stroke-width="6"/>
    <path d="M120 90 l14 14" stroke="${C.blue500}" stroke-width="8" stroke-linecap="round"/>
    ${glyph('sparkle', { x: 20, y: 18, size: 20, color: C.amber500, width: 2 })}`;
  return svg(160, 120, body, radial('es', [
    [0, C.slate300, 0.9],
    [1, C.slate200, 0],
  ]));
}

// ---- App icons (PNG; stores require PNG) -----------------------------------------------

export function appIcon() {
  return svg(
    1024,
    1024,
    `<rect width="1024" height="1024" fill="url(#b)"/><circle cx="820" cy="160" r="360" fill="#fff" opacity=".06"/>${briefcaseMark(512, 530, 560, '#fff', C.blue700)}`,
    brandGradient('b'),
  );
}

export function adaptiveForeground() {
  return svg(512, 512, briefcaseMark(256, 262, 230, '#fff', C.blue700));
}

export function adaptiveBackground() {
  return svg(512, 512, `<rect width="512" height="512" fill="url(#b)"/>`, brandGradient('b'));
}

export function adaptiveMonochrome() {
  return svg(432, 432, briefcaseMark(216, 222, 200, '#fff', '#000'));
}

export function splashIcon() {
  return svg(
    400,
    400,
    `<rect x="40" y="40" width="320" height="320" rx="84" fill="#fff"/>${briefcaseMark(200, 208, 180, 'url(#b)')}`,
    brandGradient('b'),
  );
}

export function favicon() {
  return svg(48, 48, `<rect width="48" height="48" rx="12" fill="url(#b)"/>${briefcaseMark(24, 25, 28, '#fff', C.blue700)}`, brandGradient('b'));
}
