import { useRouter } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';

import { Button, EmptyState } from '@/components/ui';
import { useTheme } from '@/theme';

export default function NotFoundScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors } = useTheme();
  return (
    <View style={{ flex: 1, justifyContent: 'center', backgroundColor: colors.background }}>
      <EmptyState icon="compass-outline" title={t('notFound.title')}>
        <Button label={t('notFound.home')} onPress={() => router.replace('/')} />
      </EmptyState>
    </View>
  );
}
