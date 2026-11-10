import { useTranslation } from 'react-i18next';
import { View } from 'react-native';

import { Button, EmptyState } from '@/components/ui';
import { useTheme } from '@/theme';

/**
 * Most government portals refuse to load inside an iframe (X-Frame-Options), so on
 * web the link opens in a new tab. useOpenLink() already does this; this screen
 * only appears when someone opens /browser directly.
 */
export function InAppBrowser({ url }: { url: string; title?: string }) {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const ok = url.startsWith('https://');
  return (
    <View style={{ flex: 1, justifyContent: 'center', backgroundColor: colors.background }}>
      <EmptyState icon="open-outline" title={ok ? url : t('browser.blocked')}>
        {ok ? (
          <Button label={t('browser.openExternal')} onPress={() => window.open(url, '_blank', 'noopener,noreferrer')} />
        ) : null}
      </EmptyState>
    </View>
  );
}
