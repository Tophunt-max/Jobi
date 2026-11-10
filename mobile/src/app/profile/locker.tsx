import { useRouter } from 'expo-router';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Button, Card, InlineBanner, ScrollScreen, Text, TextField } from '@/components/ui';
import { useUserStore } from '@/store/userStore';
import type { DocumentLocker } from '@/types/domain';

type Field = { key: keyof DocumentLocker; label: string; numeric?: boolean; max?: number };

export default function LockerScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const stored = useUserStore((s) => s.locker);
  const save = useUserStore((s) => s.setLocker);
  const [d, setD] = useState<DocumentLocker>(stored);

  const groups: { title: string; fields: Field[] }[] = [
    { title: t('locker.personal'), fields: [{ key: 'fatherName', label: t('locker.fatherName') }, { key: 'dob', label: t('locker.dob'), max: 10 }] },
    {
      title: t('locker.tenth'),
      fields: [
        { key: 'tenthBoard', label: t('locker.board') },
        { key: 'tenthRollNo', label: t('locker.rollNo') },
        { key: 'tenthYear', label: t('locker.year'), numeric: true, max: 4 },
        { key: 'tenthPercentage', label: t('locker.percentage') },
      ],
    },
    {
      title: t('locker.twelfth'),
      fields: [
        { key: 'twelfthBoard', label: t('locker.board') },
        { key: 'twelfthStream', label: t('locker.stream') },
        { key: 'twelfthRollNo', label: t('locker.rollNo') },
        { key: 'twelfthYear', label: t('locker.year'), numeric: true, max: 4 },
        { key: 'twelfthPercentage', label: t('locker.percentage') },
      ],
    },
    {
      title: t('locker.graduation'),
      fields: [
        { key: 'gradDegree', label: t('locker.degree') },
        { key: 'gradUniversity', label: t('locker.university') },
        { key: 'gradRollNo', label: t('locker.rollNo') },
        { key: 'gradYear', label: t('locker.year'), numeric: true, max: 4 },
        { key: 'gradCgpaOrPercentage', label: t('locker.percentage') },
      ],
    },
    {
      title: t('locker.certificates'),
      fields: [
        { key: 'categoryCertNo', label: t('locker.categoryCert') },
        { key: 'categoryIssueAuthority', label: t('locker.issuedBy') },
        { key: 'categoryIssueDate', label: t('locker.issueDate'), max: 10 },
        { key: 'domicileCertNo', label: t('locker.domicileCert') },
        { key: 'domicileState', label: t('locker.domicileState') },
      ],
    },
    {
      title: t('locker.identity'),
      fields: [
        { key: 'idType', label: t('locker.idType') },
        { key: 'idNumberMasked', label: t('locker.idNumber'), numeric: true, max: 4 },
      ],
    },
  ];

  return (
    <ScrollScreen>
      <InlineBanner tone="info" icon="lock-closed-outline" message={`${t('profile.lockerHint')} ${t('locker.idHint')}`} />
      {groups.map((g) => (
        <Card key={g.title}>
          <Text variant="subheading">{g.title}</Text>
          {g.fields.map((f) => (
            <TextField
              key={f.key}
              label={f.label}
              value={d[f.key]}
              keyboardType={f.numeric ? 'number-pad' : 'default'}
              maxLength={f.max ?? 80}
              onChangeText={(v) => setD((cur) => ({ ...cur, [f.key]: v }))}
            />
          ))}
        </Card>
      ))}
      <Button
        label={t('common.save')}
        icon="checkmark"
        onPress={() => {
          save(d);
          if (router.canGoBack()) router.back();
        }}
      />
    </ScrollScreen>
  );
}
