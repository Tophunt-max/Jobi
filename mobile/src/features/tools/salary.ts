/**
 * 7th CPC entry-pay estimate. DA changes twice a year; update DA_RATE when the
 * government announces a revision (and when the 8th CPC pay matrix is notified).
 */
export const DA_RATE = 0.58;
export const DA_LABEL = '58%';

export type PayLevel = { level: number; basic: number; posts: string };

export const PAY_LEVELS: PayLevel[] = [
  { level: 1, basic: 18000, posts: 'MTS, Group D, Peon' },
  { level: 2, basic: 19900, posts: 'LDC, Junior Clerk' },
  { level: 3, basic: 21700, posts: 'Constable (CAPF), Postal staff' },
  { level: 4, basic: 25500, posts: 'UDC, Head Constable, Postal Assistant' },
  { level: 5, basic: 29200, posts: 'Senior Clerk, Auditor' },
  { level: 6, basic: 35400, posts: 'Sub-Inspector, Junior Engineer' },
  { level: 7, basic: 44900, posts: 'ASO, Inspector (Income Tax / GST)' },
  { level: 8, basic: 47600, posts: 'Assistant Audit Officer' },
  { level: 10, basic: 56100, posts: 'IAS, IPS, IRS, Assistant Director' },
];

export type CityTier = 'X' | 'Y' | 'Z';
export const HRA_RATE: Record<CityTier, number> = { X: 0.3, Y: 0.2, Z: 0.1 };

export type SalaryBreakdown = {
  basic: number;
  da: number;
  hra: number;
  ta: number;
  gross: number;
  nps: number;
  professionalTax: number;
  cghs: number;
  net: number;
};

function transportAllowance(level: number, tier: CityTier): number {
  // TPTA cities (X) pay more.
  if (level >= 9) return tier === 'X' ? 7200 : 3600;
  if (level >= 3) return tier === 'X' ? 3600 : 1800;
  return tier === 'X' ? 1350 : 900;
}

export function calculateSalary(levelInfo: PayLevel, tier: CityTier): SalaryBreakdown {
  const basic = levelInfo.basic;
  const da = Math.round(basic * DA_RATE);
  const hra = Math.round(basic * HRA_RATE[tier]);
  const baseTa = transportAllowance(levelInfo.level, tier);
  const ta = baseTa + Math.round(baseTa * DA_RATE);
  const gross = basic + da + hra + ta;
  const nps = Math.round((basic + da) * 0.1);
  const professionalTax = 200;
  const cghs = levelInfo.level >= 12 ? 1000 : levelInfo.level >= 7 ? 650 : levelInfo.level >= 4 ? 450 : 250;
  return { basic, da, hra, ta, gross, nps, professionalTax, cghs, net: gross - nps - professionalTax - cghs };
}
