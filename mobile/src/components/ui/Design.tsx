import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useMemo, useRef, useState, type ComponentProps, type ComponentRef, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import {
  FlatList,
  Image,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  View,
  type ImageSourcePropType,
  type StyleProp,
  type ViewStyle,
} from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';

import { images } from '@/assets/images';
import { MAX_CONTENT_WIDTH, useTheme } from '@/theme';

import { SearchInput } from './SearchInput';
import { Text } from './Text';

export type IconName = ComponentProps<typeof Ionicons>['name'];

/** White top bar with a back arrow, a left-aligned bold title and optional actions. */
export function ScreenHeader({
  title,
  back = true,
  right,
  titleAccessory,
  onBack,
}: {
  title: string;
  back?: boolean;
  right?: ReactNode;
  titleAccessory?: ReactNode;
  onBack?: () => void;
}) {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const goBack = () => {
    if (onBack) return onBack();
    if (router.canGoBack()) router.back();
    else router.replace('/');
  };
  return (
    <View style={{ backgroundColor: colors.surface, paddingTop: insets.top }}>
      <View style={styles.header}>
        {back ? (
          <Pressable accessibilityRole="button" accessibilityLabel={t('common.back')} onPress={goBack} hitSlop={10} style={styles.headerBack}>
            <Ionicons name="arrow-back" size={22} color={colors.text} />
          </Pressable>
        ) : null}
        <View style={styles.headerTitle}>
          <Text variant="heading" weight="800" numberOfLines={1} accessibilityRole="header" style={{ fontSize: 18 }}>
            {title}
          </Text>
          {titleAccessory}
        </View>
        {right ? <View style={styles.headerRight}>{right}</View> : null}
      </View>
    </View>
  );
}

/** Rounded white card with a soft shadow. */
export function Surface({
  children,
  style,
  onPress,
  accessibilityLabel,
  padded = true,
}: {
  children: ReactNode;
  style?: StyleProp<ViewStyle>;
  onPress?: () => void;
  accessibilityLabel?: string;
  padded?: boolean;
}) {
  const { colors, shadows } = useTheme();
  const base: ViewStyle = {
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: padded ? 14 : 0,
    boxShadow: shadows.card,
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: colors.border,
  };
  if (!onPress) return <View style={[base, style]}>{children}</View>;
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
      onPress={onPress}
      style={({ pressed }) => [base, { opacity: pressed ? 0.9 : 1, transform: [{ scale: pressed ? 0.99 : 1 }] }, style]}
    >
      {children}
    </Pressable>
  );
}

export type PillTone = 'green' | 'blue' | 'orange' | 'red' | 'purple' | 'gray' | 'amber';

/** Small rounded label, e.g. "12,195 Vacancies" or "Status: Submitted". */
export function Pill({ label, tone = 'blue', icon, strong }: { label: string; tone?: PillTone; icon?: IconName; strong?: boolean }) {
  const { scheme } = useTheme();
  const [bg, fg] = pillColors(tone, scheme === 'dark');
  return (
    <View style={[styles.pill, { backgroundColor: strong ? fg : bg }]}>
      {icon ? <Ionicons name={icon} size={11} color={strong ? '#fff' : fg} /> : null}
      <Text variant="tiny" weight="700" style={{ color: strong ? '#fff' : fg, fontSize: 11 }} numberOfLines={1}>
        {label}
      </Text>
    </View>
  );
}

export function pillColors(tone: PillTone, dark = false): [string, string] {
  const light: Record<PillTone, [string, string]> = {
    green: ['#DCFCE7', '#15803D'],
    blue: ['#DBEAFE', '#1D4ED8'],
    orange: ['#FFEDD5', '#C2410C'],
    red: ['#FEE2E2', '#B91C1C'],
    purple: ['#EDE9FE', '#6D28D9'],
    gray: ['#F1F5F9', '#475569'],
    amber: ['#FEF3C7', '#B45309'],
  };
  const night: Record<PillTone, [string, string]> = {
    green: ['#064E3B', '#6EE7B7'],
    blue: ['#1E3A8A', '#93C5FD'],
    orange: ['#431407', '#FDBA74'],
    red: ['#4C0519', '#FDA4AF'],
    purple: ['#2E1065', '#C4B5FD'],
    gray: ['#1E293B', '#CBD5E1'],
    amber: ['#451A03', '#FCD34D'],
  };
  return (dark ? night : light)[tone];
}

/** Horizontal filter chips. The selected chip is solid blue. */
export function FilterChips<T extends string>({
  options,
  value,
  onChange,
  style,
}: {
  options: { value: T; label: string }[];
  value: T;
  onChange: (v: T) => void;
  style?: StyleProp<ViewStyle>;
}) {
  const { colors } = useTheme();
  const scroller = useRef<ComponentRef<typeof ScrollView>>(null);
  const scrolled = useRef(false);
  return (
    <ScrollView ref={scroller} horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={[styles.chips, style]}>
      {options.map((o) => {
        const selected = o.value === value;
        return (
          <Pressable
            key={o.value}
            // Bring the initially selected chip into view (e.g. the current month).
            onLayout={
              selected
                ? (e) => {
                    if (scrolled.current) return;
                    scrolled.current = true;
                    scroller.current?.scrollTo({ x: Math.max(0, e.nativeEvent.layout.x - 16), animated: false });
                  }
                : undefined
            }
            accessibilityRole="button"
            accessibilityState={{ selected }}
            onPress={() => onChange(o.value)}
            style={({ pressed }) => [
              styles.chip,
              {
                backgroundColor: selected ? colors.primary : colors.surface,
                borderColor: selected ? colors.primary : colors.border,
                opacity: pressed ? 0.85 : 1,
              },
            ]}
          >
            <Text variant="caption" weight="700" style={{ color: selected ? colors.onPrimary : colors.text }} numberOfLines={1}>
              {o.label}
            </Text>
          </Pressable>
        );
      })}
    </ScrollView>
  );
}

/** Equal-width tabs (Overview / Vacancy / Dates, Applied / Saved / Alerts). */
export function TabPills<T extends string>({
  options,
  value,
  onChange,
}: {
  options: { value: T; label: string }[];
  value: T;
  onChange: (v: T) => void;
}) {
  const { colors } = useTheme();
  return (
    <View style={[styles.tabPills, { backgroundColor: colors.surfaceMuted }]}>
      {options.map((o) => {
        const selected = o.value === value;
        return (
          <Pressable
            key={o.value}
            accessibilityRole="tab"
            accessibilityState={{ selected }}
            onPress={() => onChange(o.value)}
            style={[styles.tabPill, selected && { backgroundColor: colors.primary }]}
          >
            <Text variant="caption" weight="700" style={{ color: selected ? colors.onPrimary : colors.textMuted }} numberOfLines={1}>
              {o.label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

/** Settings-style row with an icon, label, optional value and a chevron. */
export function MenuRow({
  icon,
  label,
  value,
  subtitle,
  onPress,
  tone,
  last,
}: {
  icon: IconName;
  label: string;
  value?: string;
  subtitle?: string;
  onPress: () => void;
  tone?: 'danger';
  last?: boolean;
}) {
  const { colors } = useTheme();
  const fg = tone === 'danger' ? colors.danger : colors.text;
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={value ? `${label}, ${value}` : label}
      onPress={onPress}
      style={({ pressed }) => [
        styles.menuRow,
        { borderBottomColor: colors.border, borderBottomWidth: last ? 0 : StyleSheet.hairlineWidth, backgroundColor: pressed ? colors.surfaceMuted : 'transparent' },
      ]}
    >
      <Ionicons name={icon} size={20} color={tone === 'danger' ? colors.danger : colors.textMuted} />
      <View style={{ flex: 1, gap: 1 }}>
        <Text weight="600" style={{ color: fg }} numberOfLines={1}>
          {label}
        </Text>
        {subtitle ? (
          <Text variant="caption" tone="muted" numberOfLines={1}>
            {subtitle}
          </Text>
        ) : null}
      </View>
      {value ? (
        <Text variant="caption" tone="muted" numberOfLines={1}>
          {value}
        </Text>
      ) : null}
      <Ionicons name="chevron-forward" size={16} color={colors.textMuted} />
    </Pressable>
  );
}

/** Bundled image at a fixed square size, optionally tinted and on a soft rounded background. */
export function ArtIcon({
  source,
  size = 40,
  tint,
  background,
  radius = 12,
  padding,
}: {
  source: ImageSourcePropType;
  size?: number;
  tint?: string;
  background?: string;
  radius?: number;
  padding?: number;
}) {
  const img = (
    <Image
      source={source}
      accessibilityIgnoresInvertColors
      style={{ width: background ? size - (padding ?? size * 0.44) : size, height: background ? size - (padding ?? size * 0.44) : size, tintColor: tint }}
      resizeMode="contain"
    />
  );
  if (!background) return img;
  return <View style={{ width: size, height: size, borderRadius: radius, backgroundColor: background, alignItems: 'center', justifyContent: 'center' }}>{img}</View>;
}

/** Empty list placeholder with the bundled illustration. */
export function EmptyArt({ title, body, children }: { title: string; body?: string; children?: ReactNode }) {
  return (
    <View style={styles.empty}>
      <Image source={images.states.empty} style={{ width: 150, height: 112 }} resizeMode="contain" accessibilityIgnoresInvertColors />
      <Text variant="subheading" weight="800" align="center">
        {title}
      </Text>
      {body ? (
        <Text variant="caption" tone="muted" align="center">
          {body}
        </Text>
      ) : null}
      {children}
    </View>
  );
}

/** Body wrapper that centres content on wide screens. */
export function Page({ children, style }: { children: ReactNode; style?: StyleProp<ViewStyle> }) {
  return <View style={[{ width: '100%', maxWidth: MAX_CONTENT_WIDTH, alignSelf: 'center', paddingHorizontal: 16 }, style]}>{children}</View>;
}

export type SheetOption<T extends string> = { value: T; label: string; subtitle?: string };

/** Bottom sheet with a list of options. */
export function OptionSheet<T extends string>({
  visible,
  title,
  options,
  value,
  onSelect,
  onClose,
}: {
  visible: boolean;
  title: string;
  options: SheetOption<T>[];
  value?: T;
  onSelect: (v: T) => void;
  onClose: () => void;
}) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <Pressable style={[styles.backdrop, { backgroundColor: colors.overlay }]} onPress={onClose} accessibilityLabel={t('common.close')} />
      <SafeAreaView edges={['bottom']} style={[styles.sheet, { backgroundColor: colors.surface }]}>
        <View style={styles.sheetHeader}>
          <Text variant="heading" weight="800">
            {title}
          </Text>
          <Pressable accessibilityRole="button" accessibilityLabel={t('common.close')} onPress={onClose} hitSlop={8}>
            <Ionicons name="close" size={22} color={colors.text} />
          </Pressable>
        </View>
        <FlatList
          data={options}
          keyExtractor={(o) => o.value}
          style={{ maxHeight: 440 }}
          renderItem={({ item }) => (
            <Pressable
              accessibilityRole="button"
              accessibilityState={{ selected: item.value === value }}
              onPress={() => {
                onSelect(item.value);
                onClose();
              }}
              style={({ pressed }) => [styles.option, { backgroundColor: pressed ? colors.surfaceMuted : 'transparent' }]}
            >
              <View style={{ flex: 1 }}>
                <Text weight={item.value === value ? '700' : '500'}>{item.label}</Text>
                {item.subtitle ? (
                  <Text variant="caption" tone="muted" numberOfLines={1}>
                    {item.subtitle}
                  </Text>
                ) : null}
              </View>
              {item.value === value ? <Ionicons name="checkmark" size={18} color={colors.primary} /> : null}
            </Pressable>
          )}
        />
      </SafeAreaView>
    </Modal>
  );
}

/** Compact dropdown button, e.g. the year selector on the exam calendar. */
export function DropdownPill<T extends string>({
  label,
  value,
  options,
  onChange,
}: {
  label: string;
  value: T;
  options: SheetOption<T>[];
  onChange: (v: T) => void;
}) {
  const { colors } = useTheme();
  const [open, setOpen] = useState(false);
  const current = options.find((o) => o.value === value);
  return (
    <>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel={`${label}: ${current?.label ?? ''}`}
        onPress={() => setOpen(true)}
        style={[styles.dropdown, { borderColor: colors.primary, backgroundColor: colors.surface }]}
      >
        <Text weight="700" tone="primary">
          {current?.label ?? '—'}
        </Text>
        <Ionicons name="chevron-down" size={16} color={colors.primary} />
      </Pressable>
      <OptionSheet visible={open} title={label} options={options} value={value} onSelect={onChange} onClose={() => setOpen(false)} />
    </>
  );
}

/** Form field that looks like an input and opens an option sheet. */
export function FormSelect<T extends string>({
  label,
  value,
  options,
  onChange,
  placeholder,
  searchable,
}: {
  label: string;
  value: T | '';
  options: SheetOption<T>[];
  onChange: (v: T) => void;
  placeholder?: string;
  searchable?: boolean;
}) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const current = options.find((o) => o.value === value);
  const filtered = useMemo(
    () => (query ? options.filter((o) => o.label.toLowerCase().includes(query.toLowerCase())) : options),
    [options, query],
  );
  return (
    <View style={{ gap: 6 }}>
      <Text variant="caption" weight="700">
        {label}
      </Text>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel={`${label}: ${current?.label ?? placeholder ?? ''}`}
        onPress={() => setOpen(true)}
        style={[styles.formBox, { borderColor: colors.border, backgroundColor: colors.surface }]}
      >
        <Text numberOfLines={1} style={{ flex: 1 }} tone={current ? 'default' : 'muted'}>
          {current?.label ?? placeholder ?? '—'}
        </Text>
        <Ionicons name="chevron-down" size={18} color={colors.textMuted} />
      </Pressable>
      <SearchableSheet
        visible={open}
        title={label}
        options={filtered}
        value={value || undefined}
        query={searchable ? query : undefined}
        onQuery={setQuery}
        searchPlaceholder={t('common.search')}
        onSelect={onChange}
        onClose={() => {
          setOpen(false);
          setQuery('');
        }}
      />
    </View>
  );
}

function SearchableSheet<T extends string>(props: {
  visible: boolean;
  title: string;
  options: SheetOption<T>[];
  value?: T;
  query?: string;
  onQuery: (q: string) => void;
  searchPlaceholder: string;
  onSelect: (v: T) => void;
  onClose: () => void;
}) {
  const { colors } = useTheme();
  if (props.query === undefined) {
    return <OptionSheet visible={props.visible} title={props.title} options={props.options} value={props.value} onSelect={props.onSelect} onClose={props.onClose} />;
  }
  return (
    <Modal visible={props.visible} animationType="slide" transparent onRequestClose={props.onClose}>
      <Pressable style={[styles.backdrop, { backgroundColor: colors.overlay }]} onPress={props.onClose} />
      <SafeAreaView edges={['bottom']} style={[styles.sheet, { backgroundColor: colors.surface }]}>
        <View style={styles.sheetHeader}>
          <Text variant="heading" weight="800">
            {props.title}
          </Text>
          <Pressable accessibilityRole="button" onPress={props.onClose} hitSlop={8}>
            <Ionicons name="close" size={22} color={colors.text} />
          </Pressable>
        </View>
        <View style={{ paddingHorizontal: 16, paddingBottom: 8 }}>
          <SearchInput value={props.query} onChangeText={props.onQuery} placeholder={props.searchPlaceholder} />
        </View>
        <FlatList
          data={props.options}
          keyExtractor={(o) => o.value}
          keyboardShouldPersistTaps="handled"
          style={{ maxHeight: 420 }}
          renderItem={({ item }) => (
            <Pressable
              accessibilityRole="button"
              onPress={() => {
                props.onSelect(item.value);
                props.onClose();
              }}
              style={({ pressed }) => [styles.option, { backgroundColor: pressed ? colors.surfaceMuted : 'transparent' }]}
            >
              <View style={{ flex: 1 }}>
                <Text weight={item.value === props.value ? '700' : '500'}>{item.label}</Text>
                {item.subtitle ? (
                  <Text variant="caption" tone="muted" numberOfLines={1}>
                    {item.subtitle}
                  </Text>
                ) : null}
              </View>
              {item.value === props.value ? <Ionicons name="checkmark" size={18} color={colors.primary} /> : null}
            </Pressable>
          )}
        />
      </SafeAreaView>
    </Modal>
  );
}

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

/** Date of birth field: shows "15 Aug 2000" and opens a day / month / year picker. */
export function DateField({
  label,
  value,
  onChange,
  minYear = 1960,
  maxYear = new Date().getFullYear(),
}: {
  label: string;
  value: Date | null;
  onChange: (d: Date) => void;
  minYear?: number;
  maxYear?: number;
}) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const initial = value ?? new Date(maxYear - 22, 0, 1);
  const [day, setDay] = useState(initial.getDate());
  const [month, setMonth] = useState(initial.getMonth());
  const [year, setYear] = useState(initial.getFullYear());
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const years = useMemo(() => Array.from({ length: maxYear - minYear + 1 }, (_, i) => maxYear - i), [minYear, maxYear]);

  const confirm = () => {
    onChange(new Date(year, month, Math.min(day, daysInMonth)));
    setOpen(false);
  };

  const column = (items: { key: number; label: string }[], selected: number, set: (n: number) => void) => (
    <ScrollView style={styles.dateCol} contentContainerStyle={{ paddingVertical: 4 }} showsVerticalScrollIndicator={false}>
      {items.map((it) => {
        const on = it.key === selected;
        return (
          <Pressable
            key={it.key}
            accessibilityRole="button"
            accessibilityState={{ selected: on }}
            onPress={() => set(it.key)}
            style={[styles.dateItem, on && { backgroundColor: colors.primarySoft }]}
          >
            <Text weight={on ? '800' : '500'} tone={on ? 'primary' : 'default'}>
              {it.label}
            </Text>
          </Pressable>
        );
      })}
    </ScrollView>
  );

  return (
    <View style={{ gap: 6 }}>
      <Text variant="caption" weight="700">
        {label}
      </Text>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel={`${label}: ${value ? formatDob(value) : ''}`}
        onPress={() => setOpen(true)}
        style={[styles.formBox, { borderColor: colors.border, backgroundColor: colors.surface }]}
      >
        <Text style={{ flex: 1 }} tone={value ? 'default' : 'muted'}>
          {value ? formatDob(value) : 'DD MMM YYYY'}
        </Text>
        <Ionicons name="calendar-outline" size={18} color={colors.textMuted} />
      </Pressable>
      <Modal visible={open} animationType="slide" transparent onRequestClose={() => setOpen(false)}>
        <Pressable style={[styles.backdrop, { backgroundColor: colors.overlay }]} onPress={() => setOpen(false)} />
        <SafeAreaView edges={['bottom']} style={[styles.sheet, { backgroundColor: colors.surface }]}>
          <View style={styles.sheetHeader}>
            <Text variant="heading" weight="800">
              {label}
            </Text>
            <Pressable accessibilityRole="button" onPress={confirm} hitSlop={8}>
              <Text weight="800" tone="primary">
                {t('common.done')}
              </Text>
            </Pressable>
          </View>
          <View style={styles.dateCols}>
            {column(
              Array.from({ length: daysInMonth }, (_, i) => ({ key: i + 1, label: String(i + 1).padStart(2, '0') })),
              day,
              setDay,
            )}
            {column(
              MONTHS.map((m, i) => ({ key: i, label: m })),
              month,
              setMonth,
            )}
            {column(
              years.map((y) => ({ key: y, label: String(y) })),
              year,
              setYear,
            )}
          </View>
        </SafeAreaView>
      </Modal>
    </View>
  );
}

export function formatDob(d: Date): string {
  return `${String(d.getDate()).padStart(2, '0')} ${MONTHS[d.getMonth()]} ${d.getFullYear()}`;
}

const styles = StyleSheet.create({
  header: { flexDirection: 'row', alignItems: 'center', minHeight: 54, paddingHorizontal: 12, gap: 6 },
  headerBack: { padding: 6 },
  headerTitle: { flex: 1, flexDirection: 'row', alignItems: 'center', gap: 6, paddingLeft: 4 },
  headerRight: { flexDirection: 'row', alignItems: 'center', gap: 2 },
  pill: { flexDirection: 'row', alignItems: 'center', gap: 3, paddingHorizontal: 8, paddingVertical: 3, borderRadius: 6, alignSelf: 'flex-start' },
  chips: { gap: 8, paddingHorizontal: 16, paddingVertical: 4 },
  chip: { paddingHorizontal: 14, paddingVertical: 7, borderRadius: 8, borderWidth: 1 },
  tabPills: { flexDirection: 'row', borderRadius: 10, padding: 3, gap: 3 },
  tabPill: { flex: 1, alignItems: 'center', paddingVertical: 8, borderRadius: 8 },
  menuRow: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 13, paddingHorizontal: 14 },
  empty: { alignItems: 'center', justifyContent: 'center', gap: 8, padding: 24 },
  backdrop: { flex: 1 },
  sheet: { position: 'absolute', left: 0, right: 0, bottom: 0, maxHeight: '80%', borderTopLeftRadius: 20, borderTopRightRadius: 20 },
  sheetHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: 16 },
  option: { flexDirection: 'row', alignItems: 'center', paddingVertical: 13, paddingHorizontal: 16, gap: 8 },
  dropdown: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 12, paddingVertical: 7, borderRadius: 10, borderWidth: 1.5, alignSelf: 'flex-start' },
  formBox: { flexDirection: 'row', alignItems: 'center', gap: 8, minHeight: 46, borderWidth: 1, borderRadius: 10, paddingHorizontal: 12 },
  dateCols: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingBottom: 16 },
  dateCol: { flex: 1, maxHeight: 260 },
  dateItem: { paddingVertical: 10, alignItems: 'center', borderRadius: 8 },
});
