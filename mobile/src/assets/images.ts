/**
 * Every bundled image, grouped like `assets/images/`. Regenerate the files with `npm run assets`.
 * Metro needs static `require` calls, so the map is written out by hand.
 */
import type { ImageSourcePropType } from 'react-native';

export const images = {
  brand: {
    logoMark: require('../../assets/images/brand/logo-mark.webp'),
  },
  /** Background is drawn in code; the other pieces are converted from assets/source/splash. */
  splash: {
    background: require('../../assets/images/splash/background.webp'),
    logo: require('../../assets/images/splash/logo.webp'),
    laurelLeft: require('../../assets/images/splash/laurel-left.webp'),
    laurelRight: require('../../assets/images/splash/laurel-right.webp'),
    building: require('../../assets/images/splash/government-building.webp'),
    wave: require('../../assets/images/splash/tricolor-wave.webp'),
  },
  onboarding: {
    jobs: require('../../assets/images/onboarding/jobs.webp'),
    eligibility: require('../../assets/images/onboarding/eligibility.webp'),
    alerts: require('../../assets/images/onboarding/alerts.webp'),
  },
  home: {
    header: require('../../assets/images/home/header.webp'),
    hero: require('../../assets/images/home/hero.webp'),
  },
  quick: {
    latestJobs: require('../../assets/images/quick/latest-jobs.webp'),
    results: require('../../assets/images/quick/results.webp'),
    admitCards: require('../../assets/images/quick/admit-cards.webp'),
    answerKey: require('../../assets/images/quick/answer-key.webp'),
    syllabus: require('../../assets/images/quick/syllabus.webp'),
    examCalendar: require('../../assets/images/quick/exam-calendar.webp'),
    eligibility: require('../../assets/images/quick/eligibility.webp'),
    admissions: require('../../assets/images/quick/admissions.webp'),
  },
  content: {
    result: require('../../assets/images/content/result.webp'),
    admitCard: require('../../assets/images/content/admit-card.webp'),
    pdf: require('../../assets/images/content/pdf.webp'),
    answerKey: require('../../assets/images/content/answer-key.webp'),
    admission: require('../../assets/images/content/admission.webp'),
    calendar: require('../../assets/images/content/calendar.webp'),
    download: require('../../assets/images/content/download.webp'),
  },
  notifications: {
    newJob: require('../../assets/images/notifications/new-job.webp'),
    admitCard: require('../../assets/images/notifications/admit-card.webp'),
    result: require('../../assets/images/notifications/result.webp'),
    deadline: require('../../assets/images/notifications/deadline.webp'),
    general: require('../../assets/images/notifications/general.webp'),
  },
  ai: {
    robot: require('../../assets/images/ai/robot.webp'),
  },
  eligibility: {
    checklist: require('../../assets/images/eligibility/checklist.webp'),
  },
  profile: {
    avatar: require('../../assets/images/profile/avatar.webp'),
  },
  states: {
    empty: require('../../assets/images/states/empty.webp'),
  },
} satisfies Record<string, Record<string, ImageSourcePropType>>;

/**
 * Full-colour list icons, one per tone, in the same order as LIST_TINTS
 * (red, green, purple, blue, orange) so rows alternate like the mockup.
 */
export const listArt = {
  results: [
    require('../../assets/images/lists/results/red.webp'),
    require('../../assets/images/lists/results/green.webp'),
    require('../../assets/images/lists/results/purple.webp'),
    require('../../assets/images/lists/results/blue.webp'),
    require('../../assets/images/lists/results/orange.webp'),
  ],
  admitCards: [
    require('../../assets/images/lists/admit-cards/red.webp'),
    require('../../assets/images/lists/admit-cards/green.webp'),
    require('../../assets/images/lists/admit-cards/purple.webp'),
    require('../../assets/images/lists/admit-cards/blue.webp'),
    require('../../assets/images/lists/admit-cards/orange.webp'),
  ],
  syllabus: [
    require('../../assets/images/lists/syllabus/red.webp'),
    require('../../assets/images/lists/syllabus/green.webp'),
    require('../../assets/images/lists/syllabus/purple.webp'),
    require('../../assets/images/lists/syllabus/blue.webp'),
    require('../../assets/images/lists/syllabus/orange.webp'),
  ],
} satisfies Record<string, ImageSourcePropType[]>;

export type ListArtKind = keyof typeof listArt;

export type ArtKey =
  | 'central-govt'
  | 'state-govt'
  | 'railway'
  | 'banking'
  | 'defence'
  | 'police'
  | 'teaching'
  | 'medical'
  | 'psu'
  | 'other'
  | 'ssc'
  | 'upsc'
  | 'engineering';

export const categoryArt: Record<ArtKey, ImageSourcePropType> = {
  'central-govt': require('../../assets/images/categories/central-govt.webp'),
  'state-govt': require('../../assets/images/categories/state-govt.webp'),
  railway: require('../../assets/images/categories/railway.webp'),
  banking: require('../../assets/images/categories/banking.webp'),
  defence: require('../../assets/images/categories/defence.webp'),
  police: require('../../assets/images/categories/police.webp'),
  teaching: require('../../assets/images/categories/teaching.webp'),
  medical: require('../../assets/images/categories/medical.webp'),
  psu: require('../../assets/images/categories/psu.webp'),
  other: require('../../assets/images/categories/other.webp'),
  ssc: require('../../assets/images/categories/ssc.webp'),
  upsc: require('../../assets/images/categories/upsc.webp'),
  engineering: require('../../assets/images/categories/engineering.webp'),
};

export const emblemArt: Record<ArtKey, ImageSourcePropType> = {
  'central-govt': require('../../assets/images/emblems/central-govt.webp'),
  'state-govt': require('../../assets/images/emblems/state-govt.webp'),
  railway: require('../../assets/images/emblems/railway.webp'),
  banking: require('../../assets/images/emblems/banking.webp'),
  defence: require('../../assets/images/emblems/defence.webp'),
  police: require('../../assets/images/emblems/police.webp'),
  teaching: require('../../assets/images/emblems/teaching.webp'),
  medical: require('../../assets/images/emblems/medical.webp'),
  psu: require('../../assets/images/emblems/psu.webp'),
  other: require('../../assets/images/emblems/other.webp'),
  ssc: require('../../assets/images/emblems/ssc.webp'),
  upsc: require('../../assets/images/emblems/upsc.webp'),
  engineering: require('../../assets/images/emblems/engineering.webp'),
};
