import { Ionicons } from '@expo/vector-icons';
import type { ReactNode } from 'react';
import { Image, Pressable, StyleSheet, View, type ImageSourcePropType } from 'react-native';

import { listArt, type ListArtKind } from '@/assets/images';

import { ArtIcon, Surface, Text } from '@/components/ui';
import { useTheme } from '@/theme';

import { LIST_TINTS, tintFor } from './ContentList';

/**
 * List row used by the content screens: icon, title, organisation, meta line and an action.
 * `art` picks a full-colour icon set; otherwise `icon` is tinted with the row colour.
 */
export function ContentCard({
  index,
  icon,
  art,
  title,
  subtitle,
  meta,
  footer,
  onPress,
  onDownload,
  downloadLabel,
}: {
  index: number;
  icon: ImageSourcePropType;
  art?: ListArtKind;
  title: string;
  subtitle?: string;
  meta?: ReactNode;
  footer?: ReactNode;
  onPress?: () => void;
  onDownload?: () => void;
  downloadLabel?: string;
}) {
  const { colors, scheme } = useTheme();
  const tint = tintFor(index, scheme === 'dark');
  return (
    <Surface onPress={onPress} accessibilityLabel={title} style={styles.card}>
      {art ? (
        <Image
          source={listArt[art][index % LIST_TINTS.length]!}
          style={[styles.art, scheme === 'dark' ? { opacity: 0.92 } : null]}
          accessibilityIgnoresInvertColors
        />
      ) : (
        <ArtIcon source={icon} size={46} tint={tint.fg} background={tint.bg} radius={12} padding={20} />
      )}
      <View style={styles.body}>
        <Text weight="800" numberOfLines={2} style={{ fontSize: 14, lineHeight: 19 }}>
          {title}
        </Text>
        {subtitle ? (
          <Text variant="caption" tone="muted" numberOfLines={1}>
            {subtitle}
          </Text>
        ) : null}
        {meta}
        {footer}
      </View>
      {onDownload ? (
        <Pressable accessibilityRole="button" accessibilityLabel={downloadLabel} onPress={onDownload} hitSlop={10} style={styles.download}>
          <Ionicons name="download-outline" size={22} color={colors.primary} />
        </Pressable>
      ) : null}
    </Surface>
  );
}

/** Small soft action button, e.g. "View Result". */
export function SoftAction({ label, onPress, tone = 'green' }: { label: string; onPress: () => void; tone?: 'green' | 'blue' }) {
  const { scheme } = useTheme();
  const dark = scheme === 'dark';
  const bg = tone === 'green' ? (dark ? '#064E3B' : '#DCFCE7') : dark ? '#1E3A8A' : '#DBEAFE';
  const fg = tone === 'green' ? (dark ? '#6EE7B7' : '#15803D') : dark ? '#93C5FD' : '#1D4ED8';
  return (
    <Pressable accessibilityRole="button" onPress={onPress} style={({ pressed }) => [styles.soft, { backgroundColor: bg, opacity: pressed ? 0.8 : 1 }]}>
      <Text variant="caption" weight="800" style={{ color: fg }}>
        {label}
      </Text>
      <Ionicons name="open-outline" size={13} color={fg} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: { flexDirection: 'row', gap: 12, alignItems: 'flex-start' },
  art: { width: 46, height: 46, borderRadius: 12 },
  body: { flex: 1, gap: 3 },
  download: { alignSelf: 'center', padding: 4 },
  soft: { flexDirection: 'row', alignItems: 'center', gap: 4, alignSelf: 'flex-start', paddingHorizontal: 10, paddingVertical: 6, borderRadius: 8, marginTop: 6 },
});
