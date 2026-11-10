import { casteMeta, educationMeta } from '@/constants/catalog';
import { checkAge, isFeeExempt, meetsEducation } from '@/features/profile/eligibility';
import type { EligibilityProfile, Job } from '@/types/domain';

/**
 * Rule-based answers used when the AI service is unreachable.
 * They are clearly labelled as offline estimates in the UI.
 */
export function offlineEligibility(profile: EligibilityProfile, job: Job): string {
  const cat = casteMeta(profile.category);
  const age = checkAge(job, profile);
  const edu = meetsEducation(job, profile);

  const verdict =
    age.kind === 'too_old' || age.kind === 'too_young' || edu === false
      ? 'NOT ELIGIBLE (check the notice)'
      : age.kind === 'unknown' || edu === null
        ? 'CHECK REQUIRED'
        : 'LIKELY ELIGIBLE';

  const lines = [
    `Verdict: ${verdict}`,
    '',
    'Age',
    job.maxAge > 0
      ? `• UR limit: ${job.minAge || '–'} to ${job.maxAge} years`
      : '• The age limit is not available; read the official notice.',
    `• Your category: ${cat.label} (+${cat.relaxationYears} years)`,
  ];
  if (age.kind === 'eligible') lines.push(`• Your limit: ${age.upperLimit} years. You are ${profile.age}.`);
  if (age.kind === 'too_old') lines.push(`• Your limit: ${age.upperLimit} years. You are ${profile.age}, which is above it.`);
  if (age.kind === 'too_young') lines.push(`• Minimum age is ${age.minAge}. You are ${profile.age}.`);

  lines.push(
    '',
    'Education',
    `• Required: ${job.qualification}`,
    `• Yours: ${educationMeta(profile.qualification).label}`,
    edu === null ? '• Could not compare automatically.' : edu ? '• Your level meets the requirement.' : '• Your level looks lower than required.',
    '',
    'Fee',
    isFeeExempt(profile) ? '• You are usually exempt from the fee.' : `• General fee: ${job.feeGeneral}`,
    '',
    'Documents',
    '• 10th certificate (date of birth proof)',
    '• Degree / marksheets',
    '• Category certificate valid on the closing date',
    profile.category === 'OBC_NCL' ? '• OBC-NCL certificate for the current financial year' : '',
    profile.category === 'EWS' ? '• EWS income and asset certificate for the current year' : '',
  );
  return lines.filter((l) => l !== '').join('\n');
}

export function offlineStrategy(examName: string): string {
  return [
    `Preparation plan: ${examName}`,
    '',
    'Pattern',
    '• Prelims / Tier 1: speed and accuracy (objective)',
    '• Mains / Tier 2: depth and analysis',
    '• Then document verification, medical or skill test',
    '',
    'High-weightage topics',
    '• Quant: percentage, ratio, interest, time and work, data interpretation',
    '• Reasoning: syllogism, coding-decoding, seating arrangement, puzzles',
    '• General awareness: polity, modern history, geography, last 6 months of current affairs',
    '• English: reading comprehension, error spotting, cloze test',
    '',
    '90-day plan',
    '• Days 1–30: cover the full syllabus and make short notes',
    '• Days 31–60: topic-wise previous-year questions',
    '• Days 61–90: two full mocks a week with error analysis',
    '',
    'Negative marking is usually 0.25 to 0.50 per wrong answer, so accuracy matters more than attempts.',
  ].join('\n');
}

export function offlineQuery(): string {
  return [
    'The AI mentor is offline right now.',
    '',
    '• Reservation and age relaxation follow DoPT rules for central jobs and the state PSC rules for state jobs.',
    '• Category, EWS and PwD certificates must be valid on the date given in the notice.',
    '• Always confirm details in the official notification PDF.',
    '',
    'Try again when you are online for a detailed answer.',
  ].join('\n');
}
