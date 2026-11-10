import { useRouter } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';

import { Badge, Button, Card, EmptyState, ScrollScreen, Text } from '@/components/ui';
import { stageMeta } from '@/constants/catalog';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';

export default function ExamsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors } = useTheme();
  const exams = useUserStore((s) => s.appliedExams);

  return (
    <ScrollScreen>
      <Button icon="add" label={t('exams.add')} onPress={() => router.push('/profile/exam-form')} />
      {exams.length === 0 ? <EmptyState icon="clipboard-outline" title={t('exams.title')} body={t('exams.empty')} /> : null}
      {exams.map((e) => {
        const stage = stageMeta(e.stage);
        return (
          <Card key={e.id} onPress={() => router.push({ pathname: '/profile/exam-form', params: { id: e.id } })} accessibilityLabel={e.jobTitle}>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
              <View style={{ width: 8, height: 8, borderRadius: 4, backgroundColor: stage.color }} />
              <Badge label={stage.label} />
            </View>
            <Text variant="subheading">{e.jobTitle}</Text>
            {e.organization ? (
              <Text variant="caption" tone="muted">
                {e.organization}
              </Text>
            ) : null}
            <Text variant="caption" style={{ color: colors.textMuted }}>
              {[e.applicationNo && `${t('exams.applicationNo')}: ${e.applicationNo}`, e.rollNo && `${t('exams.rollNo')}: ${e.rollNo}`, e.examDate && `${t('exams.examDate')}: ${e.examDate}`]
                .filter(Boolean)
                .join(' · ')}
            </Text>
          </Card>
        );
      })}
    </ScrollScreen>
  );
}
