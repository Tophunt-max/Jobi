import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';

import { images } from '@/assets/images';
import { Pill, Text } from '@/components/ui';
import { ContentCard, SoftAction } from '@/features/content/ContentCard';
import { ContentList } from '@/features/content/ContentList';
import { daysUntil, formatDate } from '@/lib/dates';
import { useOpenLink } from '@/lib/useOpenLink';
import { useContentStore } from '@/store/contentStore';
import type { AdmissionItem } from '@/types/domain';

export default function AdmissionsScreen() {
  const { t } = useTranslation();
  const items = useContentStore((s) => s.admissions);
  const openLink = useOpenLink();
  const text = useCallback((i: AdmissionItem) => `${i.name} ${i.authority} ${i.eligibility}`, []);

  return (
    <ContentList
      title={t('results.tabAdmissions')}
      items={items}
      text={text}
      renderItem={(item, index) => {
        const left = item.closingDate ? daysUntil(item.closingDate) : null;
        const open = item.status !== 'CLOSED' && (left === null || left >= 0);
        return (
          <ContentCard
            index={index + 1}
            icon={images.content.admission}
            title={item.name}
            subtitle={item.authority}
            meta={
              <>
                <Pill tone={open ? 'green' : 'gray'} label={open ? t('results.open') : t('results.closed')} />
                {item.eligibility ? (
                  <Text variant="caption" tone="muted" numberOfLines={2}>
                    {item.eligibility}
                  </Text>
                ) : null}
                {item.closingDate ? (
                  <Text variant="caption" weight="700" tone={open && left !== null && left <= 7 ? 'danger' : 'default'}>
                    {t('results.applyBy', { date: formatDate(item.closingDate) })}
                  </Text>
                ) : null}
              </>
            }
            footer={
              <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap' }}>
                {item.applyUrl && open ? <SoftAction label={t('results.apply')} onPress={() => void openLink(item.applyUrl, item.name)} /> : null}
                {item.bulletinUrl ? <SoftAction tone="blue" label={t('results.bulletin')} onPress={() => void openLink(item.bulletinUrl, item.name)} /> : null}
              </View>
            }
          />
        );
      }}
    />
  );
}
