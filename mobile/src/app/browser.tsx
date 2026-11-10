import { useLocalSearchParams } from 'expo-router';

import { InAppBrowser } from '@/features/browser/InAppBrowser';

export default function BrowserScreen() {
  const { url = '', title } = useLocalSearchParams<{ url: string; title?: string }>();
  return <InAppBrowser url={url} title={title} />;
}
