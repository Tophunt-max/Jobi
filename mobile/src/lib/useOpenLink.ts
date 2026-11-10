import { useRouter } from 'expo-router';
import * as WebBrowser from 'expo-web-browser';
import { useCallback } from 'react';
import { Alert, Platform } from 'react-native';

import { i18n } from '@/i18n';

import { isHttpsUrl, isPdfUrl, toHttps } from './format';
import { createLogger } from './logger';

const log = createLogger('links');

/**
 * Opens official links.
 * - Web: a new browser tab.
 * - Native HTML pages: the in-app browser screen (https only).
 * - Native PDFs: the system browser sheet (Custom Tabs / Safari), which can render or download them.
 */
export function useOpenLink() {
  const router = useRouter();
  return useCallback(
    async (rawUrl: string, title?: string, opts?: { external?: boolean }) => {
      if (!rawUrl) return;
      const url = toHttps(rawUrl);
      if (!isHttpsUrl(url)) {
        Alert.alert(i18n.t('browser.blockedTitle'), i18n.t('browser.blocked'));
        return;
      }
      if (Platform.OS === 'web') {
        window.open(url, '_blank', 'noopener,noreferrer');
        return;
      }
      if (opts?.external || isPdfUrl(url)) {
        try {
          await WebBrowser.openBrowserAsync(url, {
            presentationStyle: WebBrowser.WebBrowserPresentationStyle.PAGE_SHEET,
          });
        } catch (e) {
          log.warn('system browser failed', e);
        }
        return;
      }
      router.push({ pathname: '/browser', params: { url, title: title ?? '' } });
    },
    [router],
  );
}
