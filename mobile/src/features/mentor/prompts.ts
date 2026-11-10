import { casteMeta, educationMeta } from '@/constants/catalog';
import type { EligibilityProfile, Job } from '@/types/domain';

/**
 * Prompts sent to the Worker's Gemini proxy. Only eligibility-relevant fields are sent;
 * the candidate's name and document numbers never leave the device.
 */
function profileLines(p: EligibilityProfile): string[] {
  const cat = casteMeta(p.category);
  return [
    `- Age: ${p.age} years`,
    `- Gender: ${p.gender === 'FEMALE' ? 'Female' : 'Male'}`,
    `- Category: ${cat.label} (standard age relaxation ${cat.relaxationYears} years)`,
    `- Qualification: ${educationMeta(p.qualification).label}${p.specificDegree ? ` (${p.specificDegree})` : ''}`,
    `- Home state: ${p.homeState}`,
    `- PwD: ${p.isPwD ? 'Yes' : 'No'}`,
    `- Ex-servicemen: ${p.isExServicemen ? 'Yes' : 'No'}`,
  ];
}

function jobLines(j: Job): string[] {
  return [
    `- Title: ${j.title}`,
    `- Organisation: ${j.organization}`,
    `- Advt no: ${j.advtNumber || 'n/a'}`,
    `- Scope: ${j.state}`,
    `- Total posts: ${j.totalPosts || 'n/a'}`,
    `- Age limit (UR): ${j.minAge || '?'} to ${j.maxAge || '?'}`,
    `- Qualification: ${j.qualification}`,
    `- Selection process: ${j.selectionProcess}`,
    `- Fee: General ${j.feeGeneral}; Reserved ${j.feeReserved}`,
    `- Last date: ${j.lastDate || 'n/a'}`,
  ];
}

export function eligibilityPrompt(profile: EligibilityProfile, job: Job): string {
  return [
    'Evaluate this candidate\'s eligibility for the government job below.',
    '',
    'CANDIDATE:',
    ...profileLines(profile),
    '',
    'JOB:',
    ...jobLines(job),
    '',
    'Answer with these sections:',
    '1. Verdict: ELIGIBLE, NOT ELIGIBLE or CHECK REQUIRED.',
    '2. Age: effective upper limit with category relaxation, with the arithmetic.',
    '3. Education: does the qualification match?',
    '4. Domicile and reservation: does the home-state quota apply?',
    '5. Fee and documents to keep ready.',
    'Use short headings and bullet points for a phone screen.',
  ].join('\n');
}

export function strategyPrompt(examName: string, job?: Job | null): string {
  return [
    `Give a focused preparation strategy for: ${examName}.`,
    ...(job ? [`Recruiting body: ${job.organization}`, `Selection stages: ${job.selectionProcess}`] : []),
    '',
    'Sections:',
    '1. Exam pattern and time management, including negative marking.',
    '2. Five highest-weightage topics per section.',
    '3. A 90-day plan in three phases.',
    '4. Official resources and previous-year papers to use.',
  ].join('\n');
}

export function queryPrompt(question: string, profile: EligibilityProfile): string {
  return [
    'Candidate profile:',
    ...profileLines(profile),
    '',
    'Question:',
    question.trim(),
    '',
    'Answer factually using official DoPT, UPSC, SSC, RRB and IBPS rules where relevant.',
  ].join('\n');
}
