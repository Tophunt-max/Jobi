import { Ionicons } from '@expo/vector-icons';
import { Redirect, useRouter, type Href } from 'expo-router';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Image,
  Modal,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  View,
  type ImageSourcePropType,
} from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';

import { categoryArt, images } from '@/assets/images';
import { SyncStatus } from '@/components/SyncStatus';
import { ArtIcon, Page, SearchInput, Surface, Text, type IconName } from '@/components/ui';
import { SponsorCard } from '@/features/ads/SponsorCard';
import { JobCard } from '@/features/jobs/components/JobCard';
import { useJobFilters } from '@/features/jobs/filtersStore';
import { useOpenJobs } from '@/features/jobs/hooks';
import { closingSoon, SECTOR_ART } from '@/features/jobs/logic';
import { useInbox } from '@/features/notifications/inbox';
import { useContentStore } from '@/store/contentStore';
import { useSettingsStore } from '@/store/settingsStore';
import { MAX_CONTENT_WIDTH, useTheme } from '@/theme';

const TOP_CATEGORIES = ['ssc', 'railways', 'banking', 'upsc', 'defence', 'police', 'teaching', 'state'] as const;

export default function HomeScreen() {
  const onboardingDone = useSettingsStore((s) => s.onboardingDone);
  if (!onboardingDone) return <Redirect href="/onboarding" />;
  return <Home />;
}

function Home() {
  const { t } = useTranslation();
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { colors } = useTheme();
  const jobs = useOpenJobs();
  const status = useContentStore((s) => s.status);
  const sync = useContentStore((s) => s.sync);
  const resetFilters = useJobFilters((s) => s.reset);
  const { unread } = useInbox();
  const [query, setQuery] = useState('');
  const [menuOpen, setMenuOpen] = useState(false);

  const soon = useMemo(() => closingSoon(jobs), [jobs]);
  const latest = useMemo(() => [...jobs].sort((a, b) => b.publishedAt - a.publishedAt).slice(0, 5), [jobs]);

  const openJobs = (patch: Parameters<typeof resetFilters>[0]) => {
    resetFilters(patch);
    router.navigate('/jobs');
  };

  const quick: { image: ImageSourcePropType; label: string; onPress: () => void }[] = [
    { image: images.quick.latestJobs, label: t('home.quickLatest'), onPress: () => openJobs({}) },
    { image: images.quick.results, label: t('home.quickResults'), onPress: () => router.push('/results') },
    { image: images.quick.admitCards, label: t('home.quickAdmit'), onPress: () => router.push('/admit-cards') },
    { image: images.quick.answerKey, label: t('home.quickAnswerKey'), onPress: () => router.push('/answer-keys') },
    { image: images.quick.syllabus, label: t('home.quickSyllabus'), onPress: () => router.push('/syllabus') },
    { image: images.quick.examCalendar, label: t('home.quickCalendar'), onPress: () => router.push('/exam-calendar') },
  ];

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScrollView
        keyboardShouldPersistTaps="handled"
        contentContainerStyle={{ paddingBottom: 32 }}
        refreshControl={<RefreshControl refreshing={status === 'syncing'} onRefresh={() => void sync({ force: true })} tintColor="#fff" />}
      >
        <View style={[styles.header, { paddingTop: insets.top + 8 }]}>
          <Image source={images.home.header} style={styles.fillImage} resizeMode="cover" accessibilityIgnoresInvertColors />
          <View style={styles.headerInner}>
            <View style={styles.topRow}>
              <Pressable accessibilityRole="button" accessibilityLabel={t('home.menu')} onPress={() => setMenuOpen(true)} hitSlop={10}>
                <Ionicons name="menu" size={26} color="#fff" />
              </Pressable>
              <View style={{ flex: 1 }}>
                <Text style={styles.brand}>{t('app.title')}</Text>
                <Text style={styles.brandSub}>{t('splash.tagline')}</Text>
              </View>
              <Pressable
                accessibilityRole="button"
                accessibilityLabel={t('notifications.title')}
                onPress={() => router.push('/notifications')}
                hitSlop={10}
                style={styles.bell}
              >
                <Ionicons name="notifications-outline" size={24} color="#fff" />
                {unread > 0 ? (
                  <View style={[styles.badge, { backgroundColor: colors.danger }]}>
                    <Text variant="tiny" style={{ color: '#fff', fontSize: 9 }}>
                      {unread > 9 ? '9+' : unread}
                    </Text>
                  </View>
                ) : null}
              </Pressable>
              <Pressable accessibilityRole="button" accessibilityLabel={t('tabs.profile')} onPress={() => router.navigate('/profile')}>
                <Image source={images.profile.avatar} style={styles.avatar} accessibilityIgnoresInvertColors />
              </Pressable>
            </View>

            <SearchInput
              value={query}
              onChangeText={setQuery}
              placeholder={t('home.searchPlaceholder')}
              mic
              onSubmit={(q) => openJobs({ query: q.trim() })}
              style={{ borderColor: 'transparent' }}
            />

            <Pressable accessibilityRole="button" onPress={() => openJobs({})} accessibilityLabel={t('home.heroTitle')}>
              <View style={styles.hero}>
                <Image source={images.home.hero} style={styles.fillImage} resizeMode="cover" accessibilityIgnoresInvertColors />
                <Text style={styles.heroTitle}>{t('home.heroTitle')}</Text>
                <Text style={styles.heroSub}>{t('home.heroSub')}</Text>
              </View>
            </Pressable>
          </View>
        </View>

        <Page style={{ gap: 18, marginTop: 16 }}>
          <SyncStatus />

          <View style={styles.grid}>
            {quick.map((q) => (
              <Surface key={q.label} onPress={q.onPress} accessibilityLabel={q.label} style={styles.quick}>
                <ArtIcon source={q.image} size={44} />
                <Text variant="caption" weight="700" align="center" numberOfLines={2}>
                  {q.label}
                </Text>
              </Surface>
            ))}
          </View>

          <View style={{ gap: 10 }}>
            <View style={styles.sectionRow}>
              <Text variant="heading" weight="800">
                {t('home.topCategories')}
              </Text>
              <Pressable accessibilityRole="button" onPress={() => router.push('/categories')} hitSlop={8}>
                <Text variant="caption" weight="800" tone="primary">
                  {t('common.viewAll')}
                </Text>
              </Pressable>
            </View>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 10, paddingVertical: 4, paddingHorizontal: 2 }}>
              {TOP_CATEGORIES.map((key) => (
                <Surface key={key} onPress={() => openJobs({ sector: key })} style={styles.category} accessibilityLabel={t(`sectors.${key}`)}>
                  <ArtIcon source={categoryArt[SECTOR_ART[key] ?? 'other']} size={36} />
                  <Text variant="caption" weight="700" numberOfLines={1}>
                    {t(`sectors.${key}`)}
                  </Text>
                </Surface>
              ))}
            </ScrollView>
          </View>

          {soon.length > 0 ? (
            <View style={{ gap: 10 }}>
              <View style={styles.sectionRow}>
                <Text variant="heading" weight="800">
                  {t('home.closingSoon')}
                </Text>
                <Pressable accessibilityRole="button" onPress={() => openJobs({ status: 'CLOSING_SOON', sort: 'deadline' })} hitSlop={8}>
                  <Text variant="caption" weight="800" tone="primary">
                    {t('common.viewAll')}
                  </Text>
                </Pressable>
              </View>
              {soon.slice(0, 3).map((job) => (
                <JobCard key={job.id} job={job} />
              ))}
            </View>
          ) : null}

          <SponsorCard />

          <View style={{ gap: 10 }}>
            <View style={styles.sectionRow}>
              <Text variant="heading" weight="800">
                {t('home.latest')}
              </Text>
              <Pressable accessibilityRole="button" onPress={() => openJobs({})} hitSlop={8}>
                <Text variant="caption" weight="800" tone="primary">
                  {t('common.viewAll')}
                </Text>
              </Pressable>
            </View>
            {latest.length === 0 ? (
              <Text variant="caption" tone="muted">
                {t('home.noJobs')}
              </Text>
            ) : (
              latest.map((job) => <JobCard key={job.id} job={job} />)
            )}
          </View>

          <Text variant="caption" tone="muted" align="center">
            {t('common.disclaimer')}
          </Text>
        </Page>
      </ScrollView>

      <SideMenu visible={menuOpen} onClose={() => setMenuOpen(false)} />
    </View>
  );
}

/** Drawer opened by the menu button: every section of the app in one list. */
function SideMenu({ visible, onClose }: { visible: boolean; onClose: () => void }) {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors } = useTheme();
  const items: { icon: IconName; label: string; href: Href }[] = [
    { icon: 'briefcase-outline', label: t('home.quickLatest'), href: '/jobs' },
    { icon: 'grid-outline', label: t('categories.title'), href: '/categories' },
    { icon: 'trophy-outline', label: t('home.quickResults'), href: '/results' },
    { icon: 'id-card-outline', label: t('home.quickAdmit'), href: '/admit-cards' },
    { icon: 'key-outline', label: t('home.quickAnswerKey'), href: '/answer-keys' },
    { icon: 'book-outline', label: t('home.quickSyllabus'), href: '/syllabus' },
    { icon: 'school-outline', label: t('results.tabAdmissions'), href: '/admissions' },
    { icon: 'calendar-outline', label: t('home.quickCalendar'), href: '/exam-calendar' },
    { icon: 'checkmark-done-outline', label: t('eligibility.title'), href: '/eligibility' },
    { icon: 'clipboard-outline', label: t('applications.title'), href: '/applications' },
    { icon: 'hourglass-outline', label: t('tools.age'), href: '/tools/age' },
    { icon: 'cash-outline', label: t('tools.salary'), href: '/tools/salary' },
    { icon: 'body-outline', label: t('tools.physical'), href: '/tools/physical' },
    { icon: 'notifications-outline', label: t('tools.alerts'), href: '/alerts' },
    { icon: 'help-circle-outline', label: t('help.title'), href: '/help' },
    { icon: 'information-circle-outline', label: t('about.title'), href: '/about' },
  ];
  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <View style={styles.drawerRoot}>
        <SafeAreaView edges={['top', 'bottom']} style={[styles.drawer, { backgroundColor: colors.surface }]}>
          <View style={styles.drawerHead}>
            <Image source={images.brand.logoMark} style={{ width: 40, height: 40 }} accessibilityIgnoresInvertColors />
            <View style={{ flex: 1 }}>
              <Text variant="heading" weight="900">
                {t('app.title')}
              </Text>
              <Text variant="caption" tone="muted">
                {t('splash.tagline')}
              </Text>
            </View>
            <Pressable accessibilityRole="button" accessibilityLabel={t('common.close')} onPress={onClose} hitSlop={10}>
              <Ionicons name="close" size={24} color={colors.text} />
            </Pressable>
          </View>
          <ScrollView>
            {items.map((it) => (
              <Pressable
                key={it.label}
                accessibilityRole="button"
                onPress={() => {
                  onClose();
                  router.push(it.href);
                }}
                style={({ pressed }) => [styles.drawerItem, { backgroundColor: pressed ? colors.surfaceMuted : 'transparent' }]}
              >
                <Ionicons name={it.icon} size={20} color={colors.primary} />
                <Text weight="600">{it.label}</Text>
              </Pressable>
            ))}
          </ScrollView>
        </SafeAreaView>
        <Pressable style={[styles.drawerBackdrop, { backgroundColor: colors.overlay }]} onPress={onClose} accessibilityLabel={t('common.close')} />
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  // Explicit size: on web an absolutely positioned image otherwise keeps its natural width.
  fillImage: { position: 'absolute', top: 0, left: 0, width: '100%', height: '100%' },
  header: { borderBottomLeftRadius: 26, borderBottomRightRadius: 26, overflow: 'hidden', paddingBottom: 18 },
  headerInner: { width: '100%', maxWidth: MAX_CONTENT_WIDTH, alignSelf: 'center', paddingHorizontal: 16, gap: 14 },
  topRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  brand: { color: '#fff', fontSize: 22, fontWeight: '900', letterSpacing: 0.5 },
  brandSub: { color: '#fff', opacity: 0.85, fontSize: 11, fontWeight: '600' },
  bell: { padding: 2 },
  badge: { position: 'absolute', top: -2, right: -4, minWidth: 16, height: 16, borderRadius: 8, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 3 },
  avatar: { width: 36, height: 36, borderRadius: 18, borderWidth: 2, borderColor: '#fff' },
  hero: { width: '100%', aspectRatio: 342 / 128, padding: 16, justifyContent: 'center', borderRadius: 18, overflow: 'hidden' },
  heroTitle: { color: '#fff', fontSize: 19, lineHeight: 24, fontWeight: '900', maxWidth: '55%' },
  heroSub: { color: '#fff', opacity: 0.9, fontSize: 11, fontWeight: '600', marginTop: 6 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  quick: { flexBasis: '30%', flexGrow: 1, alignItems: 'center', gap: 8, paddingVertical: 14 },
  sectionRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  category: { width: 92, alignItems: 'center', gap: 6, paddingVertical: 12, paddingHorizontal: 8 },
  drawerRoot: { flex: 1, flexDirection: 'row' },
  drawer: { width: '80%', maxWidth: 320 },
  drawerBackdrop: { flex: 1 },
  drawerHead: { flexDirection: 'row', alignItems: 'center', gap: 12, padding: 16 },
  drawerItem: { flexDirection: 'row', alignItems: 'center', gap: 14, paddingVertical: 14, paddingHorizontal: 20 },
});
