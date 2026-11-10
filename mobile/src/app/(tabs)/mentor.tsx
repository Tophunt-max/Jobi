import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useMemo, useRef, useState, type ComponentRef } from 'react';
import { useTranslation } from 'react-i18next';
import { ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, TextInput, View } from 'react-native';

import { Badge, Button, Card, Chip, Container, InlineBanner, Row, SelectField, Text, TextField } from '@/components/ui';
import { casteMeta } from '@/constants/catalog';
import { useOpenJobs } from '@/features/jobs/hooks';
import { useMentorStore, type MentorMessage } from '@/features/mentor/mentorStore';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';

export default function MentorScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors, radius, spacing } = useTheme();
  const { messages, busy, askEligibility, askStrategy, askQuestion, clear } = useMentorStore();
  const profile = useUserStore((s) => s.profile);
  const jobs = useOpenJobs();
  const [question, setQuestion] = useState('');
  const [exam, setExam] = useState('');
  const [jobId, setJobId] = useState('');
  const scrollRef = useRef<ComponentRef<typeof ScrollView>>(null);

  const jobOptions = useMemo(
    () => jobs.slice(0, 100).map((j) => ({ value: j.id, label: j.title })),
    [jobs],
  );
  const selectedJob = jobs.find((j) => j.id === jobId) ?? null;

  const send = (text: string) => {
    const q = text.trim();
    if (!q || busy) return;
    setQuestion('');
    void askQuestion(q);
  };

  return (
    <KeyboardAvoidingView
      style={{ flex: 1, backgroundColor: colors.background }}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 90 : 0}
    >
      <ScrollView
        ref={scrollRef}
        onContentSizeChange={() => scrollRef.current?.scrollToEnd({ animated: true })}
        contentContainerStyle={{ padding: spacing.lg, paddingBottom: spacing.xl }}
        keyboardShouldPersistTaps="handled"
      >
        <Container style={{ gap: spacing.lg }}>
          <Card>
            <Row>
              <Ionicons name="sparkles" size={20} color={colors.primary} />
              <Text variant="heading" style={{ flex: 1 }}>
                {t('mentor.subtitle')}
              </Text>
            </Row>
            <Text variant="caption" tone="muted">
              {t('mentor.profileHint', { age: profile.age, category: casteMeta(profile.category).label })}
            </Text>
            <Row gap={8}>
              <Button compact variant="ghost" icon="person-outline" label={t('mentor.editProfile')} onPress={() => router.push('/profile/edit')} />
              {messages.length > 0 ? (
                <Button compact variant="ghost" icon="trash-outline" label={t('mentor.clear')} onPress={clear} />
              ) : null}
            </Row>
            <InlineBanner tone="info" icon="lock-closed-outline" message={t('mentor.privacy')} />
          </Card>

          {messages.length === 0 ? (
            <>
              <Card>
                <Text variant="subheading">{t('mentor.pickJob')}</Text>
                <SelectField label={t('mentor.pickJobLabel')} value={jobId} options={jobOptions} onChange={setJobId} searchable />
                <Button
                  label={t('mentor.check')}
                  icon="checkmark-done-outline"
                  disabled={!selectedJob || busy}
                  onPress={() => selectedJob && void askEligibility(selectedJob)}
                />
              </Card>
              <Card>
                <Text variant="subheading">{t('mentor.strategy')}</Text>
                <TextField value={exam} onChangeText={setExam} placeholder={t('mentor.examName')} maxLength={80} />
                <Button
                  variant="secondary"
                  label={t('mentor.plan')}
                  icon="map-outline"
                  disabled={!exam.trim() || busy}
                  onPress={() => void askStrategy(exam.trim())}
                />
              </Card>
              <View style={{ gap: spacing.sm }}>
                <Text variant="caption" tone="muted" weight="700">
                  {t('mentor.suggestions')}
                </Text>
                <View style={styles.wrap}>
                  {(['mentor.suggest1', 'mentor.suggest2', 'mentor.suggest3'] as const).map((k) => (
                    <Chip key={k} label={t(k)} onPress={() => send(t(k))} />
                  ))}
                </View>
              </View>
            </>
          ) : (
            messages.map((m) => <Bubble key={m.id} message={m} />)
          )}

          {busy ? (
            <Row>
              <ActivityIndicator color={colors.primary} />
              <Text tone="muted">{t('mentor.thinking')}</Text>
            </Row>
          ) : null}
        </Container>
      </ScrollView>

      <View style={[styles.inputBar, { backgroundColor: colors.surface, borderTopColor: colors.border }]}>
        <Container style={styles.inputRow}>
          <TextInput
            accessibilityLabel={t('mentor.placeholder')}
            value={question}
            onChangeText={setQuestion}
            placeholder={t('mentor.placeholder')}
            placeholderTextColor={colors.textMuted}
            multiline
            maxLength={1000}
            style={[
              styles.input,
              { color: colors.text, backgroundColor: colors.surfaceMuted, borderRadius: radius.lg },
            ]}
          />
          <Pressable
            accessibilityRole="button"
            accessibilityLabel={t('mentor.send')}
            disabled={!question.trim() || busy}
            onPress={() => send(question)}
            style={[styles.send, { backgroundColor: colors.primary, opacity: !question.trim() || busy ? 0.5 : 1 }]}
          >
            <Ionicons name="send" size={18} color={colors.onPrimary} />
          </Pressable>
        </Container>
      </View>
    </KeyboardAvoidingView>
  );
}

function Bubble({ message }: { message: MentorMessage }) {
  const { t } = useTranslation();
  const { colors, radius } = useTheme();
  const mine = message.role === 'user';
  return (
    <View style={{ alignItems: mine ? 'flex-end' : 'flex-start', gap: 4 }}>
      {!mine ? (
        <Badge
          label={message.source === 'offline' ? t('mentor.offlineAnswer') : t('mentor.aiAnswer')}
          tone={message.source === 'offline' ? 'warning' : 'primary'}
          icon={message.source === 'offline' ? 'cloud-offline-outline' : 'sparkles'}
        />
      ) : null}
      {message.notice ? <InlineBanner tone="warning" message={message.notice} /> : null}
      <View
        style={{
          maxWidth: '92%',
          padding: 12,
          borderRadius: radius.lg,
          backgroundColor: mine ? colors.primary : colors.surface,
          borderWidth: mine ? 0 : StyleSheet.hairlineWidth,
          borderColor: colors.border,
        }}
      >
        <Text selectable style={{ color: mine ? colors.onPrimary : colors.text }}>
          {message.text.replace(/\*\*/g, '')}
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  inputBar: { borderTopWidth: StyleSheet.hairlineWidth, paddingHorizontal: 12, paddingVertical: 8 },
  inputRow: { flexDirection: 'row', alignItems: 'flex-end', gap: 8 },
  input: { flex: 1, maxHeight: 120, minHeight: 42, paddingHorizontal: 14, paddingVertical: 10, fontSize: 14 },
  send: { width: 42, height: 42, borderRadius: 21, alignItems: 'center', justifyContent: 'center' },
});
