import { useTranslation } from 'react-i18next';

import { InlineBanner } from '@/components/ui';
import { relativeTime } from '@/lib/dates';
import { useContentStore } from '@/store/contentStore';

/** Shows offline / not-configured state. Silent when the last sync worked. */
export function SyncStatus() {
  const { t } = useTranslation();
  const status = useContentStore((s) => s.status);
  const lastSyncedAt = useContentStore((s) => s.lastSyncedAt);
  const hasData = useContentStore((s) => s.jobs.length > 0);
  const sync = useContentStore((s) => s.sync);

  if (status === 'not_configured') {
    return <InlineBanner tone="danger" icon="cloud-offline-outline" message={t('sync.notConfigured')} />;
  }
  if (status === 'offline') {
    const message = hasData
      ? `${t('sync.offline')}${lastSyncedAt ? ` ${t('sync.updated', { time: relativeTime(lastSyncedAt) })}` : ''}`
      : t('sync.emptyOffline');
    return (
      <InlineBanner
        tone="warning"
        icon="cloud-offline-outline"
        message={message}
        action={t('common.retry')}
        onAction={() => void sync({ force: true })}
      />
    );
  }
  if (status === 'syncing' && !hasData) {
    return <InlineBanner tone="info" icon="sync-outline" message={t('sync.firstLoad')} />;
  }
  return null;
}
