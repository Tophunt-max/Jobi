import { useMemo } from 'react';

import { useBookmarkedJobs } from '@/features/jobs/hooks';
import { deadlineInboxItems } from '@/features/reminders/schedule';
import { useContentStore } from '@/store/contentStore';
import { useUserStore } from '@/store/userStore';
import type { AppNotification } from '@/types/domain';

export type InboxItem = AppNotification & { read: boolean };

/** Server notifications plus deadline alerts for saved notices, newest first. */
export function useInbox(): { items: InboxItem[]; unread: number } {
  const server = useContentStore((s) => s.notifications);
  const readIds = useUserStore((s) => s.readNotificationIds);
  const bookmarks = useBookmarkedJobs();

  return useMemo(() => {
    const read = new Set(readIds);
    const merged = [...deadlineInboxItems(bookmarks), ...server]
      .sort((a, b) => b.timestamp - a.timestamp)
      .map((n) => ({ ...n, read: read.has(n.id) }));
    return { items: merged, unread: merged.filter((n) => !n.read).length };
  }, [server, readIds, bookmarks]);
}
