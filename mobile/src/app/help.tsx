import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Linking, Pressable, ScrollView, StyleSheet, View } from 'react-native';

import { MenuRow, Page, ScreenHeader, Surface, Text } from '@/components/ui';
import { env } from '@/config/env';
import { useTheme } from '@/theme';

const FAQ = ['1', '2', '3', '4', '5'] as const;

export default function HelpScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors } = useTheme();
  const [open, setOpen] = useState<string | null>('1');

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader title={t('help.title')} />
      <ScrollView contentContainerStyle={{ paddingVertical: 16, paddingBottom: 32 }}>
        <Page style={{ gap: 14 }}>
          <Text variant="caption" weight="800" tone="muted" style={{ marginLeft: 4 }}>
            {t('help.faq').toUpperCase()}
          </Text>
          <Surface padded={false}>
            {FAQ.map((k, i) => {
              const expanded = open === k;
              return (
                <View key={k} style={[{ borderBottomColor: colors.border, borderBottomWidth: i === FAQ.length - 1 ? 0 : StyleSheet.hairlineWidth }]}>
                  <Pressable
                    accessibilityRole="button"
                    accessibilityState={{ expanded }}
                    onPress={() => setOpen(expanded ? null : k)}
                    style={styles.faqHead}
                  >
                    <Text weight="700" style={{ flex: 1 }}>
                      {t(`help.q${k}`)}
                    </Text>
                    <Ionicons name={expanded ? 'chevron-up' : 'chevron-down'} size={18} color={colors.textMuted} />
                  </Pressable>
                  {expanded ? (
                    <Text tone="muted" style={{ paddingHorizontal: 14, paddingBottom: 14 }}>
                      {t(`help.a${k}`)}
                    </Text>
                  ) : null}
                </View>
              );
            })}
          </Surface>

          <Text variant="caption" weight="800" tone="muted" style={{ marginLeft: 4 }}>
            {t('help.contact').toUpperCase()}
          </Text>
          <Surface padded={false}>
            <MenuRow icon="flag-outline" label={t('help.reportWrong')} subtitle={t('help.reportWrongHint')} onPress={() => router.navigate('/jobs')} />
            {env.supportEmail ? (
              <MenuRow icon="mail-outline" label={t('help.email')} value={env.supportEmail} onPress={() => void Linking.openURL(`mailto:${env.supportEmail}`)} />
            ) : null}
            <MenuRow icon="information-circle-outline" label={t('about.title')} onPress={() => router.push('/about')} last />
          </Surface>
        </Page>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  faqHead: { flexDirection: 'row', alignItems: 'center', gap: 10, padding: 14 },
});
