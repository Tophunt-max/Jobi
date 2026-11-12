import type { ImageSourcePropType } from 'react-native';

import { images } from '@/assets/images';

/** Brand moment on the very first launch; returning users get to their content quickly. */
const SPLASH_FIRST_LAUNCH_MS = 1500;
const SPLASH_RETURNING_MS = 700;

export function splashMinVisibleMs(returningUser: boolean): number {
  return returningUser ? SPLASH_RETURNING_MS : SPLASH_FIRST_LAUNCH_MS;
}

export type OnboardingSlide = {
  key: string;
  image: ImageSourcePropType;
  /** i18n keys */
  title: string;
  body: string;
};

export const ONBOARDING_SLIDES: readonly OnboardingSlide[] = [
  { key: 'jobs', image: images.onboarding.jobs, title: 'onboarding.title1', body: 'onboarding.body1' },
  { key: 'eligibility', image: images.onboarding.eligibility, title: 'onboarding.title2', body: 'onboarding.body2' },
  { key: 'alerts', image: images.onboarding.alerts, title: 'onboarding.title3', body: 'onboarding.body3' },
];

/** Slide index for a horizontal scroll offset, clamped to the slide range. */
export function slideIndexForOffset(offsetX: number, pageWidth: number, count = ONBOARDING_SLIDES.length): number {
  if (!(pageWidth > 0) || !Number.isFinite(offsetX)) return 0;
  return Math.min(count - 1, Math.max(0, Math.round(offsetX / pageWidth)));
}

/** Illustration size that leaves room for the text and button on short screens. */
export function onboardingArtSize(windowHeight: number): number {
  return Math.round(Math.min(280, Math.max(170, windowHeight * 0.34)));
}
