import type { EligibilityProfile } from '@/types/domain';

/**
 * Physical standards for common police and defence recruitments (general guide). Official notices can differ by state and year.
 */
export type PhysicalStatus = 'PASS' | 'BORDERLINE' | 'BELOW';

export type PhysicalResult = {
  force: string;
  post: string;
  requiredHeightCm: number;
  heightDiffCm: number;
  heightStatus: PhysicalStatus;
  chestRequirement: string | null;
  chestStatus: PhysicalStatus | null;
  runningTest: string;
  overall: PhysicalStatus;
};

type Standard = {
  force: string;
  post: string;
  height: (female: boolean, st: boolean, sc: boolean) => number;
  /** Unexpanded chest minimum (male only), or null when there is no chest test. */
  chest: ((st: boolean) => number) | null;
  running: (female: boolean) => string;
};

const STANDARDS: Standard[] = [
  {
    force: 'UP Police',
    post: 'Constable / Sub-Inspector',
    height: (f, st) => (f ? (st ? 147 : 152) : st ? 160 : 168),
    chest: (st) => (st ? 77 : 79),
    running: (f) => (f ? '2.4 km in 14 min' : '4.8 km in 25 min'),
  },
  {
    force: 'SSC GD Constable',
    post: 'CAPF (BSF, CISF, CRPF, ITBP, SSB)',
    height: (f, st) => (f ? (st ? 150 : 157) : st ? 162.5 : 170),
    chest: (st) => (st ? 76 : 80),
    running: (f) => (f ? '1.6 km in 8.5 min' : '5 km in 24 min'),
  },
  {
    force: 'Indian Army',
    post: 'Agniveer GD / Technical',
    height: (f) => (f ? 152 : 169),
    chest: () => 77,
    running: () => '1.6 km (Group 1 ≤ 5:30) + pull-ups',
  },
  {
    force: 'UPSC NDA & CDS',
    post: 'Officer entry (Army)',
    height: (f) => (f ? 152 : 157),
    chest: null,
    running: () => '2.4 km in 15 min',
  },
  {
    force: 'RPF (Railways)',
    post: 'Constable / Sub-Inspector',
    height: (f, st, sc) => (f ? (st || sc ? 152 : 157) : st || sc ? 160 : 165),
    chest: (st) => (st ? 76.2 : 80),
    running: (f) => (f ? '800 m in 3:40 + long jump' : '1600 m in 5:45 + long jump'),
  },
];

function grade(diff: number): PhysicalStatus {
  if (diff >= 0) return 'PASS';
  if (diff >= -1) return 'BORDERLINE';
  return 'BELOW';
}

function combine(a: PhysicalStatus, b: PhysicalStatus | null): PhysicalStatus {
  if (b === null) return a;
  if (a === 'BELOW' || b === 'BELOW') return 'BELOW';
  if (a === 'PASS' && b === 'PASS') return 'PASS';
  return 'BORDERLINE';
}

export function evaluatePhysical(p: EligibilityProfile): PhysicalResult[] {
  const female = p.gender === 'FEMALE';
  const st = p.category === 'ST';
  const sc = p.category === 'SC';
  return STANDARDS.map((s) => {
    const required = s.height(female, st, sc);
    const diff = Math.round((p.heightCm - required) * 10) / 10;
    const heightStatus = grade(diff);
    let chestStatus: PhysicalStatus | null = null;
    let chestRequirement: string | null = null;
    if (!female && s.chest) {
      const min = s.chest(st);
      chestRequirement = `${min} cm (+5 cm expansion)`;
      const expansion = p.chestExpandedCm - p.chestUnexpandedCm;
      chestStatus = p.chestUnexpandedCm >= min && expansion >= 5 ? 'PASS' : p.chestUnexpandedCm >= min - 1 ? 'BORDERLINE' : 'BELOW';
    }
    return {
      force: s.force,
      post: s.post,
      requiredHeightCm: required,
      heightDiffCm: diff,
      heightStatus,
      chestRequirement,
      chestStatus,
      runningTest: s.running(female),
      overall: combine(heightStatus, chestStatus),
    };
  });
}
