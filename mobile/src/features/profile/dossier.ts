import { casteMeta, educationMeta, stageMeta } from '@/constants/catalog';
import type { AppliedExam, DocumentLocker, EligibilityProfile } from '@/types/domain';

function row(label: string, value: string | number | undefined): string | null {
  const v = String(value ?? '').trim();
  return v ? `${label}: ${v}` : null;
}

/** Plain-text summary the user can share or print. Built only when they tap Share. */
export function buildDossier(p: EligibilityProfile, d: DocumentLocker, exams: AppliedExam[]): string {
  const lines: (string | null)[] = [
    'SARKARI ASPIRANT PROFILE',
    '========================',
    row('Name', p.candidateName),
    row("Father's name", d.fatherName),
    row('Date of birth', d.dob),
    row('Age', p.age),
    row('Gender', p.gender === 'FEMALE' ? 'Female' : 'Male'),
    row('Category', casteMeta(p.category).label),
    row('Home state', p.homeState),
    row('Education', educationMeta(p.qualification).label),
    row('Degree', p.specificDegree),
    '',
    'DOCUMENTS',
    '---------',
    row('10th', [d.tenthBoard, d.tenthYear, d.tenthRollNo && `Roll ${d.tenthRollNo}`, d.tenthPercentage].filter(Boolean).join(' · ')),
    row('12th', [d.twelfthBoard, d.twelfthStream, d.twelfthYear, d.twelfthRollNo && `Roll ${d.twelfthRollNo}`, d.twelfthPercentage].filter(Boolean).join(' · ')),
    row('Graduation', [d.gradDegree, d.gradUniversity, d.gradYear, d.gradCgpaOrPercentage].filter(Boolean).join(' · ')),
    row('Category certificate', [d.categoryCertNo, d.categoryIssueAuthority, d.categoryIssueDate].filter(Boolean).join(' · ')),
    row('Domicile', [d.domicileCertNo, d.domicileState].filter(Boolean).join(' · ')),
    row('Photo ID', [d.idType, d.idNumberMasked].filter(Boolean).join(' · ')),
  ];
  if (exams.length > 0) {
    lines.push('', `APPLICATIONS (${exams.length})`, '-----------------');
    exams.forEach((e, i) => {
      lines.push(`${i + 1}. ${e.jobTitle} – ${e.organization}`);
      lines.push(`   ${[e.advtNumber && `Advt ${e.advtNumber}`, e.applicationNo && `App ${e.applicationNo}`, e.rollNo && `Roll ${e.rollNo}`].filter(Boolean).join(' | ')}`);
      lines.push(`   ${stageMeta(e.stage).label}${e.examDate ? ` · Exam ${e.examDate}` : ''}`);
    });
  }
  return lines.filter((l): l is string => l !== null).join('\n');
}
