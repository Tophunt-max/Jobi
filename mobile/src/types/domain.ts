/** App-side models. API DTOs are mapped into these in services/api/mappers.ts. */

export type JobStatus = 'ACTIVE' | 'CLOSING_SOON' | 'UPCOMING' | 'EXPIRED';

export interface Job {
  id: string;
  slug: string;
  title: string;
  organization: string;
  department: string;
  postName: string;
  advtNumber: string;
  /** "All India" or a state name. */
  state: string;
  category: string;
  totalPosts: number;
  qualification: string;
  minAge: number;
  maxAge: number;
  salaryScale: string;
  feeGeneral: string;
  feeReserved: string;
  selectionProcess: string;
  /** ISO YYYY-MM-DD, or "" when unknown. */
  startDate: string;
  /** ISO YYYY-MM-DD, or "" when unknown. */
  lastDate: string;
  /** Exam / admit-card dates are often free text ("Tentative: Dec 2026"). */
  examDate: string;
  admitCardDate: string;
  notificationUrl: string;
  applyUrl: string;
  websiteUrl: string;
  sourceDomain: string;
  verified: boolean;
  featured: boolean;
  /** Epoch ms. */
  publishedAt: number;
}

export interface ResultItem {
  id: string;
  examName: string;
  recruitingOrg: string;
  advtNumber: string;
  publicationDate: string;
  url: string;
  cutOffSummary: string;
  meritListAvailable: boolean;
  verificationDomain: string;
  stage: string;
}

export interface AdmitCardItem {
  id: string;
  examName: string;
  recruitingOrg: string;
  releaseDate: string;
  examDate: string;
  url: string;
  instructions: string;
  citySlipReleased: boolean;
  verificationDomain: string;
}

export interface SyllabusItem {
  id: string;
  examName: string;
  recruitingOrg: string;
  examPattern: string;
  subjects: { name: string; marks: string }[];
  durationMinutes: number;
  negativeMarkingRule: string;
  url: string;
  verificationDomain: string;
}

export interface AdmissionItem {
  id: string;
  name: string;
  authority: string;
  eligibility: string;
  startDate: string;
  closingDate: string;
  examDate: string;
  applyUrl: string;
  bulletinUrl: string;
  verificationDomain: string;
  status: string;
}

export interface AnswerKeyItem {
  id: string;
  examName: string;
  recruitingOrg: string;
  status: string;
  releaseDate: string;
  objectionDeadline: string;
  objectionFee: string;
  url: string;
  verificationDomain: string;
}

export type NotificationType =
  | 'NEW_JOB'
  | 'DEADLINE_ALERT'
  | 'RESULT'
  | 'ADMIT_CARD'
  | 'SYLLABUS'
  | 'ANSWER_KEY'
  | string;

export interface AppNotification {
  id: string;
  title: string;
  body: string;
  type: NotificationType;
  /** Job id or slug the notification points to. */
  target: string;
  timestamp: number;
  priority: string;
}

export interface ContentSnapshot {
  jobs: Job[];
  results: ResultItem[];
  admitCards: AdmitCardItem[];
  syllabi: SyllabusItem[];
  admissions: AdmissionItem[];
  answerKeys: AnswerKeyItem[];
  notifications: AppNotification[];
  serverTimestamp: number;
}

// ---------------------------------------------------------------------------
// User data (device only)
// ---------------------------------------------------------------------------

export type CasteCategory =
  | 'GENERAL'
  | 'EWS'
  | 'OBC_NCL'
  | 'SC'
  | 'ST'
  | 'PWD_GENERAL'
  | 'PWD_OBC'
  | 'PWD_SC_ST'
  | 'EX_SERVICEMEN';

export type EducationLevel =
  | 'TENTH_PASS'
  | 'TWELFTH_PASS'
  | 'ITI'
  | 'DIPLOMA'
  | 'GRADUATE'
  | 'BTECH'
  | 'POST_GRADUATE'
  | 'LLB'
  | 'MBBS_MEDICAL';

export type Gender = 'MALE' | 'FEMALE';

export interface EligibilityProfile {
  candidateName: string;
  age: number;
  gender: Gender;
  category: CasteCategory;
  qualification: EducationLevel;
  specificDegree: string;
  homeState: string;
  isPwD: boolean;
  isExServicemen: boolean;
  heightCm: number;
  chestUnexpandedCm: number;
  chestExpandedCm: number;
}

export interface DocumentLocker {
  fatherName: string;
  dob: string;
  tenthBoard: string;
  tenthRollNo: string;
  tenthYear: string;
  tenthPercentage: string;
  twelfthBoard: string;
  twelfthStream: string;
  twelfthRollNo: string;
  twelfthYear: string;
  twelfthPercentage: string;
  gradDegree: string;
  gradUniversity: string;
  gradRollNo: string;
  gradYear: string;
  gradCgpaOrPercentage: string;
  categoryCertNo: string;
  categoryIssueAuthority: string;
  categoryIssueDate: string;
  domicileCertNo: string;
  domicileState: string;
  idType: string;
  idNumberMasked: string;
}

export type ExamStage =
  | 'SUBMITTED'
  | 'ADMIT_CARD_OUT'
  | 'EXAM_SCHEDULED'
  | 'RESULT_AWAITED'
  | 'SELECTED'
  | 'REJECTED';

export interface AppliedExam {
  id: string;
  jobTitle: string;
  organization: string;
  advtNumber: string;
  applicationNo: string;
  rollNo: string;
  examDate: string;
  examCenter: string;
  stage: ExamStage;
  appliedDate: string;
  notes: string;
}

export interface AlertPreferences {
  enabled: boolean;
  states: string[];
  categories: string[];
  qualifications: string[];
  minVacancies: number;
}
