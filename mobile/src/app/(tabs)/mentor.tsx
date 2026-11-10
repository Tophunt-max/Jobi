import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useMemo, useRef, useState, type ComponentRef } from 'react';
import { useTranslation } from 'react-i18next';
import { ActivityIndicator, Image, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, TextInput, View } from 'react-native';

import { images } from '@/assets/images';
import { Badge, InlineBanner, OptionSheet, Page, ScreenHeader, Surface, Text, type IconName } from '@/components/ui';
import { QUALIFICATIONS } from '@/constants/catalog';
import { useJobFilters } from '@/features/jobs/filtersStore';
import { useOpenJobs } from '@/features/jobs/hooks';
import { useMentorStore, type MentorMessage } from '@/features/mentor/mentorStore';
import { formatDate } from '@/lib/dates';
import { useVoiceInput } from '@/lib/useVoiceInput';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';
import type { EducationLevel, Job } from '@/types/domain';

/** Maps the profile's education level to the job list's qualification filter. */
const QUAL_FILTER: Record<EducationLevel, (typeof QUALIFICATIONS)[number]> = {
  TENTH_PASS: '10th',
  TWELFTH_PASS: '12th',
  ITI: 'ITI',
  DIPLOMA: 'Diploma',
  GRADUATE: 'Graduate',
  BTECH: 'B.Tech',
  LLB: 'Graduate',
  MBBS_MEDICAL: 'Graduate',
  POST_GRADUATE: 'Post Graduate',
};

type PickMode = 'explain' | 'lastDate' | null;

function jobFacts(job: Job): string {
  return [
    `Title: ${job.title}`,
    `Organisation: ${job.organization}`,
    job.totalPosts ? `Posts: ${job.totalPosts}` : '',
    job.qualification ? `Qualification: ${job.qualification}` : '',
    job.maxAge ? `Age: ${job.minAge || '-'} to ${job.maxAge}` : '',
    job.lastDate ? `Last date: ${formatDate(job.lastDate)}` : '',
    job.examDate ? `Exam date: ${job.examDate}` : '',
    job.feeGeneral ? `Fee (General/OBC): ${job.feeGeneral}` : '',
    job.feeReserved ? `Fee (SC/ST): ${job.feeReserved}` : '',
    job.selectionProcess ? `Selection: ${job.selectionProcess}` : '',
    job.salaryScale ? `Pay: ${job.salaryScale}` : '',
  ]
    .filter(Boolean)
    .join('\n');
}

export default function MentorScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors, shadows } = useTheme();
  const { messages, busy, askQuestion, clear } = useMentorStore();
  const profile = useUserStore((s) => s.profile);
  const resetFilters = useJobFilters((s) => s.reset);
  const jobs = useOpenJobs();
  const [question, setQuestion] = useState('');
  const [pick, setPick] = useState<PickMode>(null);
  const scrollRef = useRef<ComponentRef<typeof ScrollView>>(null);
  const inputRef = useRef<ComponentRef<typeof TextInput>>(null);
  const state = profile.homeState && profile.homeState !== 'All India' ? profile.homeState : 'Odisha';

  const send = (text: string, display?: string) => {
    const q = text.trim();
    if (!q || busy) return;
    setQuestion('');
    void askQuestion(q, display);
  };
  const voice = useVoiceInput((text) => send(text));

  const jobOptions = useMemo(() => jobs.slice(0, 150).map((j) => ({ value: j.id, label: j.title, subtitle: j.organization })), [jobs]);

  const onPicked = (id: string) => {
    const job = jobs.find((j) => j.id === id);
    if (!job) return;
    if (pick === 'explain') send(`${t('assistant.explainPrompt')}\n\n${jobFacts(job)}`, `${t('assistant.explainNotice')}: ${job.title}`);
    if (pick === 'lastDate') send(`${t('assistant.lastDatePrompt')}\n\n${jobFacts(job)}`, `${t('assistant.lastDate')} ${job.title}`);
  };

  const suggestions: { icon: IconName; label: string; onPress: () => void }[] = [
    {
      icon: 'briefcase-outline',
      label: t('assistant.findJobs'),
      onPress: () => {
        resetFilters({ qualification: QUAL_FILTER[profile.qualification] });
        router.navigate('/jobs');
      },
    },
    { icon: 'checkmark-circle-outline', label: t('assistant.checkEligibility'), onPress: () => router.push('/eligibility') },
    { icon: 'document-text-outline', label: t('assistant.explainNotice'), onPress: () => setPick('explain') },
    { icon: 'calendar-outline', label: t('assistant.lastDate'), onPress: () => setPick('lastDate') },
    {
      icon: 'location-outline',
      label: t('assistant.latestIn', { state }),
      onPress: () => {
        resetFilters({ state });
        router.navigate('/jobs');
      },
    },
  ];

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader
        title={t('assistant.title')}
        back={false}
        titleAccessory={<Ionicons name="sparkles" size={18} color={colors.accent} />}
        right={
          messages.length > 0 ? (
            <Pressable accessibilityRole="button" accessibilityLabel={t('mentor.clear')} onPress={clear} hitSlop={8} style={{ padding: 6 }}>
              <Ionicons name="trash-outline" size={21} color={colors.text} />
            </Pressable>
          ) : null
        }
      />
      <KeyboardAvoidingView style={{ flex: 1 }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView
          ref={scrollRef}
          onContentSizeChange={() => messages.length > 0 && scrollRef.current?.scrollToEnd({ animated: true })}
          contentContainerStyle={{ paddingVertical: 16, paddingBottom: 24 }}
          keyboardShouldPersistTaps="handled"
        >
          <Page style={{ gap: 14 }}>
            {messages.length === 0 ? (
              <>
                <View style={styles.hello}>
                  <Image source={images.ai.robot} style={styles.robot} resizeMode="contain" accessibilityIgnoresInvertColors />
                  <Text style={styles.helloTitle} align="center">
                    {t('assistant.hello')}
                  </Text>
                  <Text tone="muted" align="center">
                    {t('assistant.subtitle')}
                  </Text>
                </View>
                <View style={{ gap: 10 }}>
                  {suggestions.map((s) => (
                    <Surface key={s.label} onPress={s.onPress} accessibilityLabel={s.label} style={styles.suggestion}>
                      <View style={[styles.suggestionIcon, { backgroundColor: colors.primarySoft }]}>
                        <Ionicons name={s.icon} size={18} color={colors.primary} />
                      </View>
                      <Text weight="700" style={{ flex: 1 }}>
                        {s.label}
                      </Text>
                      <Ionicons name="chevron-forward" size={16} color={colors.textMuted} />
                    </Surface>
                  ))}
                </View>
                <Text variant="caption" tone="muted" align="center">
                  {t('mentor.privacy')}
                </Text>
              </>
            ) : (
              messages.map((m) => <Bubble key={m.id} message={m} />)
            )}
            {busy ? (
              <View style={styles.thinking}>
                <ActivityIndicator color={colors.primary} />
                <Text tone="muted">{t('mentor.thinking')}</Text>
              </View>
            ) : null}
          </Page>
        </ScrollView>

        <View style={[styles.inputBar, { backgroundColor: colors.surface, boxShadow: shadows.bar }]}>
          <Page style={styles.inputRow}>
            <View style={[styles.inputBox, { backgroundColor: colors.surfaceMuted, borderColor: colors.border }]}>
              <TextInput
                ref={inputRef}
                accessibilityLabel={t('assistant.placeholder')}
                value={question}
                onChangeText={setQuestion}
                placeholder={t('assistant.placeholder')}
                placeholderTextColor={colors.textMuted}
                multiline
                maxLength={1000}
                style={[styles.input, { color: colors.text }]}
              />
              <Pressable
                accessibilityRole="button"
                accessibilityLabel={t('common.voiceSearch')}
                hitSlop={8}
                onPress={() => (voice.supported ? voice.start() : inputRef.current?.focus())}
              >
                <Ionicons name={voice.listening ? 'mic' : 'mic-outline'} size={20} color={voice.listening ? colors.danger : colors.textMuted} />
              </Pressable>
            </View>
            <Pressable
              accessibilityRole="button"
              accessibilityLabel={t('mentor.send')}
              disabled={!question.trim() || busy}
              onPress={() => send(question)}
              style={[styles.send, { backgroundColor: colors.primary, opacity: !question.trim() || busy ? 0.5 : 1 }]}
            >
              <Ionicons name="send" size={18} color="#fff" />
            </Pressable>
          </Page>
        </View>
      </KeyboardAvoidingView>

      <OptionSheet
        visible={pick !== null}
        title={t('mentor.pickJob')}
        options={jobOptions}
        onSelect={onPicked}
        onClose={() => setPick(null)}
      />
    </View>
  );
}

function Bubble({ message }: { message: MentorMessage }) {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const mine = message.role === 'user';
  return (
    <View style={{ alignItems: mine ? 'flex-end' : 'flex-start', gap: 4 }}>
      {!mine ? (
        <View style={styles.botHead}>
          <Image source={images.ai.robot} style={{ width: 26, height: 26 }} accessibilityIgnoresInvertColors />
          <Badge
            label={message.source === 'offline' ? t('mentor.offlineAnswer') : t('mentor.aiAnswer')}
            tone={message.source === 'offline' ? 'warning' : 'primary'}
            icon={message.source === 'offline' ? 'cloud-offline-outline' : 'sparkles'}
          />
        </View>
      ) : null}
      {message.notice ? <InlineBanner tone="warning" message={message.notice} /> : null}
      <View
        style={[
          styles.bubble,
          mine
            ? { backgroundColor: colors.primary, borderBottomRightRadius: 4 }
            : { backgroundColor: colors.surface, borderColor: colors.border, borderWidth: StyleSheet.hairlineWidth, borderBottomLeftRadius: 4 },
        ]}
      >
        <Text selectable style={{ color: mine ? '#fff' : colors.text }}>
          {message.text.replace(/\*\*/g, '')}
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  hello: { alignItems: 'center', gap: 4, marginBottom: 6 },
  robot: { width: 150, height: 146 },
  helloTitle: { fontSize: 22, fontWeight: '900' },
  suggestion: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 12 },
  suggestionIcon: { width: 34, height: 34, borderRadius: 10, alignItems: 'center', justifyContent: 'center' },
  thinking: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  botHead: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  bubble: { maxWidth: '88%', padding: 12, borderRadius: 16 },
  inputBar: { paddingVertical: 10 },
  inputRow: { flexDirection: 'row', alignItems: 'flex-end', gap: 10 },
  inputBox: { flex: 1, flexDirection: 'row', alignItems: 'center', gap: 8, borderRadius: 14, borderWidth: 1, paddingHorizontal: 12 },
  input: { flex: 1, maxHeight: 120, minHeight: 44, paddingVertical: 11, fontSize: 14 },
  send: { width: 46, height: 46, borderRadius: 23, alignItems: 'center', justifyContent: 'center' },
});
