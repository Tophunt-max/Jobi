import { Ionicons } from '@expo/vector-icons';
import { useLocalSearchParams } from 'expo-router';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Image, Pressable, ScrollView, StyleSheet, View } from 'react-native';

import { images } from '@/assets/images';
import { DateField, FormSelect, Page, ScreenHeader, Surface, Text, formatDob } from '@/components/ui';
import { CASTE_CATEGORIES, casteMeta, EDUCATION_LEVELS, educationMeta } from '@/constants/catalog';
import { useOpenJobs } from '@/features/jobs/hooks';
import { checkAge, meetsEducation } from '@/features/profile/eligibility';
import { ageOn, parseDate } from '@/lib/dates';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';
import type { CasteCategory, EducationLevel, Job } from '@/types/domain';

type Verdict = {
  eligible: boolean | null;
  age: number;
  cutoff: Date;
  required: string;
  relaxation: number;
  education: boolean | null;
  qualificationLabel: string;
  ageKind: ReturnType<typeof checkAge>['kind'];
};

export default function EligibilityScreen() {
  const { t } = useTranslation();
  const { job: jobParam } = useLocalSearchParams<{ job?: string }>();
  const { colors, shadows } = useTheme();
  const jobs = useOpenJobs();
  const profile = useUserStore((s) => s.profile);
  const locker = useUserStore((s) => s.locker);

  const [jobId, setJobId] = useState<string>(jobParam ?? '');
  const [qualification, setQualification] = useState<EducationLevel>(profile.qualification);
  const [category, setCategory] = useState<CasteCategory | ''>(profile.category);
  const [dob, setDob] = useState<Date | null>(() => parseDate(locker.dob));
  const [verdict, setVerdict] = useState<Verdict | null>(null);

  const job: Job | undefined = jobs.find((j) => j.id === jobId);
  const jobOptions = useMemo(() => jobs.map((j) => ({ value: j.id, label: j.title, subtitle: j.organization })), [jobs]);

  const run = () => {
    if (!job || !dob) return;
    // Age is counted on the last date of the notice, or today when the notice has none.
    const cutoff = parseDate(job.lastDate) ?? new Date();
    const age = ageOn(dob, cutoff).years;
    const candidate = { ...profile, age, qualification, category: category || 'GENERAL' };
    const ageCheck = checkAge(job, candidate);
    const education = meetsEducation(job, candidate);
    const relaxation = casteMeta(candidate.category).relaxationYears;
    const ageOk = ageCheck.kind === 'eligible' ? true : ageCheck.kind === 'unknown' ? null : false;
    const eligible = ageOk === false || education === false ? false : ageOk === null || education === null ? null : true;
    setVerdict({
      eligible,
      age,
      cutoff,
      relaxation,
      required: job.maxAge > 0 ? t('job.ageRangeShort', { min: job.minAge || '–', max: job.maxAge + relaxation }) : t('eligibility.notListed'),
      education,
      qualificationLabel: educationMeta(qualification).label,
      ageKind: ageCheck.kind,
    });
  };

  const tone = verdict?.eligible === true ? 'success' : verdict?.eligible === false ? 'danger' : 'warning';
  const toneBg = { success: colors.successSoft, danger: colors.dangerSoft, warning: colors.warningSoft }[tone];
  const toneFg = { success: colors.success, danger: colors.danger, warning: colors.warning }[tone];

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader title={t('eligibility.title')} />
      <ScrollView contentContainerStyle={{ paddingVertical: 16, paddingBottom: 40 }} keyboardShouldPersistTaps="handled">
        <Page style={{ gap: 16 }}>
          <View style={styles.hero}>
            <Image source={images.eligibility.checklist} style={styles.art} resizeMode="contain" accessibilityIgnoresInvertColors />
            <Text weight="900" align="center" style={{ fontSize: 17, lineHeight: 23 }}>
              {t('eligibility.heading')}
            </Text>
          </View>

          <Surface style={{ gap: 14 }}>
            <FormSelect
              label={t('eligibility.selectJob')}
              value={jobId}
              options={jobOptions}
              onChange={(v) => {
                setJobId(v);
                setVerdict(null);
              }}
              placeholder={t('eligibility.selectJobPlaceholder')}
              searchable
            />
            <FormSelect<EducationLevel>
              label={t('eligibility.qualification')}
              value={qualification}
              options={EDUCATION_LEVELS.map((e) => ({ value: e.key, label: e.label }))}
              onChange={(v) => {
                setQualification(v);
                setVerdict(null);
              }}
            />
            <DateField
              label={t('eligibility.dob')}
              value={dob}
              onChange={(d) => {
                setDob(d);
                setVerdict(null);
              }}
            />
            <FormSelect<CasteCategory>
              label={t('eligibility.category')}
              value={category}
              options={CASTE_CATEGORIES.map((c) => ({ value: c.key, label: c.label, subtitle: c.relaxationYears ? t('eligibility.relaxation', { count: c.relaxationYears }) : undefined }))}
              onChange={(v) => {
                setCategory(v);
                setVerdict(null);
              }}
            />
            <Pressable
              accessibilityRole="button"
              disabled={!job || !dob}
              onPress={run}
              style={({ pressed }) => [styles.cta, { backgroundColor: colors.primary, boxShadow: shadows.raised, opacity: !job || !dob ? 0.5 : pressed ? 0.9 : 1 }]}
            >
              <Text weight="800" style={{ color: '#fff', fontSize: 15 }}>
                {t('eligibility.check')}
              </Text>
            </Pressable>
            {!job || !dob ? (
              <Text variant="caption" tone="muted" align="center">
                {t('eligibility.fillHint')}
              </Text>
            ) : null}
          </Surface>

          {verdict ? (
            <View accessibilityLiveRegion="polite" style={[styles.result, { backgroundColor: toneBg, borderColor: toneFg }]}>
              <View style={styles.resultHead}>
                <Ionicons
                  name={tone === 'success' ? 'checkmark-circle' : tone === 'danger' ? 'close-circle' : 'help-circle'}
                  size={26}
                  color={toneFg}
                />
                <Text weight="900" style={{ color: toneFg, fontSize: 17 }}>
                  {tone === 'success' ? t('eligibility.eligible') : tone === 'danger' ? t('eligibility.notEligible') : t('eligibility.checkNotice')}
                </Text>
              </View>
              <CheckLine
                ok={verdict.ageKind === 'eligible' ? true : verdict.ageKind === 'unknown' ? null : false}
                text={t('eligibility.ageLine', { age: verdict.age, date: formatDob(verdict.cutoff) })}
              />
              <CheckLine
                ok={null}
                text={`${t('eligibility.requiredLine', { range: verdict.required })}${
                  verdict.relaxation > 0 && job && job.maxAge > 0 ? ` ${t('eligibility.inclRelaxation', { count: verdict.relaxation })}` : ''
                }`}
              />
              <CheckLine ok={verdict.education} text={t('eligibility.qualificationLine', { label: verdict.qualificationLabel })} />
              <Text variant="caption" tone="muted">
                {t('common.disclaimer')}
              </Text>
            </View>
          ) : null}
        </Page>
      </ScrollView>
    </View>
  );
}

/** Result line with a tick, a cross, or a neutral dot when the notice does not say. */
function CheckLine({ ok, text }: { ok: boolean | null; text: string }) {
  const { colors } = useTheme();
  return (
    <View style={styles.line}>
      <Ionicons
        name={ok === true ? 'checkmark-circle' : ok === false ? 'close-circle' : 'ellipse-outline'}
        size={18}
        color={ok === true ? colors.success : ok === false ? colors.danger : colors.textMuted}
      />
      <Text weight="600" style={{ flex: 1 }}>
        {text}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  line: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  hero: { alignItems: 'center', gap: 8 },
  art: { width: 96, height: 96 },
  cta: { borderRadius: 12, paddingVertical: 14, alignItems: 'center' },
  result: { borderRadius: 14, borderWidth: 1.5, padding: 16, gap: 6 },
  resultHead: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 2 },
});
