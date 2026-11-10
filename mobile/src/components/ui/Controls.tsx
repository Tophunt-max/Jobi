import { Ionicons } from '@expo/vector-icons';
import { useState, type ComponentProps } from 'react';
import {
  FlatList,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  TextInput,
  View,
  type KeyboardTypeOptions,
  type StyleProp,
  type ViewStyle,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { i18n } from '@/i18n';
import { useTheme } from '@/theme';

import { Text } from './Text';

type IconName = ComponentProps<typeof Ionicons>['name'];

export function Chip({
  label,
  selected,
  onPress,
  icon,
  count,
}: {
  label: string;
  selected?: boolean;
  onPress?: () => void;
  icon?: string;
  count?: number;
}) {
  const { colors, radius } = useTheme();
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ selected: !!selected }}
      onPress={onPress}
      style={({ pressed }) => [
        styles.chip,
        {
          borderRadius: radius.pill,
          backgroundColor: selected ? colors.primary : colors.surface,
          borderColor: selected ? colors.primary : colors.border,
          opacity: pressed ? 0.8 : 1,
        },
      ]}
    >
      <Text variant="caption" weight="700" style={{ color: selected ? colors.onPrimary : colors.text }} numberOfLines={1}>
        {icon ? `${icon} ` : ''}
        {label}
        {count !== undefined ? ` · ${count}` : ''}
      </Text>
    </Pressable>
  );
}

export function ChipRow({ children }: { children: React.ReactNode }) {
  return (
    <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.chipRow}>
      {children}
    </ScrollView>
  );
}

export type BadgeTone = 'neutral' | 'primary' | 'success' | 'danger' | 'warning' | 'info' | 'accent';

export function Badge({ label, tone = 'neutral', icon }: { label: string; tone?: BadgeTone; icon?: IconName }) {
  const { colors, radius } = useTheme();
  const map: Record<BadgeTone, [string, string]> = {
    neutral: [colors.surfaceMuted, colors.textMuted],
    primary: [colors.primarySoft, colors.primary],
    success: [colors.successSoft, colors.success],
    danger: [colors.dangerSoft, colors.danger],
    warning: [colors.warningSoft, colors.warning],
    info: [colors.infoSoft, colors.info],
    accent: [colors.accentSoft, colors.accent],
  };
  const [bg, fg] = map[tone];
  return (
    <View style={[styles.badge, { backgroundColor: bg, borderRadius: radius.sm }]}>
      {icon ? <Ionicons name={icon} size={11} color={fg} /> : null}
      <Text variant="tiny" style={{ color: fg }} numberOfLines={1}>
        {label}
      </Text>
    </View>
  );
}

export function TextField({
  label,
  value,
  onChangeText,
  placeholder,
  multiline,
  keyboardType,
  maxLength,
  secureTextEntry,
  autoCapitalize,
  style,
}: {
  label?: string;
  value: string;
  onChangeText: (v: string) => void;
  placeholder?: string;
  multiline?: boolean;
  keyboardType?: KeyboardTypeOptions;
  maxLength?: number;
  secureTextEntry?: boolean;
  autoCapitalize?: 'none' | 'sentences' | 'words' | 'characters';
  style?: StyleProp<ViewStyle>;
}) {
  const { colors, radius, typography } = useTheme();
  return (
    <View style={[{ gap: 4 }, style]}>
      {label ? (
        <Text variant="caption" tone="muted">
          {label}
        </Text>
      ) : null}
      <TextInput
        accessibilityLabel={label ?? placeholder}
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.textMuted}
        multiline={multiline}
        keyboardType={keyboardType}
        maxLength={maxLength}
        secureTextEntry={secureTextEntry}
        autoCapitalize={autoCapitalize}
        style={[
          typography.body,
          styles.input,
          {
            color: colors.text,
            backgroundColor: colors.surface,
            borderColor: colors.border,
            borderRadius: radius.md,
            minHeight: multiline ? 96 : 44,
            textAlignVertical: multiline ? 'top' : 'center',
          },
        ]}
      />
    </View>
  );
}

export function NumberField({
  label,
  value,
  onChange,
  min = 0,
  max = 999,
  decimals = false,
}: {
  label: string;
  value: number;
  onChange: (n: number) => void;
  min?: number;
  max?: number;
  decimals?: boolean;
}) {
  const [text, setText] = useState(String(value));
  return (
    <TextField
      label={label}
      value={text}
      keyboardType={decimals ? 'decimal-pad' : 'number-pad'}
      onChangeText={(t) => {
        const cleaned = t.replace(decimals ? /[^0-9.]/g : /[^0-9]/g, '');
        setText(cleaned);
        const n = Number(cleaned);
        if (cleaned !== '' && Number.isFinite(n)) onChange(Math.min(max, Math.max(min, n)));
      }}
      maxLength={6}
    />
  );
}

export function ToggleRow({
  label,
  description,
  value,
  onValueChange,
  disabled,
}: {
  label: string;
  description?: string;
  value: boolean;
  onValueChange: (v: boolean) => void;
  disabled?: boolean;
}) {
  const { colors } = useTheme();
  return (
    <View style={styles.toggleRow}>
      <View style={{ flex: 1, gap: 2 }}>
        <Text variant="subheading">{label}</Text>
        {description ? (
          <Text variant="caption" tone="muted">
            {description}
          </Text>
        ) : null}
      </View>
      <Switch
        accessibilityLabel={label}
        value={value}
        onValueChange={onValueChange}
        disabled={disabled}
        trackColor={{ true: colors.primary, false: colors.border }}
      />
    </View>
  );
}

export function Segmented<T extends string>({
  options,
  value,
  onChange,
}: {
  options: { value: T; label: string }[];
  value: T;
  onChange: (v: T) => void;
}) {
  const { colors, radius } = useTheme();
  return (
    <View style={[styles.segment, { backgroundColor: colors.surfaceMuted, borderRadius: radius.md }]}>
      {options.map((o) => {
        const active = o.value === value;
        return (
          <Pressable
            key={o.value}
            accessibilityRole="button"
            accessibilityState={{ selected: active }}
            onPress={() => onChange(o.value)}
            style={[
              styles.segmentItem,
              { borderRadius: radius.sm, backgroundColor: active ? colors.surface : 'transparent' },
            ]}
          >
            <Text variant="caption" weight="700" tone={active ? 'primary' : 'muted'} numberOfLines={1}>
              {o.label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

export type PickerOption<T extends string> = { value: T; label: string };

/** A field that opens a searchable list in a modal. Works the same on every platform. */
export function SelectField<T extends string>({
  label,
  value,
  options,
  onChange,
  searchable,
}: {
  label: string;
  value: T;
  options: PickerOption<T>[];
  onChange: (v: T) => void;
  searchable?: boolean;
}) {
  const { colors, radius, spacing } = useTheme();
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const current = options.find((o) => o.value === value);
  const filtered = query
    ? options.filter((o) => o.label.toLowerCase().includes(query.toLowerCase()))
    : options;

  return (
    <View style={{ gap: 4 }}>
      <Text variant="caption" tone="muted">
        {label}
      </Text>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel={`${label}: ${current?.label ?? ''}`}
        onPress={() => setOpen(true)}
        style={[
          styles.input,
          styles.select,
          { borderColor: colors.border, backgroundColor: colors.surface, borderRadius: radius.md },
        ]}
      >
        <Text numberOfLines={1} style={{ flex: 1 }}>
          {current?.label ?? '—'}
        </Text>
        <Ionicons name="chevron-down" size={16} color={colors.textMuted} />
      </Pressable>
      <Modal visible={open} animationType="slide" transparent onRequestClose={() => setOpen(false)}>
        <Pressable style={[styles.backdrop, { backgroundColor: colors.overlay }]} onPress={() => setOpen(false)} />
        <SafeAreaView
          edges={['bottom']}
          style={[styles.sheet, { backgroundColor: colors.surface, borderTopLeftRadius: radius.xl, borderTopRightRadius: radius.xl }]}
        >
          <View style={{ padding: spacing.lg, gap: spacing.md }}>
            <View style={styles.sheetHeader}>
              <Text variant="heading">{label}</Text>
              <Pressable accessibilityRole="button" accessibilityLabel={i18n.t('common.close')} onPress={() => setOpen(false)} hitSlop={8}>
                <Ionicons name="close" size={22} color={colors.text} />
              </Pressable>
            </View>
            {searchable ? <TextField value={query} onChangeText={setQuery} placeholder={i18n.t('common.search')} /> : null}
          </View>
          <FlatList
            data={filtered}
            keyExtractor={(o) => o.value}
            style={{ maxHeight: 420 }}
            renderItem={({ item }) => (
              <Pressable
                accessibilityRole="button"
                accessibilityState={{ selected: item.value === value }}
                onPress={() => {
                  onChange(item.value);
                  setOpen(false);
                  setQuery('');
                }}
                style={({ pressed }) => [
                  styles.option,
                  { paddingHorizontal: spacing.lg, backgroundColor: pressed ? colors.surfaceMuted : 'transparent' },
                ]}
              >
                <Text style={{ flex: 1 }} weight={item.value === value ? '700' : '400'}>
                  {item.label}
                </Text>
                {item.value === value ? <Ionicons name="checkmark" size={18} color={colors.primary} /> : null}
              </Pressable>
            )}
          />
        </SafeAreaView>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  chip: { paddingHorizontal: 12, paddingVertical: 7, borderWidth: 1 },
  chipRow: { gap: 8, paddingVertical: 2 },
  badge: { flexDirection: 'row', alignItems: 'center', gap: 3, paddingHorizontal: 6, paddingVertical: 3, alignSelf: 'flex-start' },
  input: { borderWidth: 1, paddingHorizontal: 12, paddingVertical: 10 },
  select: { flexDirection: 'row', alignItems: 'center', gap: 8, minHeight: 44 },
  toggleRow: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 6 },
  segment: { flexDirection: 'row', padding: 3, gap: 3 },
  segmentItem: { flex: 1, alignItems: 'center', paddingVertical: 8, paddingHorizontal: 6 },
  backdrop: { flex: 1 },
  sheet: { position: 'absolute', left: 0, right: 0, bottom: 0, maxHeight: '80%' },
  sheetHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  option: { flexDirection: 'row', alignItems: 'center', paddingVertical: 14, gap: 8 },
});
