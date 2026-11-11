import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';

import { images } from '@/assets/images';
import { Pill, Text } from '@/components/ui';
import { ContentCard, SoftAction } from '@/features/content/ContentCard';
import { ContentList } from '@/features/content/ContentList';
import { formatDate } from '@/lib/dates';
import { useOpenLink } from '@/lib/useOpenLink';
import { useContentStore } from '@/store/contentStore';
import type { ResultItem } from '@/types/domain';

export default function ResultsScreen() {
  const { t } = useTranslation();
  const items = useContentStore((s) => s.results);
  const openLink = useOpenLink();
  const text = useCallback((i: ResultItem) => `${i.examName} ${i.recruitingOrg} ${i.stage}`, []);

  return (
    <ContentList
      title={t('results.tabResults')}
      items={items}
      text={text}
      renderItem={(item, index) => (
        <ContentCard
          index={index}
          icon={images.content.result}
          art="results"
          title={item.examName}
          subtitle={item.recruitingOrg}
          onPress={item.url ? () => void openLink(item.url, item.examName) : undefined}
          meta={
            <>
              {item.publicationDate ? (
                <Text variant="caption" weight="600">
                  {t('results.declared', { date: formatDate(item.publicationDate) })}
                </Text>
              ) : null}
              {item.cutOffSummary ? (
                <Text variant="caption" tone="muted" numberOfLines={2}>
                  {t('results.cutOff')}: {item.cutOffSummary}
                </Text>
              ) : null}
              {item.meritListAvailable ? <Pill tone="purple" label={t('results.meritList')} /> : null}
            </>
          }
          footer={item.url ? <SoftAction label={t('results.viewResult')} onPress={() => void openLink(item.url, item.examName)} /> : null}
        />
      )}
    />
  );
}
