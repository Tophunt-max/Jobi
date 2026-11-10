import { useTranslation } from 'react-i18next';
import { View } from 'react-native';

import { Badge, Button, Card, Row, Text } from '@/components/ui';
import { useOpenLink } from '@/lib/useOpenLink';

import { hasSponsor, useAdConfigStore } from './config';

/** Direct sponsor configured in the admin panel. Rendered on every platform, clearly labelled. */
export function SponsorCard() {
  const { t } = useTranslation();
  const config = useAdConfigStore((s) => s.config);
  const openLink = useOpenLink();
  if (!hasSponsor(config)) return null;

  return (
    <Card accessibilityLabel={`${t('common.sponsored')}: ${config.sponsorHeadline}`}>
      <Row style={{ justifyContent: 'space-between' }}>
        <Badge label={t('common.sponsored')} tone="warning" />
        {config.sponsorRating ? (
          <Text variant="caption" tone="muted">
            ★ {config.sponsorRating}
          </Text>
        ) : null}
      </Row>
      <Row style={{ alignItems: 'flex-start' }}>
        {config.sponsorEmoji ? <Text style={{ fontSize: 28 }}>{config.sponsorEmoji}</Text> : null}
        <View style={{ flex: 1, gap: 2 }}>
          <Text variant="subheading">{config.sponsorHeadline}</Text>
          {config.sponsorName ? (
            <Text variant="caption" tone="muted">
              {config.sponsorName}
            </Text>
          ) : null}
          {config.sponsorBody ? <Text variant="caption">{config.sponsorBody}</Text> : null}
        </View>
      </Row>
      <Button
        compact
        variant="secondary"
        label={config.sponsorCtaText || t('common.openLink')}
        onPress={() => void openLink(config.sponsorTargetUrl, config.sponsorName, { external: true })}
      />
    </Card>
  );
}
