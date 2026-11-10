import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';

import { Badge, Card, Chip, ScrollScreen, Section, Text, ToggleRow } from '@/components/ui';
import { INDIAN_STATES, QUALIFICATIONS, SECTORS, VACANCY_THRESHOLDS } from '@/constants/catalog';
import { matchJobs } from '@/features/alerts/matcher';
import { JobCard } from '@/features/jobs/components/JobCard';
import { useOpenJobs } from '@/features/jobs/hooks';
import { formatCount } from '@/lib/format';
import { useUserStore } from '@/store/userStore';

function toggle(list: string[], value: string): string[] {
  return list.includes(value) ? list.filter((v) => v !== value) : [...list, value];
}

export default function AlertsScreen() {
  const { t } = useTranslation();
  const prefs = useUserStore((s) => s.alertPrefs);
  const setPrefs = useUserStore((s) => s.setAlertPrefs);
  const jobs = useOpenJobs();
  const matched = useMemo(() => matchJobs(jobs, prefs), [jobs, prefs]);

  return (
    <ScrollScreen>
      <Card>
        <ToggleRow
          label={t('alerts.enabled')}
          description={t('alerts.enabledDesc')}
          value={prefs.enabled}
          onValueChange={(enabled) => setPrefs({ ...prefs, enabled })}
        />
        <Text variant="caption" tone="muted">
          {t('alerts.anyHint')}
        </Text>
      </Card>

      {prefs.enabled ? (
        <>
          <Section title={t('alerts.sectors')}>
            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
              {SECTORS.map((s) => (
                <Chip
                  key={s.key}
                  icon={s.icon}
                  label={t(`sectors.${s.key}`)}
                  selected={prefs.categories.includes(s.key)}
                  onPress={() => setPrefs({ ...prefs, categories: toggle(prefs.categories, s.key) })}
                />
              ))}
            </View>
          </Section>

          <Section title={t('alerts.qualifications')}>
            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
              {QUALIFICATIONS.map((q) => (
                <Chip
                  key={q}
                  label={q}
                  selected={prefs.qualifications.includes(q)}
                  onPress={() => setPrefs({ ...prefs, qualifications: toggle(prefs.qualifications, q) })}
                />
              ))}
            </View>
          </Section>

          <Section title={t('alerts.states')}>
            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
              {INDIAN_STATES.map((st) => (
                <Chip
                  key={st}
                  label={st}
                  selected={prefs.states.includes(st)}
                  onPress={() => setPrefs({ ...prefs, states: toggle(prefs.states, st) })}
                />
              ))}
            </View>
          </Section>

          <Section title={t('alerts.minPosts')}>
            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
              {VACANCY_THRESHOLDS.map((n) => (
                <Chip
                  key={n}
                  label={n === 0 ? t('alerts.any') : `${formatCount(n)}+`}
                  selected={prefs.minVacancies === n}
                  onPress={() => setPrefs({ ...prefs, minVacancies: n })}
                />
              ))}
            </View>
          </Section>

          <Section title={t('alerts.matches', { count: matched.length })}>
            {matched.length === 0 ? (
              <Text variant="caption" tone="muted">
                {t('alerts.none')}
              </Text>
            ) : (
              matched.slice(0, 50).map((m) => (
                <View key={m.job.id} style={{ gap: 4 }}>
                  <Badge label={t('alerts.match', { score: m.score })} tone={m.score >= 90 ? 'success' : 'primary'} />
                  <JobCard job={m.job} compact />
                </View>
              ))
            )}
          </Section>
        </>
      ) : null}
    </ScrollScreen>
  );
}
