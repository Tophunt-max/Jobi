import { Ionicons } from '@expo/vector-icons';
import { useRouter, type Href } from 'expo-router';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Pressable, RefreshControl, StyleSheet, TextInput, View } from 'react-native';

import { SyncStatus } from '@/components/SyncStatus';
import { Card, Chip, ChipRow, Row, ScrollScreen, Section, Text } from '@/components/ui';
import { INDIAN_STATES, SECTORS } from '@/constants/catalog';
import { matchJobs } from '@/features/alerts/matcher';
import { SponsorCard } from '@/features/ads/SponsorCard';
import { JobCard } from '@/features/jobs/components/JobCard';
import { useJobFilters } from '@/features/jobs/filtersStore';
import { useBookmarkedJobs, useOpenJobs } from '@/features/jobs/hooks';
import { closingSoon } from '@/features/jobs/logic';
import { useContentStore } from '@/store/contentStore';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';

export default function HomeScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors, radius, spacing } = useTheme();
  const jobs = useOpenJobs();
  const saved = useBookmarkedJobs();
  const status = useContentStore((s) => s.status);
  const sync = useContentStore((s) => s.sync);
  const name = useUserStore((s) => s.profile.candidateName);
  const alertPrefs = useUserStore((s) => s.alertPrefs);
  const resetFilters = useJobFilters((s) => s.reset);
  const [query, setQuery] = useState('');

  const soon = useMemo(() => closingSoon(jobs), [jobs]);
  const matched = useMemo(() => matchJobs(jobs, alertPrefs), [jobs, alertPrefs]);
  const latest = useMemo(() => [...jobs].sort((a, b) => b.publishedAt - a.publishedAt).slice(0, 8), [jobs]);

  const openJobs = (patch: Parameters<typeof resetFilters>[0]) => {
    resetFilters(patch);
    router.navigate('/jobs');
  };

  const tools: { icon: keyof typeof Ionicons.glyphMap; title: string; desc: string; href: Href }[] = [
    { icon: 'calendar-outline', title: t('tools.age'), desc: t('tools.ageDesc'), href: '/tools/age' },
    { icon: 'cash-outline', title: t('tools.salary'), desc: t('tools.salaryDesc'), href: '/tools/salary' },
    { icon: 'notifications-outline', title: t('tools.alerts'), desc: t('tools.alertsDesc'), href: '/alerts' },
    { icon: 'body-outline', title: t('tools.physical'), desc: t('tools.physicalDesc'), href: '/tools/physical' },
  ];

  return (
    <ScrollScreen
      refreshControl={
        <RefreshControl refreshing={status === 'syncing'} onRefresh={() => void sync({ force: true })} tintColor={colors.primary} />
      }
    >
      <View style={{ gap: 4 }}>
        <Text variant="title">{t('home.greeting', { name: name ? `, ${name.split(' ')[0]}` : '' })}</Text>
        <Text tone="muted">{t('home.subtitle', { count: jobs.length })}</Text>
      </View>

      <View style={[styles.search, { backgroundColor: colors.surface, borderColor: colors.border, borderRadius: radius.lg }]}>
        <Ionicons name="search" size={18} color={colors.textMuted} />
        <TextInput
          accessibilityLabel={t('home.searchPlaceholder')}
          value={query}
          onChangeText={setQuery}
          placeholder={t('home.searchPlaceholder')}
          placeholderTextColor={colors.textMuted}
          returnKeyType="search"
          onSubmitEditing={() => openJobs({ query: query.trim() })}
          style={{ flex: 1, color: colors.text, paddingVertical: 12, fontSize: 14 }}
        />
      </View>

      <SyncStatus />

      <Row gap={spacing.sm}>
        <Stat label={t('home.statsOpen')} value={jobs.length} onPress={() => openJobs({})} />
        <Stat label={t('home.statsClosing')} value={soon.length} tone="danger" onPress={() => openJobs({ status: 'CLOSING_SOON', sort: 'deadline' })} />
        <Stat label={t('home.statsSaved')} value={saved.length} tone="success" onPress={() => router.navigate('/profile')} />
      </Row>

      <Card onPress={() => router.push('/alerts')} style={{ backgroundColor: colors.primarySoft, borderColor: colors.primarySoft }}>
        <Row>
          <Ionicons name="sparkles" size={20} color={colors.primary} />
          <Text tone="primary" weight="700" style={{ flex: 1 }}>
            {alertPrefs.enabled && matched.length > 0 ? t('home.alertsMatched', { count: matched.length }) : t('home.alertsSetup')}
          </Text>
          <Ionicons name="chevron-forward" size={18} color={colors.primary} />
        </Row>
      </Card>

      <Section
        title={t('home.closingSoon')}
        action={soon.length > 3 ? t('common.viewAll') : undefined}
        onAction={() => openJobs({ status: 'CLOSING_SOON', sort: 'deadline' })}
      >
        {soon.length === 0 ? (
          <Text tone="muted" variant="caption">
            {t('home.closingSoonEmpty')}
          </Text>
        ) : (
          soon.slice(0, 3).map((job) => <JobCard key={job.id} job={job} compact />)
        )}
      </Section>

      <Section title={t('home.sectors')}>
        <View style={styles.wrap}>
          {SECTORS.map((s) => (
            <Chip key={s.key} icon={s.icon} label={t(`sectors.${s.key}`)} onPress={() => openJobs({ sector: s.key })} />
          ))}
        </View>
      </Section>

      <Section title={t('home.states')}>
        <ChipRow>
          {INDIAN_STATES.map((st) => (
            <Chip key={st} label={st} onPress={() => openJobs({ state: st })} />
          ))}
        </ChipRow>
      </Section>

      <Section title={t('home.tools')}>
        <View style={styles.grid}>
          {tools.map((tool) => (
            <Card key={tool.title} onPress={() => router.push(tool.href)} style={styles.tool} accessibilityLabel={tool.title}>
              <Ionicons name={tool.icon} size={22} color={colors.primary} />
              <Text variant="subheading" numberOfLines={1}>
                {tool.title}
              </Text>
              <Text variant="caption" tone="muted" numberOfLines={2}>
                {tool.desc}
              </Text>
            </Card>
          ))}
        </View>
      </Section>

      <SponsorCard />

      <Section title={t('home.latest')} action={t('common.viewAll')} onAction={() => openJobs({})}>
        {latest.map((job) => (
          <JobCard key={job.id} job={job} />
        ))}
      </Section>

      <Text variant="caption" tone="muted" align="center">
        {t('common.disclaimer')}
      </Text>
    </ScrollScreen>
  );
}

function Stat({
  label,
  value,
  tone = 'primary',
  onPress,
}: {
  label: string;
  value: number;
  tone?: 'primary' | 'danger' | 'success';
  onPress: () => void;
}) {
  const { colors, radius } = useTheme();
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={`${label}: ${value}`}
      onPress={onPress}
      style={({ pressed }) => [
        styles.stat,
        { backgroundColor: colors.surface, borderColor: colors.border, borderRadius: radius.lg, opacity: pressed ? 0.85 : 1 },
      ]}
    >
      <Text variant="title" tone={tone}>
        {value}
      </Text>
      <Text variant="caption" tone="muted" numberOfLines={1}>
        {label}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  search: { flexDirection: 'row', alignItems: 'center', gap: 8, paddingHorizontal: 14, borderWidth: 1 },
  wrap: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  tool: { flexGrow: 1, flexBasis: '45%' },
  stat: { flex: 1, borderWidth: StyleSheet.hairlineWidth, padding: 12, gap: 2, alignItems: 'flex-start' },
});
