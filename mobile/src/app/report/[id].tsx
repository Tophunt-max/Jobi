import { useLocalSearchParams, useRouter } from 'expo-router';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';

import { Button, Chip, InlineBanner, ScrollScreen, Text, TextField } from '@/components/ui';
import { REPORT_REASONS } from '@/constants/catalog';
import { ApiError } from '@/services/api/client';
import { submitReport } from '@/services/api/endpoints';
import { useContentStore } from '@/store/contentStore';
import { useUserStore } from '@/store/userStore';

export default function ReportScreen() {
  const { id = '' } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const router = useRouter();
  const fromFeed = useContentStore((s) => s.jobs.find((j) => j.id === id));
  const fromSaved = useUserStore((s) => s.bookmarks[id]);
  const job = fromFeed ?? fromSaved;
  const [reason, setReason] = useState<string>(REPORT_REASONS[0]);
  const [details, setDetails] = useState('');
  const [state, setState] = useState<'idle' | 'sending' | 'sent' | 'error' | 'limited'>('idle');

  const send = async () => {
    setState('sending');
    try {
      await submitReport(id, reason, details.trim());
      setState('sent');
      setTimeout(() => router.canGoBack() && router.back(), 1200);
    } catch (e) {
      setState(e instanceof ApiError && e.kind === 'rate_limited' ? 'limited' : 'error');
    }
  };

  return (
    <ScrollScreen>
      <View style={{ gap: 4 }}>
        <Text tone="muted">{t('report.subtitle')}</Text>
        {job ? <Text variant="subheading">{job.title}</Text> : null}
      </View>
      <Text variant="caption" tone="muted" weight="700">
        {t('report.reason')}
      </Text>
      <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
        {REPORT_REASONS.map((r) => (
          <Chip key={r} label={r} selected={reason === r} onPress={() => setReason(r)} />
        ))}
      </View>
      <TextField label={t('report.details')} value={details} onChangeText={setDetails} multiline maxLength={1000} />
      {state === 'sent' ? <InlineBanner tone="success" message={t('report.sent')} /> : null}
      {state === 'error' ? <InlineBanner tone="danger" message={t('report.failed')} /> : null}
      {state === 'limited' ? <InlineBanner tone="warning" message={t('report.tooMany')} /> : null}
      <Button
        label={t('report.submit')}
        icon="send-outline"
        loading={state === 'sending'}
        disabled={state === 'sent' || !id}
        onPress={() => void send()}
      />
    </ScrollScreen>
  );
}
