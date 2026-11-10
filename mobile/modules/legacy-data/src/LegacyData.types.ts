/** Raw payload returned by LegacyData.readAsync(), keyed as in the Kotlin app. */
export type LegacyPayload = {
  eligibility_profile_v1?: string;
  document_locker_v1?: string;
  applied_exams_v1?: string;
  alert_prefs?: Record<string, unknown>;
  bookmarked_jobs?: Record<string, unknown>[];
  read_notification_ids?: string[];
};
