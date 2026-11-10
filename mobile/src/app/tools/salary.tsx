import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';

import { Card, Chip, ChipRow, Divider, Row, ScrollScreen, Segmented, Text } from '@/components/ui';
import { calculateSalary, DA_LABEL, PAY_LEVELS, type CityTier } from '@/features/tools/salary';
import { formatInr } from '@/lib/format';

function Line({ label, value, bold, negative }: { label: string; value: number; bold?: boolean; negative?: boolean }) {
  return (
    <Row style={{ justifyContent: 'space-between' }}>
      <Text weight={bold ? '800' : '400'}>{label}</Text>
      <Text weight={bold ? '800' : '600'} tone={negative ? 'danger' : bold ? 'primary' : 'default'}>
        {negative ? '−' : ''}
        {formatInr(value)}
      </Text>
    </Row>
  );
}

export default function SalaryCalculatorScreen() {
  const { t } = useTranslation();
  const [level, setLevel] = useState(7);
  const [tier, setTier] = useState<CityTier>('Y');
  const info = PAY_LEVELS.find((l) => l.level === level) ?? PAY_LEVELS[6]!;
  const s = useMemo(() => calculateSalary(info, tier), [info, tier]);

  return (
    <ScrollScreen>
      <Card>
        <Text variant="subheading">{t('salary.level')}</Text>
        <ChipRow>
          {PAY_LEVELS.map((l) => (
            <Chip key={l.level} label={`L${l.level} · ${formatInr(l.basic)}`} selected={l.level === level} onPress={() => setLevel(l.level)} />
          ))}
        </ChipRow>
        <Text variant="caption" tone="muted">
          {t('salary.typical', { posts: info.posts })}
        </Text>
        <Text variant="subheading">{t('salary.city')}</Text>
        <Segmented<CityTier>
          value={tier}
          onChange={setTier}
          options={[
            { value: 'X', label: t('salary.cityX') },
            { value: 'Y', label: t('salary.cityY') },
            { value: 'Z', label: t('salary.cityZ') },
          ]}
        />
      </Card>

      <Card>
        <View style={{ gap: 8 }}>
          <Line label={t('salary.basic')} value={s.basic} />
          <Line label={t('salary.da', { rate: DA_LABEL })} value={s.da} />
          <Line label={t('salary.hra')} value={s.hra} />
          <Line label={t('salary.ta')} value={s.ta} />
          <Divider />
          <Line label={t('salary.gross')} value={s.gross} bold />
          <Line label={t('salary.nps')} value={s.nps} negative />
          <Line label={t('salary.ptax')} value={s.professionalTax} negative />
          <Line label={t('salary.cghs')} value={s.cghs} negative />
          <Divider />
          <Line label={t('salary.net')} value={s.net} bold />
        </View>
      </Card>
      <Text variant="caption" tone="muted">
        {t('salary.note')}
      </Text>
    </ScrollScreen>
  );
}
