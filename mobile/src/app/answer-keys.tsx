import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';

import { images } from '@/assets/images';
import { Pill, Text } from '@/components/ui';
import { ContentCard, SoftAction } from '@/features/content/ContentCard';
import { ContentList } from '@/features/content/ContentList';
import { formatDate } from '@/lib/dates';
import { useOpenLink } from '@/lib/useOpenLink';
import { useContentStore } from '@/store/contentStore';
import type { AnswerKeyItem } from '@/types/domain';

export default function AnswerKeysScreen() {
  const { t } = useTranslation();
  const items = useContentStore((s) => s.answerKeys);
  const openLink = useOpenLink();
  const text = useCallback((i: AnswerKeyItem) => `${i.examName} ${i.recruitingOrg}`, []);

  return (
    <ContentList
      title={t('results.tabAnswerKeys')}
      items={items}
      text={text}
      renderItem={(item, index) => {
        const isFinal = item.status.toUpperCase() === 'FINAL';
        return (
          <ContentCard
            index={index + 3}
            icon={images.content.answerKey}
            title={item.examName}
            subtitle={item.recruitingOrg}
            onPress={item.url ? () => void openLink(item.url, item.examName) : undefined}
            meta={
              <>
                <Pill tone={isFinal ? 'green' : 'amber'} label={isFinal ? t('results.final') : t('results.provisional')} />
                {item.releaseDate ? (
                  <Text variant="caption" weight="600">
                    {t('results.released', { date: formatDate(item.releaseDate) })}
                  </Text>
                ) : null}
                {!isFinal && item.objectionDeadline ? (
                  <Text variant="caption" tone="danger" weight="600">
                    {t('results.objection', { date: formatDate(item.objectionDeadline) })}
                  </Text>
                ) : null}
                {item.objectionFee ? (
                  <Text variant="caption" tone="muted">
                    {t('results.objectionFee', { fee: item.objectionFee })}
                  </Text>
                ) : null}
              </>
            }
            footer={item.url ? <SoftAction tone="blue" label={t('results.viewKey')} onPress={() => void openLink(item.url, item.examName)} /> : null}
          />
        );
      }}
    />
  );
}
