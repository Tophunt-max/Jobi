import { Text as RNText, type TextProps as RNTextProps, type TextStyle } from 'react-native';

import { useTheme, type ThemeColors } from '@/theme';

type Variant = 'title' | 'heading' | 'subheading' | 'body' | 'caption' | 'tiny';
type Tone = 'default' | 'muted' | 'primary' | 'success' | 'danger' | 'warning' | 'inverse' | 'accent';

export type TextProps = RNTextProps & {
  variant?: Variant;
  tone?: Tone;
  weight?: TextStyle['fontWeight'];
  align?: TextStyle['textAlign'];
};

const toneKey: Record<Tone, keyof ThemeColors> = {
  default: 'text',
  muted: 'textMuted',
  primary: 'primary',
  success: 'success',
  danger: 'danger',
  warning: 'warning',
  inverse: 'textInverse',
  accent: 'accent',
};

export function Text({ variant = 'body', tone = 'default', weight, align, style, ...rest }: TextProps) {
  const { colors, typography } = useTheme();
  return (
    <RNText
      {...rest}
      style={[
        typography[variant],
        { color: colors[toneKey[tone]] },
        weight ? { fontWeight: weight } : null,
        align ? { textAlign: align } : null,
        style,
      ]}
    />
  );
}
