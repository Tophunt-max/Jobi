import { FlashList } from '@shopify/flash-list';
import { useState, type ReactElement } from 'react';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';
import { useShallow } from 'zustand/react/shallow';

import { SyncStatus } from '@/components/SyncStatus';
import { Badge, Button, Card, Chip, ChipRow, Container, EmptyState, Row, Text } from '@/components/ui';
import { daysUntil, formatDate } from '@/lib/dates';
import { useOpenLink } from '@/lib/useOpenLink';
import { useContentStore } from '@/store/contentStore';
import { useTheme } from '@/theme';
import type { AdmissionItem, AdmitCardItem, AnswerKeyItem, ResultItem, SyllabusItem } from '@/types/domain';

type TabKey = 'results' | 'admitCards' | 'syllabi' | 'admissions' | 'answerKeys';

const TABS: { key: TabKey; label: string }[] = [
  { key: 'results', label: 'results.tabResults' },
  { key: 'admitCards', label: 'results.tabAdmitCards' },
  { key: 'syllabi', label: 'results.tabSyllabus' },
  { key: 'admissions', label: 'results.tabAdmissions' },
  { key: 'answerKeys', label: 'results.tabAnswerKeys' },
];

export default function ResultsScreen() {
  const { t } = useTranslation();
  const { colors, spacing } = useTheme();
  const [tab, setTab] = useState<TabKey>('results');
  const content = useContentStore(
    useShallow((s) => ({
      results: s.results,
      admitCards: s.admitCards,
      syllabi: s.syllabi,
      admissions: s.admissions,
      answerKeys: s.answerKeys,
      status: s.status,
      sync: s.sync,
    })),
  );

  const counts: Record<TabKey, number> = {
    results: content.results.length,
    admitCards: content.admitCards.length,
    syllabi: content.syllabi.length,
    admissions: content.admissions.length,
    answerKeys: content.answerKeys.length,
  };

  const header = (
    <Container style={{ padding: spacing.lg, gap: spacing.md }}>
      <SyncStatus />
      <ChipRow>
        {TABS.map((x) => (
          <Chip key={x.key} label={t(x.label)} count={counts[x.key]} selected={tab === x.key} onPress={() => setTab(x.key)} />
        ))}
      </ChipRow>
    </Container>
  );

  const common = {
    ListHeaderComponent: header,
    ListEmptyComponent: <EmptyState icon="document-text-outline" title={t('results.empty')} body={t('results.emptyHint')} />,
    refreshing: content.status === 'syncing',
    onRefresh: () => void content.sync({ force: true }),
    contentContainerStyle: { paddingBottom: spacing.xxl },
  };

  const wrap = (node: ReactElement) => (
    <Container style={{ paddingHorizontal: spacing.lg, paddingBottom: spacing.md }}>{node}</Container>
  );

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      {tab === 'results' ? (
        <FlashList key="r" data={content.results} keyExtractor={(i) => i.id} renderItem={({ item }) => wrap(<ResultCard item={item} />)} {...common} />
      ) : tab === 'admitCards' ? (
        <FlashList key="a" data={content.admitCards} keyExtractor={(i) => i.id} renderItem={({ item }) => wrap(<AdmitCard item={item} />)} {...common} />
      ) : tab === 'syllabi' ? (
        <FlashList key="s" data={content.syllabi} keyExtractor={(i) => i.id} renderItem={({ item }) => wrap(<SyllabusCard item={item} />)} {...common} />
      ) : tab === 'admissions' ? (
        <FlashList key="ad" data={content.admissions} keyExtractor={(i) => i.id} renderItem={({ item }) => wrap(<AdmissionCard item={item} />)} {...common} />
      ) : (
        <FlashList key="k" data={content.answerKeys} keyExtractor={(i) => i.id} renderItem={({ item }) => wrap(<AnswerKeyCard item={item} />)} {...common} />
      )}
    </View>
  );
}

function Header({ title, org, domain }: { title: string; org: string; domain: string }) {
  return (
    <View style={{ gap: 2 }}>
      <Text variant="subheading">{title}</Text>
      <Text variant="caption" tone="muted">
        {org}
        {domain ? ` · ${domain}` : ''}
      </Text>
    </View>
  );
}

function LinkButton({ url, label, title }: { url: string; label: string; title: string }) {
  const openLink = useOpenLink();
  if (!url) return null;
  return <Button compact variant="secondary" icon="open-outline" label={label} onPress={() => void openLink(url, title)} />;
}

function ResultCard({ item }: { item: ResultItem }) {
  const { t } = useTranslation();
  return (
    <Card>
      <Row gap={6}>
        <Badge label={item.stage} tone="success" />
        {item.meritListAvailable ? <Badge label={t('results.meritList')} tone="primary" /> : null}
      </Row>
      <Header title={item.examName} org={item.recruitingOrg} domain={item.verificationDomain} />
      {item.publicationDate ? (
        <Text variant="caption" tone="muted">
          {t('results.released', { date: formatDate(item.publicationDate) })}
        </Text>
      ) : null}
      {item.cutOffSummary ? (
        <Text variant="caption">
          {t('results.cutOff')}: {item.cutOffSummary}
        </Text>
      ) : null}
      <LinkButton url={item.url} label={t('results.viewResult')} title={item.examName} />
    </Card>
  );
}

function AdmitCard({ item }: { item: AdmitCardItem }) {
  const { t } = useTranslation();
  return (
    <Card>
      <Row gap={6}>
        {item.citySlipReleased ? <Badge label={t('results.citySlip')} tone="info" /> : null}
        {item.examDate ? <Badge label={t('results.examOn', { date: formatDate(item.examDate) })} tone="accent" /> : null}
      </Row>
      <Header title={item.examName} org={item.recruitingOrg} domain={item.verificationDomain} />
      {item.releaseDate ? (
        <Text variant="caption" tone="muted">
          {t('results.released', { date: formatDate(item.releaseDate) })}
        </Text>
      ) : null}
      {item.instructions ? (
        <Text variant="caption">
          {t('results.instructions')}: {item.instructions}
        </Text>
      ) : null}
      <LinkButton url={item.url} label={t('results.download')} title={item.examName} />
    </Card>
  );
}

function SyllabusCard({ item }: { item: SyllabusItem }) {
  const { t } = useTranslation();
  return (
    <Card>
      <Header title={item.examName} org={item.recruitingOrg} domain={item.verificationDomain} />
      {item.examPattern ? (
        <Text variant="caption">
          {t('results.pattern')}: {item.examPattern}
        </Text>
      ) : null}
      <Row gap={6} style={{ flexWrap: 'wrap' }}>
        {item.durationMinutes > 0 ? <Badge label={t('results.duration', { count: item.durationMinutes })} /> : null}
        {item.negativeMarkingRule ? <Badge label={`${t('results.negative')}: ${item.negativeMarkingRule}`} tone="danger" /> : null}
      </Row>
      {item.subjects.length > 0 ? (
        <View style={{ gap: 2 }}>
          <Text variant="caption" tone="muted">
            {t('results.subjects')}
          </Text>
          {item.subjects.map((s) => (
            <Text key={s.name} variant="caption">
              • {s.name}
              {s.marks ? ` (${s.marks})` : ''}
            </Text>
          ))}
        </View>
      ) : null}
      <LinkButton url={item.url} label={t('results.openPdf')} title={item.examName} />
    </Card>
  );
}

function AdmissionCard({ item }: { item: AdmissionItem }) {
  const { t } = useTranslation();
  const left = item.closingDate ? daysUntil(item.closingDate) : null;
  const open = item.status !== 'CLOSED' && (left === null || left >= 0);
  const openLink = useOpenLink();
  return (
    <Card>
      <Row gap={6}>
        <Badge label={open ? t('results.open') : t('results.closed')} tone={open ? 'success' : 'neutral'} />
      </Row>
      <Header title={item.name} org={item.authority} domain={item.verificationDomain} />
      {item.eligibility ? <Text variant="caption">{item.eligibility}</Text> : null}
      {item.closingDate ? (
        <Text variant="caption" tone={open && left !== null && left <= 7 ? 'danger' : 'muted'}>
          {t('results.applyBy', { date: formatDate(item.closingDate) })}
        </Text>
      ) : null}
      <Row gap={8} style={{ flexWrap: 'wrap' }}>
        {item.applyUrl && open ? (
          <Button compact icon="open-outline" label={t('results.apply')} onPress={() => void openLink(item.applyUrl, item.name)} />
        ) : null}
        <LinkButton url={item.bulletinUrl} label={t('results.bulletin')} title={item.name} />
      </Row>
    </Card>
  );
}

function AnswerKeyCard({ item }: { item: AnswerKeyItem }) {
  const { t } = useTranslation();
  const isFinal = item.status.toUpperCase() === 'FINAL';
  return (
    <Card>
      <Row gap={6}>
        <Badge label={isFinal ? t('results.final') : t('results.provisional')} tone={isFinal ? 'success' : 'warning'} />
      </Row>
      <Header title={item.examName} org={item.recruitingOrg} domain={item.verificationDomain} />
      {item.releaseDate ? (
        <Text variant="caption" tone="muted">
          {t('results.released', { date: formatDate(item.releaseDate) })}
        </Text>
      ) : null}
      {!isFinal && item.objectionDeadline ? (
        <Text variant="caption">{t('results.objection', { date: formatDate(item.objectionDeadline) })}</Text>
      ) : null}
      {item.objectionFee ? <Text variant="caption">{t('results.objectionFee', { fee: item.objectionFee })}</Text> : null}
      <LinkButton url={item.url} label={t('common.openLink')} title={item.examName} />
    </Card>
  );
}
