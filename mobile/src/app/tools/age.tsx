import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Card, InlineBanner, ScrollScreen, Section, Text, TextField } from '@/components/ui';
import { casteMeta } from '@/constants/catalog';
import { JobCard } from '@/features/jobs/components/JobCard';
import { useOpenJobs } from '@/features/jobs/hooks';
import { effectiveUpperAge } from '@/features/profile/eligibility';
import { ageOn, parseDate, toIsoDate } from '@/lib/dates';
import { useUserStore } from '@/store/userStore';

function todayDdMmYyyy(): string {
  const [y, m, d] = toIsoDate(new Date()).split('-');
  return `${d}-${m}-${y}`;
}

export default function AgeCalculatorScreen() {
  const { t } = useTranslation();
  const profile = useUserStore((s) => s.profile);
  const lockerDob = useUserStore((s) => s.locker.dob);
  const jobs = useOpenJobs();
  const [dob, setDob] = useState(lockerDob);
  const [cutoff, setCutoff] = useState(todayDdMmYyyy());

  const dobDate = parseDate(dob);
  const cutoffDate = parseDate(cutoff);
  const age = dobDate && cutoffDate && dobDate <= cutoffDate ? ageOn(dobDate, cutoffDate) : null;

  const eligible = useMemo(() => {
    if (!age) return [];
    return jobs.filter((j) => j.maxAge > 0 && age.years >= j.minAge && age.years <= effectiveUpperAge(j, profile)).slice(0, 30);
  }, [age, jobs, profile]);

  return (
    <ScrollScreen>
      <Card>
        <TextField label={t('age.dob')} value={dob} onChangeText={setDob} placeholder="15-08-2001" keyboardType="numbers-and-punctuation" maxLength={10} />
        <TextField label={t('age.cutoff')} value={cutoff} onChangeText={setCutoff} placeholder="01-08-2026" keyboardType="numbers-and-punctuation" maxLength={10} />
        {age ? (
          <Text variant="title" tone="primary">
            {t('age.result', age)}
          </Text>
        ) : dob.length >= 8 ? (
          <InlineBanner tone="warning" message={t('age.invalid')} />
        ) : null}
      </Card>

      {age ? (
        <Section title={t('age.eligibleJobs')}>
          <Text variant="caption" tone="muted">
            {t('age.eligibleJobsHint', { category: casteMeta(profile.category).label })}
          </Text>
          {eligible.length === 0 ? (
            <Text tone="muted">{t('age.none')}</Text>
          ) : (
            eligible.map((j) => <JobCard key={j.id} job={j} compact />)
          )}
        </Section>
      ) : null}
    </ScrollScreen>
  );
}
