import { useRouter } from 'expo-router';
import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';

import { Badge, Button, Card, Row, ScrollScreen, Text, type BadgeTone } from '@/components/ui';
import { evaluatePhysical, type PhysicalStatus } from '@/features/profile/physical';
import { useUserStore } from '@/store/userStore';

const TONE: Record<PhysicalStatus, BadgeTone> = { PASS: 'success', BORDERLINE: 'warning', BELOW: 'danger' };

export default function PhysicalStandardsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const profile = useUserStore((s) => s.profile);
  const results = useMemo(() => evaluatePhysical(profile), [profile]);

  return (
    <ScrollScreen>
      <Text tone="muted">{t('physical.intro', { height: profile.heightCm })}</Text>
      <Button compact variant="ghost" icon="create-outline" label={t('profile.physical')} onPress={() => router.push('/profile/edit')} />
      {results.map((r) => (
        <Card key={r.force}>
          <Row style={{ justifyContent: 'space-between' }}>
            <Text variant="subheading" style={{ flex: 1 }}>
              {r.force}
            </Text>
            <Badge label={t(`physical.${r.overall}`)} tone={TONE[r.overall]} />
          </Row>
          <Text variant="caption" tone="muted">
            {r.post}
          </Text>
          <Row style={{ justifyContent: 'space-between' }}>
            <Text variant="caption">{t('physical.required', { value: r.requiredHeightCm })}</Text>
            <Text variant="caption" tone={r.heightDiffCm >= 0 ? 'success' : 'danger'} weight="700">
              {r.heightDiffCm >= 0
                ? t('physical.diffPlus', { value: r.heightDiffCm })
                : t('physical.diffMinus', { value: Math.abs(r.heightDiffCm) })}
            </Text>
          </Row>
          {r.chestRequirement && r.chestStatus ? (
            <Row style={{ justifyContent: 'space-between' }}>
              <Text variant="caption">{t('physical.chest', { value: r.chestRequirement })}</Text>
              <Badge label={t(`physical.${r.chestStatus}`)} tone={TONE[r.chestStatus]} />
            </Row>
          ) : null}
          <Text variant="caption" tone="muted">
            {t('physical.run', { value: r.runningTest })}
          </Text>
        </Card>
      ))}
    </ScrollScreen>
  );
}
