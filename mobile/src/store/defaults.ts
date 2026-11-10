import type { AlertPreferences, DocumentLocker, EligibilityProfile } from '@/types/domain';

export const DEFAULT_PROFILE: EligibilityProfile = {
  candidateName: '',
  age: 23,
  gender: 'MALE',
  category: 'GENERAL',
  qualification: 'GRADUATE',
  specificDegree: '',
  homeState: 'All India',
  isPwD: false,
  isExServicemen: false,
  heightCm: 170,
  chestUnexpandedCm: 80,
  chestExpandedCm: 85,
};

export const EMPTY_LOCKER: DocumentLocker = {
  fatherName: '',
  dob: '',
  tenthBoard: '',
  tenthRollNo: '',
  tenthYear: '',
  tenthPercentage: '',
  twelfthBoard: '',
  twelfthStream: '',
  twelfthRollNo: '',
  twelfthYear: '',
  twelfthPercentage: '',
  gradDegree: '',
  gradUniversity: '',
  gradRollNo: '',
  gradYear: '',
  gradCgpaOrPercentage: '',
  categoryCertNo: '',
  categoryIssueAuthority: '',
  categoryIssueDate: '',
  domicileCertNo: '',
  domicileState: '',
  idType: '',
  idNumberMasked: '',
};

export const DEFAULT_ALERT_PREFS: AlertPreferences = {
  enabled: true,
  states: ['All India'],
  categories: [],
  qualifications: [],
  minVacancies: 0,
};
