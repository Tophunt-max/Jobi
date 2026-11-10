import { Ionicons } from '@expo/vector-icons';
import { FlashList } from '@shopify/flash-list';
import { useMemo, useState, type ReactElement } from 'react';
import { useTranslation } from 'react-i18next';
import { Pressable, View } from 'react-native';

import { SyncStatus } from '@/components/SyncStatus';
import { EmptyArt, FilterChips, Page, ScreenHeader, SearchInput } from '@/components/ui';
import { ALL } from '@/constants/catalog';
import { textMatchesSector } from '@/features/jobs/logic';
import { useContentStore } from '@/store/contentStore';
import { useTheme } from '@/theme';

const CHIPS = [ALL, 'ssc', 'railways', 'banking', 'state', 'upsc', 'defence', 'police', 'teaching', 'medical'];

/** Colours cycled through the list icons, like the mockup's red / green / purple / blue rows. */
export const LIST_TINTS = [
  { fg: '#EF4444', bg: '#FEE2E2' },
  { fg: '#16A34A', bg: '#DCFCE7' },
  { fg: '#7C3AED', bg: '#EDE9FE' },
  { fg: '#2563EB', bg: '#DBEAFE' },
  { fg: '#EA580C', bg: '#FFEDD5' },
];

export function tintFor(index: number, dark: boolean) {
  const t = LIST_TINTS[index % LIST_TINTS.length]!;
  return dark ? { fg: t.fg, bg: `${t.fg}33` } : t;
}

/** Shared screen for results, admit cards, syllabus, answer keys and admissions. */
export function ContentList<T extends { id: string }>({
  title,
  items,
  text,
  renderItem,
}: {
  title: string;
  items: T[];
  /** Text used for search and sector chips. */
  text: (item: T) => string;
  renderItem: (item: T, index: number) => ReactElement;
}) {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const status = useContentStore((s) => s.status);
  const sync = useContentStore((s) => s.sync);
  const [sector, setSector] = useState<string>(ALL);
  const [searching, setSearching] = useState(false);
  const [query, setQuery] = useState('');

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return items.filter((i) => {
      const body = text(i);
      return textMatchesSector(body, sector) && (!q || body.toLowerCase().includes(q));
    });
  }, [items, text, sector, query]);

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader
        title={title}
        right={
          <Pressable
            accessibilityRole="button"
            accessibilityLabel={t('common.search')}
            onPress={() => {
              setSearching((s) => !s);
              setQuery('');
            }}
            hitSlop={8}
            style={{ padding: 6 }}
          >
            <Ionicons name={searching ? 'close' : 'search'} size={22} color={colors.text} />
          </Pressable>
        }
      />
      <View style={{ backgroundColor: colors.surface, paddingBottom: 10, gap: 10 }}>
        {searching ? (
          <Page>
            <SearchInput value={query} onChangeText={setQuery} placeholder={t('common.searchIn', { title })} autoFocus />
          </Page>
        ) : null}
        <FilterChips
          options={CHIPS.map((k) => ({ value: k, label: k === ALL ? t('jobs.statusAll') : t(`sectors.${k}`) }))}
          value={sector}
          onChange={setSector}
        />
      </View>
      <FlashList
        data={filtered}
        keyExtractor={(i) => i.id}
        renderItem={({ item, index }) => <Page style={{ paddingBottom: 10 }}>{renderItem(item, index)}</Page>}
        ListHeaderComponent={
          <Page style={{ paddingTop: 12 }}>
            <SyncStatus />
          </Page>
        }
        ListEmptyComponent={<EmptyArt title={t('results.empty')} body={t('results.emptyHint')} />}
        refreshing={status === 'syncing'}
        onRefresh={() => void sync({ force: true })}
        contentContainerStyle={{ paddingBottom: 24 }}
      />
    </View>
  );
}
