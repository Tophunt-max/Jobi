'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { Gauge, LayoutTemplate, Megaphone, MonitorSmartphone, PlayCircle, Rows3, Save, Sparkles, Undo2 } from 'lucide-react';
import { adminApi } from '../../lib/api';
import { AdConfig } from '../../lib/types';
import { formatDateTime } from '../../lib/cn';
import { Alert, Badge, Button, Card, CardContent, CardHeader, Field, Input, PageHeader, Skeleton, Switch, Textarea } from '../../components/ui';
import { useToast } from '../../components/Toast';

const DEFAULTS: AdConfig = {
  masterEnabled: true, bannerEnabled: true, interstitialEnabled: true, nativeEnabled: true, rewardedEnabled: true,
  bannerId: '', interstitialId: '', nativeId: '', rewardedId: '',
  interstitialClickInterval: 3, interstitialCooldownSeconds: 60,
  customSponsorActive: false, sponsorName: '', sponsorHeadline: '', sponsorBody: '', sponsorRating: '', sponsorCtaText: '', sponsorEmoji: '', sponsorTargetUrl: '',
};

const UNIT_RE = /^ca-app-pub-\d{16}\/\d{10}$/;

const FORMATS: { key: 'banner' | 'interstitial' | 'native' | 'rewarded'; title: string; desc: string; icon: React.ReactNode }[] = [
  { key: 'banner', title: 'Banner', desc: 'Adaptive banner above the bottom navigation', icon: <Rows3 className="h-4 w-4" /> },
  { key: 'interstitial', title: 'Interstitial', desc: 'Full-screen ad between job views (frequency-capped)', icon: <MonitorSmartphone className="h-4 w-4" /> },
  { key: 'native', title: 'Native', desc: 'In-feed card styled like a job listing', icon: <LayoutTemplate className="h-4 w-4" /> },
  { key: 'rewarded', title: 'Rewarded', desc: 'Optional video that unlocks premium tools', icon: <PlayCircle className="h-4 w-4" /> },
];

const PRESETS: { name: string; desc: string; patch: Partial<AdConfig> }[] = [
  { name: 'Light', desc: 'No interstitials', patch: { bannerEnabled: true, interstitialEnabled: false, nativeEnabled: true, rewardedEnabled: true, interstitialClickInterval: 5, interstitialCooldownSeconds: 120 } },
  { name: 'Balanced', desc: 'Recommended', patch: { bannerEnabled: true, interstitialEnabled: true, nativeEnabled: true, rewardedEnabled: true, interstitialClickInterval: 3, interstitialCooldownSeconds: 60 } },
  { name: 'Revenue', desc: 'More frequent', patch: { bannerEnabled: true, interstitialEnabled: true, nativeEnabled: true, rewardedEnabled: true, interstitialClickInterval: 2, interstitialCooldownSeconds: 30 } },
];

function SponsorPreview({ c }: { c: AdConfig }) {
  return (
    <div className="mx-auto w-full max-w-[320px] rounded-[2rem] border-[6px] border-foreground/90 bg-background p-3 shadow-pop">
      <div className="mb-3 h-1.5 w-16 mx-auto rounded-full bg-foreground/20" />
      <div className="rounded-2xl border bg-card p-3.5">
        <div className="mb-2 flex items-center justify-between">
          <span className="rounded bg-warning-soft px-1.5 py-0.5 text-[9px] font-semibold text-warning">Ad · प्रायोजित</span>
          <span className="text-[10px] text-muted-foreground">{c.sponsorRating}</span>
        </div>
        <div className="flex gap-3">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-muted text-xl">{c.sponsorEmoji || '📣'}</div>
          <div className="min-w-0">
            <p className="text-sm font-semibold leading-snug">{c.sponsorHeadline || 'Your headline'}</p>
            <p className="mt-0.5 text-[11px] text-muted-foreground">{c.sponsorName || 'Sponsor name'}</p>
          </div>
        </div>
        <p className="mt-2 text-xs text-muted-foreground line-clamp-3">{c.sponsorBody || 'Short description of the offer.'}</p>
        <div className="mt-3 rounded-lg bg-primary py-2 text-center text-xs font-semibold text-primary-foreground">{c.sponsorCtaText || 'Learn more'}</div>
      </div>
      <div className="mt-3 space-y-2">
        <div className="h-16 rounded-xl bg-muted" />
        <div className="h-16 rounded-xl bg-muted" />
      </div>
    </div>
  );
}

export default function AdsPage() {
  const toast = useToast();
  const [saved, setSaved] = useState<AdConfig>(DEFAULTS);
  const [config, setConfig] = useState<AdConfig>(DEFAULTS);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    adminApi
      .getAdConfig()
      .then((d) => {
        const merged = { ...DEFAULTS, ...d };
        setSaved(merged);
        setConfig(merged);
      })
      .catch((e) => setLoadError(e?.message || 'Could not load the current configuration'))
      .finally(() => setLoading(false));
  }, []);

  const set = <K extends keyof AdConfig>(k: K, v: AdConfig[K]) => setConfig((c) => ({ ...c, [k]: v }));
  const dirty = useMemo(() => JSON.stringify({ ...config, updatedAt: '' }) !== JSON.stringify({ ...saved, updatedAt: '' }), [config, saved]);

  const errors = useMemo(() => {
    const e: Record<string, string> = {};
    for (const f of FORMATS) {
      const v = (config[`${f.key}Id` as keyof AdConfig] as string).trim();
      if (v && !UNIT_RE.test(v)) e[`${f.key}Id`] = 'Format: ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX';
    }
    if (config.sponsorTargetUrl && !config.sponsorTargetUrl.startsWith('https://')) e.sponsorTargetUrl = 'Must start with https://';
    if (config.customSponsorActive && (!config.sponsorHeadline || !config.sponsorTargetUrl)) e.sponsor = 'Headline and link are required to show a sponsor card.';
    return e;
  }, [config]);

  const save = async () => {
    if (Object.keys(errors).length) {
      toast.error('Fix the highlighted fields first');
      return;
    }
    setSaving(true);
    try {
      const res = await adminApi.saveAdConfig(config);
      const merged = { ...DEFAULTS, ...res.data };
      setSaved(merged);
      setConfig(merged);
      toast.success('Ad settings published', 'The app picks them up on next launch.');
    } catch (err: any) {
      toast.error('Save failed', err?.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-10 w-64" />
        <Skeleton className="h-24 w-full rounded-xl" />
        <div className="grid gap-4 md:grid-cols-2">{Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-40 rounded-xl" />)}</div>
      </div>
    );
  }

  return (
    <div className="space-y-6 pb-24">
      <PageHeader
        title="Ads & monetization"
        description="Control which ad formats the app shows. Changes apply to release builds on next launch."
        actions={saved.updatedAt ? <span className="text-xs text-muted-foreground">Last published {formatDateTime(saved.updatedAt)}</span> : undefined}
      />

      {loadError && <Alert tone="warning" title="Showing defaults">{loadError}</Alert>}

      {/* Master switch */}
      <Card className={config.masterEnabled ? '' : 'border-danger/40'}>
        <div className="flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-start gap-3">
            <div className={`flex h-10 w-10 items-center justify-center rounded-xl ${config.masterEnabled ? 'bg-success-soft text-success' : 'bg-danger-soft text-danger'}`}>
              <Megaphone className="h-5 w-5" />
            </div>
            <div>
              <p className="font-semibold">All ads {config.masterEnabled ? 'on' : 'off'}</p>
              <p className="text-sm text-muted-foreground">Master switch. Turning it off hides every ad in the app immediately.</p>
            </div>
          </div>
          <Switch checked={config.masterEnabled} onChange={(v) => set('masterEnabled', v)} label="All ads" />
        </div>
        <div className="flex flex-wrap items-center gap-2 border-t px-5 py-3">
          <span className="mr-1 text-xs text-muted-foreground">Presets</span>
          {PRESETS.map((p) => (
            <Button key={p.name} size="sm" variant="outline" onClick={() => setConfig((c) => ({ ...c, ...p.patch }))} title={p.desc}>
              {p.name}
              <span className="text-muted-foreground font-normal">· {p.desc}</span>
            </Button>
          ))}
        </div>
      </Card>

      {/* Formats */}
      <div className={`grid gap-4 md:grid-cols-2 ${config.masterEnabled ? '' : 'pointer-events-none opacity-50'}`}>
        {FORMATS.map((f) => {
          const enabledKey = `${f.key}Enabled` as keyof AdConfig;
          const idKey = `${f.key}Id` as keyof AdConfig;
          const on = config[enabledKey] as boolean;
          return (
            <Card key={f.key}>
              <CardHeader
                icon={f.icon}
                title={<span className="flex items-center gap-2">{f.title} <Badge tone={on ? 'success' : 'neutral'}>{on ? 'On' : 'Off'}</Badge></span>}
                description={f.desc}
                actions={<Switch checked={on} onChange={(v) => set(enabledKey, v as never)} label={`${f.title} ads`} />}
              />
              <CardContent>
                <Field label="AdMob unit ID" hint="Leave empty to use the ID built into the app" error={errors[idKey as string]}>
                  <Input value={config[idKey] as string} onChange={(e) => set(idKey, e.target.value as never)} placeholder="ca-app-pub-…/…" className="font-mono text-xs" />
                </Field>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Frequency */}
      <Card>
        <CardHeader icon={<Gauge className="h-4 w-4" />} title="Interstitial frequency" description="Keeps full-screen ads from getting in the way of job seekers." />
        <CardContent className="grid gap-6 md:grid-cols-2">
          <Field label={`Show after every ${config.interstitialClickInterval} job opens`}>
            <input type="range" min={1} max={10} value={config.interstitialClickInterval} onChange={(e) => set('interstitialClickInterval', Number(e.target.value))} className="w-full accent-[hsl(var(--primary))]" />
          </Field>
          <Field label={`Wait at least ${config.interstitialCooldownSeconds} seconds between them`}>
            <input type="range" min={15} max={300} step={15} value={config.interstitialCooldownSeconds} onChange={(e) => set('interstitialCooldownSeconds', Number(e.target.value))} className="w-full accent-[hsl(var(--primary))]" />
          </Field>
        </CardContent>
      </Card>

      {/* Sponsor */}
      <Card>
        <CardHeader
          icon={<Sparkles className="h-4 w-4" />}
          title="Direct sponsor card"
          description="Your own sponsor shown as a native card. Only real, contracted sponsors."
          actions={<Switch checked={config.customSponsorActive} onChange={(v) => set('customSponsorActive', v)} label="Sponsor card" />}
        />
        <CardContent className="grid gap-8 lg:grid-cols-[1fr_auto]">
          <div className="grid gap-4 sm:grid-cols-2">
            {errors.sponsor && <Alert tone="warning" className="sm:col-span-2">{errors.sponsor}</Alert>}
            <Field label="Sponsor name"><Input value={config.sponsorName} onChange={(e) => set('sponsorName', e.target.value)} maxLength={80} /></Field>
            <Field label="Emoji"><Input value={config.sponsorEmoji} onChange={(e) => set('sponsorEmoji', e.target.value)} maxLength={8} placeholder="🎯" /></Field>
            <Field label="Headline" className="sm:col-span-2"><Input value={config.sponsorHeadline} onChange={(e) => set('sponsorHeadline', e.target.value)} maxLength={120} /></Field>
            <Field label="Description" className="sm:col-span-2"><Textarea value={config.sponsorBody} onChange={(e) => set('sponsorBody', e.target.value)} maxLength={300} /></Field>
            <Field label="Rating / tagline"><Input value={config.sponsorRating} onChange={(e) => set('sponsorRating', e.target.value)} maxLength={20} placeholder="4.8 ★" /></Field>
            <Field label="Button text"><Input value={config.sponsorCtaText} onChange={(e) => set('sponsorCtaText', e.target.value)} maxLength={40} placeholder="Learn more" /></Field>
            <Field label="Link" className="sm:col-span-2" error={errors.sponsorTargetUrl}>
              <Input type="url" value={config.sponsorTargetUrl} onChange={(e) => set('sponsorTargetUrl', e.target.value)} placeholder="https://…" />
            </Field>
          </div>
          <div className="hidden lg:block">
            <p className="mb-3 text-center text-xs font-medium text-muted-foreground">Preview</p>
            <SponsorPreview c={config} />
          </div>
        </CardContent>
      </Card>

      {/* Sticky save bar */}
      {dirty && (
        <div className="fixed inset-x-0 bottom-6 z-30 flex justify-center px-4 lg:pl-64">
          <div className="flex items-center gap-3 rounded-2xl border bg-card px-4 py-3 shadow-pop animate-slide-up">
            <span className="text-sm">Unsaved changes</span>
            <Button size="sm" variant="ghost" icon={<Undo2 className="h-3.5 w-3.5" />} onClick={() => setConfig(saved)}>Discard</Button>
            <Button size="sm" loading={saving} icon={<Save className="h-3.5 w-3.5" />} onClick={save}>Publish</Button>
          </div>
        </div>
      )}
    </div>
  );
}
