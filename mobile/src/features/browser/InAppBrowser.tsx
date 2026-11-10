import * as WebBrowser from 'expo-web-browser';
import { Stack } from 'expo-router';
import { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ActivityIndicator, Linking, StyleSheet, View } from 'react-native';
import { WebView, type WebViewNavigation } from 'react-native-webview';
import type { ShouldStartLoadRequest } from 'react-native-webview/lib/WebViewTypes';

import { Button, EmptyState, IconButton, Row, Text } from '@/components/ui';
import { hostnameOf, isPdfUrl } from '@/lib/format';
import { useTheme } from '@/theme';

/** Schemes handed to the OS (dialer, mail, UPI apps, Play Store) instead of the WebView. */
const EXTERNAL_SCHEMES = ['tel:', 'mailto:', 'sms:', 'upi:', 'market:', 'intent:'];

/**
 * Locked-down WebView for official portals: https only, no file access,
 * no mixed content, no JS bridge. PDFs go to the system browser.
 */
export function InAppBrowser({ url, title }: { url: string; title?: string }) {
  const { t } = useTranslation();
  const { colors } = useTheme();
  // The library's default generic (P = undefined) collapses the props to `never`; `object` keeps them.
  const ref = useRef<WebView<object>>(null);
  const [nav, setNav] = useState<{ url: string; title: string; canGoBack: boolean; canGoForward: boolean }>({
    url,
    title: title ?? '',
    canGoBack: false,
    canGoForward: false,
  });
  const [failed, setFailed] = useState(false);
  const [progress, setProgress] = useState(0);

  if (!url.startsWith('https://')) {
    return <EmptyState icon="lock-closed-outline" title={t('browser.blocked')} />;
  }

  const onShouldStart = (req: ShouldStartLoadRequest) => {
    const target = req.url;
    if (target.startsWith('https://')) {
      if (isPdfUrl(target) && req.isTopFrame) {
        void WebBrowser.openBrowserAsync(target);
        return false;
      }
      return true;
    }
    if (target === 'about:blank') return true;
    if (EXTERNAL_SCHEMES.some((s) => target.startsWith(s))) {
      void Linking.openURL(target).catch(() => {});
    }
    // http:, file:, javascript:, data: and anything else are blocked.
    return false;
  };

  const onNav = (state: WebViewNavigation) =>
    setNav({ url: state.url, title: state.title, canGoBack: state.canGoBack, canGoForward: state.canGoForward });

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <Stack.Screen
        options={{
          headerTitle: () => (
            <View style={{ maxWidth: 220 }}>
              <Text variant="subheading" numberOfLines={1}>
                {nav.title || title || hostnameOf(nav.url)}
              </Text>
              <Text variant="tiny" tone="muted" numberOfLines={1}>
                🔒 {hostnameOf(nav.url)}
              </Text>
            </View>
          ),
          headerRight: () => (
            <IconButton icon="open-outline" label={t('browser.openExternal')} onPress={() => void WebBrowser.openBrowserAsync(nav.url)} />
          ),
        }}
      />
      {progress < 1 && !failed ? <View style={[styles.progress, { width: `${Math.max(progress, 0.05) * 100}%`, backgroundColor: colors.primary }]} /> : null}
      {failed ? (
        <EmptyState icon="cloud-offline-outline" title={t('browser.loadError')}>
          <Row>
            <Button
              variant="secondary"
              label={t('browser.reload')}
              icon="refresh"
              onPress={() => {
                setFailed(false);
                ref.current?.reload();
              }}
            />
            <Button variant="ghost" label={t('browser.openExternal')} onPress={() => void WebBrowser.openBrowserAsync(nav.url)} />
          </Row>
        </EmptyState>
      ) : (
        <WebView<object>
          ref={ref}
          source={{ uri: url }}
          originWhitelist={['https://*']}
          onShouldStartLoadWithRequest={onShouldStart}
          onNavigationStateChange={onNav}
          onLoadProgress={(e) => setProgress(e.nativeEvent.progress)}
          onError={() => setFailed(true)}
          onHttpError={(e) => {
            if (e.nativeEvent.statusCode >= 500) setFailed(true);
          }}
          onRenderProcessGone={() => setFailed(true)}
          onContentProcessDidTerminate={() => ref.current?.reload()}
          startInLoadingState
          renderLoading={() => (
            <View style={StyleSheet.absoluteFill}>
              <ActivityIndicator style={{ marginTop: 40 }} color={colors.primary} />
            </View>
          )}
          javaScriptEnabled
          domStorageEnabled
          allowFileAccess={false}
          allowFileAccessFromFileURLs={false}
          allowUniversalAccessFromFileURLs={false}
          allowsLinkPreview={false}
          mixedContentMode="never"
          thirdPartyCookiesEnabled={false}
          setSupportMultipleWindows={false}
          javaScriptCanOpenWindowsAutomatically={false}
          mediaPlaybackRequiresUserAction
          allowsBackForwardNavigationGestures
          pullToRefreshEnabled
          style={{ flex: 1, backgroundColor: colors.background }}
        />
      )}
      <View style={[styles.toolbar, { backgroundColor: colors.surface, borderTopColor: colors.border }]}>
        <IconButton icon="chevron-back" label={t('common.back')} onPress={() => ref.current?.goBack()} color={nav.canGoBack ? colors.text : colors.border} />
        <IconButton icon="chevron-forward" label={t('browser.forward')} onPress={() => ref.current?.goForward()} color={nav.canGoForward ? colors.text : colors.border} />
        <IconButton icon="refresh" label={t('browser.reload')} onPress={() => ref.current?.reload()} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  progress: { height: 2 },
  toolbar: { flexDirection: 'row', justifyContent: 'space-around', paddingVertical: 6, borderTopWidth: StyleSheet.hairlineWidth },
});
