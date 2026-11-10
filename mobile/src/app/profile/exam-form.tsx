import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Alert, Platform } from 'react-native';

import { Button, Card, InlineBanner, ScrollScreen, SelectField, TextField } from '@/components/ui';
import { EXAM_STAGES } from '@/constants/catalog';
import { makeId } from '@/lib/format';
import { useUserStore } from '@/store/userStore';
import type { AppliedExam, ExamStage } from '@/types/domain';

const EMPTY: Omit<AppliedExam, 'id'> = {
  jobTitle: '',
  organization: '',
  advtNumber: '',
  applicationNo: '',
  rollNo: '',
  examDate: '',
  examCenter: '',
  stage: 'SUBMITTED',
  appliedDate: '',
  notes: '',
};

function confirm(message: string, labels: { cancel: string; confirm: string }, onYes: () => void) {
  if (Platform.OS === 'web') {
    if (window.confirm(message)) onYes();
    return;
  }
  Alert.alert(message, undefined, [
    { text: labels.cancel, style: 'cancel' },
    { text: labels.confirm, style: 'destructive', onPress: onYes },
  ]);
}

export default function ExamFormScreen() {
  const { id } = useLocalSearchParams<{ id?: string }>();
  const { t } = useTranslation();
  const router = useRouter();
  const existing = useUserStore((s) => s.appliedExams.find((e) => e.id === id));
  const upsert = useUserStore((s) => s.upsertExam);
  const remove = useUserStore((s) => s.deleteExam);
  const [form, setForm] = useState<AppliedExam>(existing ?? { id: makeId('exam'), ...EMPTY });
  const [error, setError] = useState(false);
  const set = (patch: Partial<AppliedExam>) => setForm((f) => ({ ...f, ...patch }));
  const close = () => router.canGoBack() && router.back();

  return (
    <ScrollScreen>
      <Stack.Screen options={{ title: existing ? t('exams.edit') : t('exams.add') }} />
      <Card>
        <TextField label={t('exams.jobTitle')} value={form.jobTitle} onChangeText={(jobTitle) => set({ jobTitle })} maxLength={120} />
        <TextField label={t('exams.organization')} value={form.organization} onChangeText={(organization) => set({ organization })} maxLength={80} />
        <TextField label={t('exams.advtNo')} value={form.advtNumber} onChangeText={(advtNumber) => set({ advtNumber })} maxLength={60} />
        <TextField label={t('exams.applicationNo')} value={form.applicationNo} onChangeText={(applicationNo) => set({ applicationNo })} maxLength={60} />
        <TextField label={t('exams.rollNo')} value={form.rollNo} onChangeText={(rollNo) => set({ rollNo })} maxLength={60} />
        <TextField label={t('exams.appliedDate')} value={form.appliedDate} onChangeText={(appliedDate) => set({ appliedDate })} placeholder="DD-MM-YYYY" maxLength={10} />
        <TextField label={t('exams.examDate')} value={form.examDate} onChangeText={(examDate) => set({ examDate })} placeholder="DD-MM-YYYY" maxLength={10} />
        <TextField label={t('exams.examCenter')} value={form.examCenter} onChangeText={(examCenter) => set({ examCenter })} maxLength={120} />
        <SelectField<ExamStage>
          label={t('exams.stage')}
          value={form.stage}
          options={EXAM_STAGES.map((s) => ({ value: s.key, label: s.label }))}
          onChange={(stage) => set({ stage })}
        />
        <TextField label={t('exams.notes')} value={form.notes} onChangeText={(notes) => set({ notes })} multiline maxLength={500} />
      </Card>
      {error ? <InlineBanner tone="danger" message={t('exams.required')} /> : null}
      <Button
        label={t('common.save')}
        icon="checkmark"
        onPress={() => {
          if (!form.jobTitle.trim()) {
            setError(true);
            return;
          }
          upsert({ ...form, jobTitle: form.jobTitle.trim() });
          close();
        }}
      />
      {existing ? (
        <Button
          variant="danger"
          icon="trash-outline"
          label={t('common.delete')}
          onPress={() =>
            confirm(t('exams.deleteConfirm'), { cancel: t('common.cancel'), confirm: t('common.delete') }, () => {
              remove(existing.id);
              close();
            })
          }
        />
      ) : null}
    </ScrollScreen>
  );
}
