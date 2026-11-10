import { Ionicons } from '@expo/vector-icons';
import { useRef, type ComponentRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Pressable, StyleSheet, TextInput, View, type StyleProp, type ViewStyle } from 'react-native';

import { useVoiceInput } from '@/lib/useVoiceInput';
import { useTheme } from '@/theme';

/** Rounded search box with an optional microphone button. */
export function SearchInput({
  value,
  onChangeText,
  placeholder,
  onSubmit,
  mic,
  style,
  autoFocus,
}: {
  value: string;
  onChangeText: (v: string) => void;
  placeholder: string;
  onSubmit?: (v: string) => void;
  mic?: boolean;
  style?: StyleProp<ViewStyle>;
  autoFocus?: boolean;
}) {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const inputRef = useRef<ComponentRef<typeof TextInput>>(null);
  const voice = useVoiceInput((text) => {
    onChangeText(text);
    onSubmit?.(text);
  });

  return (
    <View style={[styles.box, { backgroundColor: colors.surface, borderColor: colors.border }, style]}>
      <Ionicons name="search" size={18} color={colors.textMuted} />
      <TextInput
        ref={inputRef}
        accessibilityLabel={placeholder}
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.textMuted}
        returnKeyType="search"
        autoFocus={autoFocus}
        onSubmitEditing={() => onSubmit?.(value.trim())}
        style={[styles.input, { color: colors.text }]}
      />
      {value ? (
        <Pressable accessibilityRole="button" accessibilityLabel={t('common.clear')} onPress={() => onChangeText('')} hitSlop={8}>
          <Ionicons name="close-circle" size={18} color={colors.textMuted} />
        </Pressable>
      ) : null}
      {mic ? (
        <Pressable
          accessibilityRole="button"
          accessibilityLabel={t('common.voiceSearch')}
          hitSlop={8}
          onPress={() => (voice.supported ? voice.start() : inputRef.current?.focus())}
        >
          <Ionicons name={voice.listening ? 'mic' : 'mic-outline'} size={20} color={voice.listening ? colors.danger : colors.primary} />
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  box: { flexDirection: 'row', alignItems: 'center', gap: 8, paddingHorizontal: 12, borderRadius: 12, borderWidth: 1, minHeight: 46 },
  input: { flex: 1, paddingVertical: 11, fontSize: 14 },
});
