import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';

import { images } from '@/assets/images';
import { Pill, Text } from '@/components/ui';
import { ContentCard } from '@/features/content/ContentCard';
import { ContentList } from '@/features/content/ContentList';
import { formatDate } from '@/lib/dates';
import { useOpenLink } from '@/lib/useOpenLink';
import { useContentStore } from '@/store/contentStore';
import type { AdmitCardItem } from '@/types/domain';

export default function AdmitCardsScreen() {
  const { t } = useTranslation();
  const items = useContentStore((s) => s.admitCards);
  const openLink = useOpenLink();
  const text = useCallback((i: AdmitCardItem) => `${i.examName} ${i.recruitingOrg}`, []);

  return (
    <ContentList
      title={t('results.tabAdmitCards')}
      items={items}
      text={text}
      renderItem={(item, index) => (
        <ContentCard
          index={index}
          icon={images.content.admitCard}
          title={item.examName}
          subtitle={item.recruitingOrg}
          onPress={item.url ? () => void openLink(item.url, item.examName) : undefined}
          onDownload={item.url ? () => void openLink(item.url, item.examName) : undefined}
          downloadLabel={t('results.download')}
          meta={
            <>
              {item.examDate ? (
                <Text variant="caption" weight="700">
                  {t('results.examDateLabel', { date: formatDate(item.examDate) })}
                </Text>
              ) : item.releaseDate ? (
                <Text variant="caption" tone="muted">
                  {t('results.released', { date: formatDate(item.releaseDate) })}
                </Text>
              ) : null}
              {item.citySlipReleased ? <Pill tone="blue" label={t('results.citySlip')} /> : null}
            </>
          }
        />
      )}
    />
  );
}
