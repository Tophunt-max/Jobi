import { useRouter } from 'expo-router';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ScrollView, StyleSheet, View } from 'react-native';
import { useShallow } from 'zustand/react/shallow';

import { DropdownPill, EmptyArt, FilterChips, Page, Pill, ScreenHeader, Surface, Text, type PillTone } from '@/components/ui';
import { buildCalendar, type CalendarKind } from '@/features/calendar/events';
import { useOpenLink } from '@/lib/useOpenLink';
import { useContentStore } from '@/store/contentStore';
import { useTheme } from '@/theme';

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

const KIND_TONE: Record<CalendarKind, PillTone> = {
  exam: 'blue',
  examStart: 'purple',
  lastDate: 'red',
  applyStart: 'green',
};

export default function ExamCalendarScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const openLink = useOpenLink();
  const { colors } = useTheme();
  const { jobs, admitCards } = useContentStore(useShallow((s) => ({ jobs: s.jobs, admitCards: s.admitCards })));
  const events = buildCalendar(jobs, admitCards);

  const now = new Date();
  const thisYear = now.getFullYear();
  const years = [...new Set([...events.map((e) => e.date.getFullYear()), thisYear])].sort((a, b) => a - b);
  const [year, setYear] = useState(String(now.getFullYear()));
  const [month, setMonth] = useState(String(now.getMonth()));

  const visible = events.filter((e) => e.date.getFullYear() === Number(year) && e.date.getMonth() === Number(month));
  const perMonth = MONTHS.map((_, m) => events.filter((e) => e.date.getFullYear() === Number(year) && e.date.getMonth() === m).length);

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader title={t('calendar.title')} />
      <View style={{ backgroundColor: colors.surface, paddingBottom: 12, gap: 12 }}>
        <Page>
          <DropdownPill label={t('calendar.year')} value={year} options={years.map((y) => ({ value: String(y), label: String(y) }))} onChange={setYear} />
        </Page>
        <FilterChips
          options={MONTHS.map((m, i) => ({ value: String(i), label: perMonth[i] ? `${m} · ${perMonth[i]}` : m }))}
          value={month}
          onChange={setMonth}
        />
      </View>
      <ScrollView contentContainerStyle={{ paddingVertical: 14, paddingBottom: 32 }}>
        <Page style={{ gap: 10 }}>
          {visible.length === 0 ? (
            <EmptyArt title={t('calendar.empty', { month: MONTHS[Number(month)], year })} body={t('calendar.emptyHint')} />
          ) : (
            visible.map((e) => (
              <Surface
                key={e.id}
                accessibilityLabel={`${e.date.getDate()} ${MONTHS[e.date.getMonth()]}, ${e.title}`}
                onPress={
                  e.jobId
                    ? () => router.push({ pathname: '/job/[id]', params: { id: e.jobId! } })
                    : e.url
                      ? () => void openLink(e.url!, e.title)
                      : undefined
                }
                style={styles.row}
              >
                <View style={[styles.dateBox, { backgroundColor: colors.primarySoft }]}>
                  <Text weight="900" tone="primary" style={{ fontSize: 20, lineHeight: 24 }}>
                    {String(e.date.getDate()).padStart(2, '0')}
                  </Text>
                  <Text variant="tiny" weight="800" tone="primary">
                    {MONTHS[e.date.getMonth()]}
                  </Text>
                </View>
                <View style={{ flex: 1, gap: 4 }}>
                  <Text weight="800" numberOfLines={2}>
                    {e.title}
                  </Text>
                  <Pill tone={KIND_TONE[e.kind]} label={t(`calendar.kind_${e.kind}`)} />
                </View>
              </Surface>
            ))
          )}
        </Page>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 14 },
  dateBox: { width: 58, height: 58, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
});
