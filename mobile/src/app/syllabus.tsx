import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';

import { images } from '@/assets/images';
import { Text } from '@/components/ui';
import { ContentCard } from '@/features/content/ContentCard';
import { ContentList } from '@/features/content/ContentList';
import { isPdfUrl } from '@/lib/format';
import { formatBytes, usePdfSize } from '@/lib/usePdfSize';
import { useOpenLink } from '@/lib/useOpenLink';
import { useContentStore } from '@/store/contentStore';
import type { SyllabusItem } from '@/types/domain';

export default function SyllabusScreen() {
  const { t } = useTranslation();
  const items = useContentStore((s) => s.syllabi);
  const text = useCallback((i: SyllabusItem) => `${i.examName} ${i.recruitingOrg} ${i.examPattern}`, []);

  return (
    <ContentList
      title={t('results.tabSyllabus')}
      items={items}
      text={text}
      renderItem={(item, index) => <SyllabusRow item={item} index={index} label={t('results.download')} />}
    />
  );
}

function SyllabusRow({ item, index, label }: { item: SyllabusItem; index: number; label: string }) {
  const { t } = useTranslation();
  const openLink = useOpenLink();
  const pdf = isPdfUrl(item.url);
  const size = usePdfSize(pdf ? item.url : '');
  const subtitle = item.examPattern || item.subjects.map((s) => s.name).slice(0, 3).join(', ') || item.recruitingOrg;
  const meta = [pdf ? 'PDF' : t('results.officialLink'), size ? formatBytes(size) : '', item.durationMinutes > 0 ? t('results.duration', { count: item.durationMinutes }) : '']
    .filter(Boolean)
    .join(' • ');
  return (
    <ContentCard
      index={index + 4}
      icon={images.content.pdf}
      title={item.examName}
      subtitle={subtitle}
      onPress={item.url ? () => void openLink(item.url, item.examName) : undefined}
      onDownload={item.url ? () => void openLink(item.url, item.examName) : undefined}
      downloadLabel={label}
      meta={
        <>
          <Text variant="caption" weight="600" tone="muted">
            {meta}
          </Text>
          {item.negativeMarkingRule ? (
            <Text variant="caption" tone="danger" numberOfLines={1}>
              {t('results.negative')}: {item.negativeMarkingRule}
            </Text>
          ) : null}
        </>
      }
    />
  );
}
