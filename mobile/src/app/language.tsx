import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Alert, Pressable, StyleSheet, View } from 'react-native';

import { Card, InlineBanner, ScrollScreen, Text } from '@/components/ui';
import { applyDirection, initI18n, LANGUAGES } from '@/i18n';
import { useSettingsStore } from '@/store/settingsStore';
import { useTheme } from '@/theme';

export default function LanguageScreen() {
  const { t, i18n } = useTranslation();
  const router = useRouter();
  const { colors } = useTheme();
  const setLanguage = useSettingsStore((s) => s.setLanguage);
  const [current, setCurrent] = useState(i18n.language);
  const meta = LANGUAGES.find((l) => l.code === current);

  const choose = (code: string) => {
    setCurrent(code);
    setLanguage(code);
    initI18n(code);
    if (applyDirection(code)) {
      Alert.alert(t('language.title'), t('language.rtlRestart'));
    } else if (router.canGoBack()) {
      router.back();
    }
  };

  return (
    <ScrollScreen>
      {meta && !meta.complete ? <InlineBanner tone="info" message={t('language.partial')} /> : null}
      <Card style={{ paddingVertical: 4 }}>
        {LANGUAGES.map((l, i) => (
          <Pressable
            key={l.code}
            accessibilityRole="button"
            accessibilityState={{ selected: l.code === current }}
            onPress={() => choose(l.code)}
            style={[styles.row, i > 0 && { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: colors.border }]}
          >
            <View style={{ flex: 1 }}>
              <Text variant="subheading">{l.nativeName}</Text>
              <Text variant="caption" tone="muted">
                {l.englishName}
              </Text>
            </View>
            {l.code === current ? <Ionicons name="checkmark-circle" size={22} color={colors.primary} /> : null}
          </Pressable>
        ))}
      </Card>
    </ScrollScreen>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12 },
});
