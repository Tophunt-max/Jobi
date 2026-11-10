import { useRouter } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';

import { IconButton } from '@/components/ui';
import { useInbox } from '@/features/notifications/inbox';

export function HeaderActions() {
  const router = useRouter();
  const { t } = useTranslation();
  const { unread } = useInbox();
  return (
    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4, paddingHorizontal: 8 }}>
      <IconButton icon="language-outline" label={t('language.title')} onPress={() => router.push('/language')} />
      <IconButton
        icon="notifications-outline"
        label={t('notifications.title')}
        badge={unread}
        onPress={() => router.push('/notifications')}
      />
    </View>
  );
}
