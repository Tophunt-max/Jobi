import { Ionicons } from '@expo/vector-icons';
import type { ComponentProps, ReactNode } from 'react';
import {
  Pressable,
  ScrollView,
  StyleSheet,
  View,
  type ScrollViewProps,
  type StyleProp,
  type ViewStyle,
} from 'react-native';

import { MAX_CONTENT_WIDTH, useTheme } from '@/theme';

import { Text } from './Text';

/** Centres content and caps its width on tablets and desktop web. */
export function Container({ children, style }: { children: ReactNode; style?: StyleProp<ViewStyle> }) {
  return <View style={[styles.container, style]}>{children}</View>;
}

export function ScrollScreen({ children, contentContainerStyle, ...rest }: ScrollViewProps & { children: ReactNode }) {
  const { colors, spacing } = useTheme();
  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: colors.background }}
      contentContainerStyle={[{ padding: spacing.lg, paddingBottom: spacing.xxl * 2 }, contentContainerStyle]}
      keyboardShouldPersistTaps="handled"
      {...rest}
    >
      <Container style={{ gap: spacing.lg }}>{children}</Container>
    </ScrollView>
  );
}

export function Card({
  children,
  style,
  onPress,
  accessibilityLabel,
}: {
  children: ReactNode;
  style?: StyleProp<ViewStyle>;
  onPress?: () => void;
  accessibilityLabel?: string;
}) {
  const { colors, radius, spacing } = useTheme();
  const base: ViewStyle = {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: radius.lg,
    padding: spacing.lg,
    gap: spacing.sm,
  };
  if (!onPress) return <View style={[base, style]}>{children}</View>;
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
      onPress={onPress}
      style={({ pressed }) => [base, { opacity: pressed ? 0.9 : 1 }, style]}
    >
      {children}
    </Pressable>
  );
}

export function Section({
  title,
  action,
  onAction,
  children,
}: {
  title: string;
  action?: string;
  onAction?: () => void;
  children: ReactNode;
}) {
  const { spacing } = useTheme();
  return (
    <View style={{ gap: spacing.sm }}>
      <View style={styles.sectionHeader}>
        <Text variant="heading" accessibilityRole="header">
          {title}
        </Text>
        {action && onAction ? (
          <Pressable accessibilityRole="button" onPress={onAction} hitSlop={8}>
            <Text variant="caption" tone="primary" weight="700">
              {action}
            </Text>
          </Pressable>
        ) : null}
      </View>
      {children}
    </View>
  );
}

type IconName = ComponentProps<typeof Ionicons>['name'];

export function EmptyState({
  icon = 'file-tray-outline',
  title,
  body,
  children,
}: {
  icon?: IconName;
  title: string;
  body?: string;
  children?: ReactNode;
}) {
  const { colors, spacing } = useTheme();
  return (
    <View style={[styles.empty, { gap: spacing.sm, padding: spacing.xl }]}>
      <Ionicons name={icon} size={40} color={colors.textMuted} />
      <Text variant="subheading" align="center">
        {title}
      </Text>
      {body ? (
        <Text tone="muted" align="center">
          {body}
        </Text>
      ) : null}
      {children}
    </View>
  );
}

type BannerTone = 'info' | 'warning' | 'danger' | 'success';

export function InlineBanner({
  tone = 'info',
  icon,
  message,
  action,
  onAction,
}: {
  tone?: BannerTone;
  icon?: IconName;
  message: string;
  action?: string;
  onAction?: () => void;
}) {
  const { colors, radius, spacing } = useTheme();
  const map = {
    info: [colors.infoSoft, colors.info],
    warning: [colors.warningSoft, colors.warning],
    danger: [colors.dangerSoft, colors.danger],
    success: [colors.successSoft, colors.success],
  } as const;
  const [bg, fg] = map[tone];
  return (
    <View
      accessibilityRole="alert"
      style={[styles.banner, { backgroundColor: bg, borderRadius: radius.md, padding: spacing.md, gap: spacing.sm }]}
    >
      {icon ? <Ionicons name={icon} size={18} color={fg} /> : null}
      <Text variant="caption" style={{ color: fg, flex: 1 }}>
        {message}
      </Text>
      {action && onAction ? (
        <Pressable accessibilityRole="button" onPress={onAction} hitSlop={8}>
          <Text variant="caption" weight="800" style={{ color: fg }}>
            {action}
          </Text>
        </Pressable>
      ) : null}
    </View>
  );
}

export function Divider() {
  const { colors } = useTheme();
  return <View style={{ height: StyleSheet.hairlineWidth, backgroundColor: colors.border }} />;
}

export function Row({ children, style, gap = 8 }: { children: ReactNode; style?: StyleProp<ViewStyle>; gap?: number }) {
  return <View style={[{ flexDirection: 'row', alignItems: 'center', gap }, style]}>{children}</View>;
}

const styles = StyleSheet.create({
  container: { width: '100%', maxWidth: MAX_CONTENT_WIDTH, alignSelf: 'center' },
  sectionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  empty: { alignItems: 'center', justifyContent: 'center' },
  banner: { flexDirection: 'row', alignItems: 'center' },
});
