import type { CasteCategory, EducationLevel, ExamStage } from '@/types/domain';

export const ALL = 'ALL';

export const INDIAN_STATES = [
  'All India',
  'Uttar Pradesh',
  'Bihar',
  'Rajasthan',
  'Madhya Pradesh',
  'Maharashtra',
  'Delhi',
  'West Bengal',
  'Odisha',
  'Gujarat',
  'Tamil Nadu',
  'Andhra Pradesh',
  'Telangana',
  'Karnataka',
  'Kerala',
  'Punjab',
  'Haryana',
  'Jharkhand',
  'Chhattisgarh',
  'Uttarakhand',
  'Himachal Pradesh',
  'Assam',
  'Jammu & Kashmir',
] as const;

/** Sector filters. Each matches on category, organisation and title keywords. */
export const SECTORS: { key: string; icon: string; keywords: string[] }[] = [
  { key: 'ssc', icon: '📝', keywords: ['ssc', 'staff selection'] },
  { key: 'upsc', icon: '🏛️', keywords: ['upsc', 'union public service'] },
  { key: 'banking', icon: '🏦', keywords: ['bank', 'ibps', 'sbi', 'rbi', 'nabard', 'insurance', 'lic'] },
  { key: 'railways', icon: '🚆', keywords: ['railway', 'rrb', 'rpf', 'metro'] },
  { key: 'defence', icon: '🛡️', keywords: ['army', 'navy', 'air force', 'iaf', 'defence', 'nda', 'cds', 'agniveer', 'capf', 'itbp', 'bsf', 'crpf', 'cisf', 'drdo'] },
  { key: 'police', icon: '👮', keywords: ['police', 'constable', 'sub-inspector', 'sub inspector'] },
  { key: 'teaching', icon: '📚', keywords: ['teacher', 'teaching', 'tet', 'kvs', 'nvs', 'dsssb', 'lecturer', 'professor'] },
  { key: 'engineering', icon: '⚙️', keywords: ['engineer', 'je', 'junior engineer', 'isro', 'scientist', 'technical', 'optcl', 'ohpc'] },
  { key: 'medical', icon: '🩺', keywords: ['medical', 'nurse', 'nursing', 'health', 'doctor', 'pharmacist', 'aiims'] },
  { key: 'statePsc', icon: '🏢', keywords: ['psc', 'public service commission', 'opsc', 'ossc', 'osssc'] },
];

export const QUALIFICATIONS = [
  '10th',
  '12th',
  'ITI',
  'Diploma',
  'Graduate',
  'B.Tech',
  'Post Graduate',
] as const;

export const CASTE_CATEGORIES: { key: CasteCategory; label: string; relaxationYears: number; feeExempt: boolean }[] = [
  { key: 'GENERAL', label: 'General (UR)', relaxationYears: 0, feeExempt: false },
  { key: 'EWS', label: 'EWS', relaxationYears: 0, feeExempt: false },
  { key: 'OBC_NCL', label: 'OBC (Non-Creamy Layer)', relaxationYears: 3, feeExempt: false },
  { key: 'SC', label: 'Scheduled Caste (SC)', relaxationYears: 5, feeExempt: true },
  { key: 'ST', label: 'Scheduled Tribe (ST)', relaxationYears: 5, feeExempt: true },
  { key: 'PWD_GENERAL', label: 'PwD (General/EWS)', relaxationYears: 10, feeExempt: true },
  { key: 'PWD_OBC', label: 'PwD (OBC-NCL)', relaxationYears: 13, feeExempt: true },
  { key: 'PWD_SC_ST', label: 'PwD (SC/ST)', relaxationYears: 15, feeExempt: true },
  { key: 'EX_SERVICEMEN', label: 'Ex-Servicemen', relaxationYears: 3, feeExempt: true },
];

export const EDUCATION_LEVELS: { key: EducationLevel; label: string; rank: number }[] = [
  { key: 'TENTH_PASS', label: '10th Pass', rank: 1 },
  { key: 'TWELFTH_PASS', label: '12th Pass', rank: 2 },
  { key: 'ITI', label: 'ITI', rank: 2 },
  { key: 'DIPLOMA', label: 'Diploma', rank: 3 },
  { key: 'GRADUATE', label: 'Graduate', rank: 4 },
  { key: 'BTECH', label: 'B.Tech / B.E.', rank: 4 },
  { key: 'LLB', label: 'LL.B.', rank: 4 },
  { key: 'MBBS_MEDICAL', label: 'Medical / Nursing', rank: 4 },
  { key: 'POST_GRADUATE', label: 'Post Graduate', rank: 5 },
];

export const EXAM_STAGES: { key: ExamStage; label: string; color: string }[] = [
  { key: 'SUBMITTED', label: 'Application submitted', color: '#2563EB' },
  { key: 'ADMIT_CARD_OUT', label: 'Admit card out', color: '#EA580C' },
  { key: 'EXAM_SCHEDULED', label: 'Exam scheduled', color: '#7C3AED' },
  { key: 'RESULT_AWAITED', label: 'Result awaited', color: '#0284C7' },
  { key: 'SELECTED', label: 'Selected', color: '#059669' },
  { key: 'REJECTED', label: 'Not qualified', color: '#E11D48' },
];

export const REPORT_REASONS = [
  'Wrong last date',
  'Wrong eligibility or age limit',
  'Broken or wrong link',
  'Notice is fake or not official',
  'Duplicate notice',
  'Other',
] as const;

export const VACANCY_THRESHOLDS = [0, 100, 500, 1000, 5000] as const;

export function casteMeta(key: CasteCategory) {
  return CASTE_CATEGORIES.find((c) => c.key === key) ?? CASTE_CATEGORIES[0]!;
}

export function educationMeta(key: EducationLevel) {
  return EDUCATION_LEVELS.find((e) => e.key === key) ?? EDUCATION_LEVELS[4]!;
}

export function stageMeta(key: ExamStage) {
  return EXAM_STAGES.find((s) => s.key === key) ?? EXAM_STAGES[0]!;
}
