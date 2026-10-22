'use client';

import React, { useState, useEffect } from 'react';
import { Header } from '../../components/Header';
import { adminApi } from '../../lib/api';
import { AdConfig } from '../../lib/types';
import { 
  Megaphone, 
  Save, 
  RefreshCw, 
  CheckCircle2, 
  AlertTriangle, 
  ShieldAlert, 
  Sparkles, 
  Layers, 
  Sliders, 
  ExternalLink, 
  Smartphone,
  Eye,
  Info,
  Check,
  Zap,
  SlidersHorizontal,
  DollarSign
} from 'lucide-react';

const TEST_IDS = {
  banner: 'ca-app-pub-3940256099942544/6300978111',
  interstitial: 'ca-app-pub-3940256099942544/1033173712',
  native: 'ca-app-pub-3940256099942544/2247696110',
  rewarded: 'ca-app-pub-3940256099942544/5224354917',
};

const DEFAULT_CONFIG: AdConfig = {
  masterEnabled: true,
  bannerEnabled: true,
  interstitialEnabled: true,
  nativeEnabled: true,
  rewardedEnabled: true,
  bannerId: '',
  interstitialId: '',
  nativeId: '',
  rewardedId: '',
  interstitialClickInterval: 3,
  interstitialCooldownSeconds: 60,
  customSponsorActive: false,
  sponsorName: 'Sarkari Pariksha Partner',
  sponsorHeadline: '🎯 All India Mock Test Series 2026',
  sponsorBody: '500+ Sectional & Full-length tests with live All-India rank predictor & video solutions.',
  sponsorRating: '4.9 ★',
  sponsorCtaText: 'Start Free Test',
  sponsorEmoji: '🎯',
  sponsorTargetUrl: 'https://play.google.com/store/search?q=sarkari+mock+test&c=apps',
};

export default function AdsManagementPage() {
  const [config, setConfig] = useState<AdConfig>(DEFAULT_CONFIG);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'formats' | 'frequency' | 'sponsor' | 'preview'>('formats');

  useEffect(() => {
    fetchConfig();
  }, []);

  const fetchConfig = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const data = await adminApi.getAdConfig();
      if (data) {
        setConfig({ ...DEFAULT_CONFIG, ...data });
      }
    } catch (err: any) {
      console.warn('Using default config due to connection:', err.message);
      // Keep DEFAULT_CONFIG if backend is fresh or offline
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    setErrorMessage(null);
    setSaveSuccess(false);
    try {
      const res = await adminApi.saveAdConfig(config);
      if (res.success) {
        setSaveSuccess(true);
        setTimeout(() => setSaveSuccess(false), 3500);
      } else {
        setErrorMessage(res.message || 'Failed to save configuration.');
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Error communicating with Cloudflare Worker.');
    } finally {
      setSaving(false);
    }
  };

  const applyPreset = (presetName: 'safe' | 'balanced' | 'aggressive') => {
    if (presetName === 'safe') {
      setConfig((prev) => ({
        ...prev,
        masterEnabled: true,
        bannerEnabled: true,
        interstitialEnabled: false,
        nativeEnabled: true,
        rewardedEnabled: true,
        interstitialClickInterval: 5,
        interstitialCooldownSeconds: 120,
      }));
    } else if (presetName === 'balanced') {
      setConfig((prev) => ({
        ...prev,
        masterEnabled: true,
        bannerEnabled: true,
        interstitialEnabled: true,
        nativeEnabled: true,
        rewardedEnabled: true,
        interstitialClickInterval: 3,
        interstitialCooldownSeconds: 60,
      }));
    } else if (presetName === 'aggressive') {
      setConfig((prev) => ({
        ...prev,
        masterEnabled: true,
        bannerEnabled: true,
        interstitialEnabled: true,
        nativeEnabled: true,
        rewardedEnabled: true,
        customSponsorActive: true,
        interstitialClickInterval: 2,
        interstitialCooldownSeconds: 30,
      }));
    }
  };

  const loadTestIds = () => {
    setConfig((prev) => ({
      ...prev,
      bannerId: TEST_IDS.banner,
      interstitialId: TEST_IDS.interstitial,
      nativeId: TEST_IDS.native,
      rewardedId: TEST_IDS.rewarded,
    }));
  };

  const clearToDefaultIds = () => {
    setConfig((prev) => ({
      ...prev,
      bannerId: '',
      interstitialId: '',
      nativeId: '',
      rewardedId: '',
    }));
  };

  const isUsingProduction = Boolean(
    config.bannerId.trim() ||
    config.interstitialId.trim() ||
    config.nativeId.trim() ||
    config.rewardedId.trim()
  );

  return (
    <>
      <Header
        title="Ads & Monetization Control Center"
        subtitle="Configure AdMob placements, frequency capping, and direct affiliate sponsorships across all active Android apps"
      />

      <main className="p-8 space-y-8 flex-1 max-w-6xl pb-24">
        {/* Top Notification Alerts */}
        {saveSuccess && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl p-4 flex items-center gap-3 animate-fade-in shadow-sm">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0" />
            <div className="flex-1">
              <p className="text-sm font-bold">Ad Configuration Synced to Edge & Mobile Devices!</p>
              <p className="text-xs text-emerald-700">Changes are deployed instantly via Cloudflare D1. All mobile clients receive these updates on their next sync cycle.</p>
            </div>
          </div>
        )}

        {errorMessage && (
          <div className="bg-rose-50 border border-rose-200 text-rose-800 rounded-xl p-4 flex items-center gap-3 shadow-sm">
            <AlertTriangle className="w-5 h-5 text-rose-600 shrink-0" />
            <div className="flex-1">
              <p className="text-sm font-bold">Failed to sync settings with Cloudflare Worker</p>
              <p className="text-xs text-rose-700">{errorMessage}</p>
            </div>
          </div>
        )}

        {/* Master Control & Status Bar */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="space-y-1">
            <div className="flex items-center gap-3">
              <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold ${
                config.masterEnabled 
                  ? 'bg-emerald-100 text-emerald-800 border border-emerald-300' 
                  : 'bg-rose-100 text-rose-800 border border-rose-300'
              }`}>
                {config.masterEnabled ? '● MONETIZATION ACTIVE' : '○ ADS PAUSED'}
              </span>

              <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${
                isUsingProduction 
                  ? 'bg-blue-100 text-blue-800 border border-blue-200' 
                  : 'bg-amber-100 text-amber-800 border border-amber-200'
              }`}>
                {isUsingProduction ? '🚀 Live AdMob Production IDs' : '🧪 Google Official Test Mode'}
              </span>

              {config.customSponsorActive && (
                <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800 border border-purple-200">
                  ⭐ Sponsor Campaign Active
                </span>
              )}
            </div>
            <h2 className="text-lg font-bold text-slate-900">Global Monetization Killswitch</h2>
            <p className="text-xs text-slate-500">
              Disabling the master switch will immediately suppress all banners, interstitials, and native ads in the mobile app.
            </p>
          </div>

          <div className="flex items-center gap-4 w-full md:w-auto justify-between md:justify-end">
            <label className="relative inline-flex items-center cursor-pointer">
              <input 
                type="checkbox" 
                checked={config.masterEnabled} 
                onChange={(e) => setConfig({ ...config, masterEnabled: e.target.checked })} 
                className="sr-only peer"
              />
              <div className="w-14 h-7 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[4px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-6 after:w-6 after:transition-all peer-checked:bg-emerald-600"></div>
            </label>

            <button
              onClick={handleSave}
              disabled={saving}
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm shadow-md shadow-blue-500/20 disabled:opacity-50 transition"
            >
              <Save className="w-4 h-4" />
              {saving ? 'Deploying to Cloudflare...' : 'Save & Publish'}
            </button>
          </div>
        </div>

        {/* Quick Monetization Presets */}
        <div className="bg-slate-50 border border-slate-200 rounded-2xl p-4 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <SlidersHorizontal className="w-4 h-4 text-slate-500" />
            <span className="text-xs font-bold text-slate-700 uppercase tracking-wider">Quick Presets:</span>
          </div>
          <div className="flex flex-wrap gap-2 w-full sm:w-auto">
            <button
              onClick={() => applyPreset('safe')}
              className="text-xs px-3 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-100 text-slate-700 font-medium transition"
            >
              🌱 Aspirant-Friendly (No Interstitials)
            </button>
            <button
              onClick={() => applyPreset('balanced')}
              className="text-xs px-3 py-1.5 rounded-lg border border-blue-200 bg-blue-50 hover:bg-blue-100 text-blue-800 font-semibold transition"
            >
              ⚖️ Balanced (Recommended)
            </button>
            <button
              onClick={() => applyPreset('aggressive')}
              className="text-xs px-3 py-1.5 rounded-lg border border-purple-200 bg-purple-50 hover:bg-purple-100 text-purple-800 font-medium transition"
            >
              🔥 Max Revenue + Sponsor
            </button>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="flex border-b border-slate-200 gap-2">
          <button
            onClick={() => setActiveTab('formats')}
            className={`pb-3 px-4 font-semibold text-sm flex items-center gap-2 border-b-2 transition ${
              activeTab === 'formats'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            <Layers className="w-4 h-4" />
            Ad Formats & Unit IDs
          </button>
          <button
            onClick={() => setActiveTab('frequency')}
            className={`pb-3 px-4 font-semibold text-sm flex items-center gap-2 border-b-2 transition ${
              activeTab === 'frequency'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            <Sliders className="w-4 h-4" />
            Frequency Capping & Guardrails
          </button>
          <button
            onClick={() => setActiveTab('sponsor')}
            className={`pb-3 px-4 font-semibold text-sm flex items-center gap-2 border-b-2 transition ${
              activeTab === 'sponsor'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            <Sparkles className="w-4 h-4" />
            Direct Sponsor / Mock Test Affiliate
          </button>
          <button
            onClick={() => setActiveTab('preview')}
            className={`pb-3 px-4 font-semibold text-sm flex items-center gap-2 border-b-2 transition ${
              activeTab === 'preview'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            <Smartphone className="w-4 h-4" />
            Mobile App Live Preview
          </button>
        </div>

        {/* TAB 1: Ad Formats & Unit IDs */}
        {activeTab === 'formats' && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-blue-50 border border-blue-200 rounded-xl p-4">
              <div className="flex items-start gap-3">
                <Info className="w-5 h-5 text-blue-600 shrink-0 mt-0.5" />
                <div className="text-xs text-blue-900 leading-relaxed">
                  <p className="font-bold">Google AdMob Production vs Test IDs</p>
                  <p className="text-blue-700">Leave Unit ID fields blank or use Google test IDs during development to avoid policy suspensions.</p>
                </div>
              </div>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={loadTestIds}
                  className="px-3 py-1.5 rounded-lg bg-white border border-blue-300 text-blue-700 hover:bg-blue-100 text-xs font-semibold"
                >
                  Fill Official Test IDs
                </button>
                <button
                  type="button"
                  onClick={clearToDefaultIds}
                  className="px-3 py-1.5 rounded-lg bg-white border border-slate-300 text-slate-700 hover:bg-slate-100 text-xs font-semibold"
                >
                  Reset to Blank
                </button>
              </div>
            </div>

            {/* 1. Adaptive Banner */}
            <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-base font-bold text-slate-900">1. Adaptive Bottom Banner</h3>
                    <span className="text-[11px] px-2 py-0.5 rounded bg-blue-100 text-blue-800 font-semibold">320x50 / Adaptive</span>
                  </div>
                  <p className="text-xs text-slate-500 mt-1">
                    Pinned seamlessly above the bottom navigation bar on all main tabs (Home, Jobs, Results, Profile).
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input 
                    type="checkbox" 
                    checked={config.bannerEnabled} 
                    onChange={(e) => setConfig({ ...config, bannerEnabled: e.target.checked })} 
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700">AdMob Banner Unit ID</label>
                <input
                  type="text"
                  placeholder={`e.g. ${TEST_IDS.banner}`}
                  value={config.bannerId}
                  onChange={(e) => setConfig({ ...config, bannerId: e.target.value })}
                  className="w-full px-3.5 py-2.5 text-xs font-mono bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white"
                />
                <p className="text-[11px] text-slate-400">Fallback default: {TEST_IDS.banner}</p>
              </div>
            </div>

            {/* 2. Full-Screen Interstitial */}
            <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-base font-bold text-slate-900">2. Full-Screen Interstitial Ad</h3>
                    <span className="text-[11px] px-2 py-0.5 rounded bg-amber-100 text-amber-800 font-semibold">High eCPM</span>
                  </div>
                  <p className="text-xs text-slate-500 mt-1">
                    Appears naturally when an aspirant taps to open a detailed recruitment circular or gazette notice.
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input 
                    type="checkbox" 
                    checked={config.interstitialEnabled} 
                    onChange={(e) => setConfig({ ...config, interstitialEnabled: e.target.checked })} 
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700">AdMob Interstitial Unit ID</label>
                <input
                  type="text"
                  placeholder={`e.g. ${TEST_IDS.interstitial}`}
                  value={config.interstitialId}
                  onChange={(e) => setConfig({ ...config, interstitialId: e.target.value })}
                  className="w-full px-3.5 py-2.5 text-xs font-mono bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white"
                />
                <p className="text-[11px] text-slate-400">Fallback default: {TEST_IDS.interstitial}</p>
              </div>
            </div>

            {/* 3. Native In-Feed Job Ad */}
            <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-base font-bold text-slate-900">3. Native In-Feed Job Listing Ad</h3>
                    <span className="text-[11px] px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 font-semibold">Best CTR</span>
                  </div>
                  <p className="text-xs text-slate-500 mt-1">
                    Blends natively into the candidate search and jobs feed at index #3 with "Sponsored Job Notice" tag.
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input 
                    type="checkbox" 
                    checked={config.nativeEnabled} 
                    onChange={(e) => setConfig({ ...config, nativeEnabled: e.target.checked })} 
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700">AdMob Native Unit ID</label>
                <input
                  type="text"
                  placeholder={`e.g. ${TEST_IDS.native}`}
                  value={config.nativeId}
                  onChange={(e) => setConfig({ ...config, nativeId: e.target.value })}
                  className="w-full px-3.5 py-2.5 text-xs font-mono bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white"
                />
                <p className="text-[11px] text-slate-400">Fallback default: {TEST_IDS.native}</p>
              </div>
            </div>

            {/* 4. Rewarded Video Ad */}
            <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="text-base font-bold text-slate-900">4. Rewarded Video Ad</h3>
                    <span className="text-[11px] px-2 py-0.5 rounded bg-purple-100 text-purple-800 font-semibold">User Opt-in</span>
                  </div>
                  <p className="text-xs text-slate-500 mt-1">
                    Rewarded placement when candidates unlock full official PDF schemes or extra mock test papers.
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input 
                    type="checkbox" 
                    checked={config.rewardedEnabled} 
                    onChange={(e) => setConfig({ ...config, rewardedEnabled: e.target.checked })} 
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700">AdMob Rewarded Unit ID</label>
                <input
                  type="text"
                  placeholder={`e.g. ${TEST_IDS.rewarded}`}
                  value={config.rewardedId}
                  onChange={(e) => setConfig({ ...config, rewardedId: e.target.value })}
                  className="w-full px-3.5 py-2.5 text-xs font-mono bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white"
                />
                <p className="text-[11px] text-slate-400">Fallback default: {TEST_IDS.rewarded}</p>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: Frequency Capping & Guardrails */}
        {activeTab === 'frequency' && (
          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-6">
            <div>
              <h3 className="text-base font-bold text-slate-900">User Experience Guardrails & Frequency Caps</h3>
              <p className="text-xs text-slate-500 mt-1">
                Carefully calibrate ad density so genuine government job seekers never experience ad fatigue or spam.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
              {/* Click Interval Slider */}
              <div className="border border-slate-200 rounded-xl p-5 bg-slate-50/50 space-y-3">
                <div className="flex justify-between items-center">
                  <label className="text-sm font-bold text-slate-800">Job Click Interval</label>
                  <span className="text-sm font-bold text-blue-600 bg-blue-100 px-3 py-1 rounded-full">
                    Every {config.interstitialClickInterval} clicks
                  </span>
                </div>
                <p className="text-xs text-slate-500">
                  How many job notices a candidate must view before an interstitial ad is triggered.
                </p>
                <input
                  type="range"
                  min="1"
                  max="10"
                  step="1"
                  value={config.interstitialClickInterval}
                  onChange={(e) => setConfig({ ...config, interstitialClickInterval: parseInt(e.target.value, 10) })}
                  className="w-full accent-blue-600 cursor-pointer"
                />
                <div className="flex justify-between text-[11px] text-slate-400">
                  <span>1 (Every Click - High Density)</span>
                  <span>3 (Recommended)</span>
                  <span>10 (Rare)</span>
                </div>
              </div>

              {/* Cooldown Seconds Slider */}
              <div className="border border-slate-200 rounded-xl p-5 bg-slate-50/50 space-y-3">
                <div className="flex justify-between items-center">
                  <label className="text-sm font-bold text-slate-800">Minimum Cooldown Window</label>
                  <span className="text-sm font-bold text-emerald-600 bg-emerald-100 px-3 py-1 rounded-full">
                    {config.interstitialCooldownSeconds} seconds
                  </span>
                </div>
                <p className="text-xs text-slate-500">
                  Guaranteed time gap between two consecutive full-screen interstitial ads.
                </p>
                <input
                  type="range"
                  min="15"
                  max="180"
                  step="5"
                  value={config.interstitialCooldownSeconds}
                  onChange={(e) => setConfig({ ...config, interstitialCooldownSeconds: parseInt(e.target.value, 10) })}
                  className="w-full accent-emerald-600 cursor-pointer"
                />
                <div className="flex justify-between text-[11px] text-slate-400">
                  <span>15s (Aggressive)</span>
                  <span>60s (Recommended)</span>
                  <span>180s (3 Minutes)</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: Direct Sponsor / Mock Test Affiliate */}
        {activeTab === 'sponsor' && (
          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-base font-bold text-slate-900">Custom Sponsor & Test Series Affiliate</h3>
                <p className="text-xs text-slate-500 mt-1">
                  Promote coaching institutes, mock test packages, or custom affiliate deals directly in the mobile app without AdMob revenue share.
                </p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input 
                  type="checkbox" 
                  checked={config.customSponsorActive} 
                  onChange={(e) => setConfig({ ...config, customSponsorActive: e.target.checked })} 
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-purple-600"></div>
              </label>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700">Sponsor / Partner Name</label>
                <input
                  type="text"
                  value={config.sponsorName}
                  onChange={(e) => setConfig({ ...config, sponsorName: e.target.value })}
                  placeholder="e.g. Sarkari Pariksha Partner"
                  className="w-full px-3.5 py-2 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:bg-white"
                />
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700">Emoji / Icon Badge</label>
                <input
                  type="text"
                  value={config.sponsorEmoji}
                  onChange={(e) => setConfig({ ...config, sponsorEmoji: e.target.value })}
                  placeholder="e.g. 🎯 or 📚 or 🏆"
                  className="w-full px-3.5 py-2 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:bg-white"
                />
              </div>

              <div className="space-y-1 md:col-span-2">
                <label className="text-xs font-bold text-slate-700">Headline</label>
                <input
                  type="text"
                  value={config.sponsorHeadline}
                  onChange={(e) => setConfig({ ...config, sponsorHeadline: e.target.value })}
                  placeholder="e.g. 🎯 All India Mock Test Series 2026"
                  className="w-full px-3.5 py-2 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:bg-white font-semibold"
                />
              </div>

              <div className="space-y-1 md:col-span-2">
                <label className="text-xs font-bold text-slate-700">Body Description</label>
                <textarea
                  rows={2}
                  value={config.sponsorBody}
                  onChange={(e) => setConfig({ ...config, sponsorBody: e.target.value })}
                  placeholder="e.g. 500+ Sectional & Full-length tests with live All-India rank predictor & video solutions."
                  className="w-full px-3.5 py-2 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:bg-white"
                />
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700">Rating / Social Proof</label>
                <input
                  type="text"
                  value={config.sponsorRating}
                  onChange={(e) => setConfig({ ...config, sponsorRating: e.target.value })}
                  placeholder="e.g. 4.9 ★"
                  className="w-full px-3.5 py-2 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:bg-white"
                />
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700">Call-To-Action Button Text</label>
                <input
                  type="text"
                  value={config.sponsorCtaText}
                  onChange={(e) => setConfig({ ...config, sponsorCtaText: e.target.value })}
                  placeholder="e.g. Start Free Test"
                  className="w-full px-3.5 py-2 text-xs bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:bg-white font-bold"
                />
              </div>

              <div className="space-y-1 md:col-span-2">
                <label className="text-xs font-bold text-slate-700">Target Destination URL (Web or Play Store)</label>
                <input
                  type="url"
                  value={config.sponsorTargetUrl}
                  onChange={(e) => setConfig({ ...config, sponsorTargetUrl: e.target.value })}
                  placeholder="https://example.com/mock-test-series"
                  className="w-full px-3.5 py-2 text-xs font-mono bg-slate-50 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:bg-white"
                />
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: Mobile App Live Preview */}
        {activeTab === 'preview' && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-start">
            {/* Native Card Preview */}
            <div className="space-y-3">
              <h3 className="text-sm font-bold text-slate-800 flex items-center gap-2">
                <Eye className="w-4 h-4 text-emerald-600" />
                Native In-Feed Job Ad (Candidate View)
              </h3>
              <p className="text-xs text-slate-500">
                How candidates see the sponsored card while scrolling through sarkari vacancies:
              </p>

              <div className="border border-slate-200 rounded-2xl bg-white p-5 shadow-sm space-y-3 relative overflow-hidden">
                <div className="flex items-center justify-between">
                  <span className="bg-amber-100 text-amber-900 border border-amber-300 text-[10px] font-bold px-2 py-0.5 rounded">
                    SPONSORED NOTICE
                  </span>
                  <span className="text-[11px] text-slate-400 font-medium">Google AdMob</span>
                </div>

                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-xl bg-gradient-to-tr from-amber-500 to-orange-400 flex items-center justify-center text-xl text-white font-bold shadow-md shadow-amber-500/20">
                    🏛️
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-slate-900">UPSC / SSC Free Prep Kit 2026</h4>
                    <p className="text-xs text-slate-500">Official Exam Mentorship & PYQ Papers</p>
                  </div>
                </div>

                <p className="text-xs text-slate-600 leading-relaxed">
                  Download previous 10-year solved papers with video explanations. Instant verified download for aspirants.
                </p>

                <div className="pt-1 flex items-center justify-between">
                  <span className="text-xs font-bold text-emerald-600">Free PDF Access</span>
                  <button className="px-4 py-1.5 rounded-lg bg-blue-600 text-white text-xs font-bold shadow-sm">
                    Open Notice
                  </button>
                </div>
              </div>
            </div>

            {/* Direct Sponsor Affiliate Card Preview */}
            <div className="space-y-3">
              <h3 className="text-sm font-bold text-slate-800 flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-purple-600" />
                Direct Sponsor Card (Mock Test Affiliate)
              </h3>
              <p className="text-xs text-slate-500">
                {config.customSponsorActive 
                  ? 'Active in mobile apps right now:' 
                  : 'Currently inactive (Enable in Direct Sponsor tab)'}
              </p>

              <div className={`border-2 rounded-2xl p-5 shadow-sm space-y-3 transition ${
                config.customSponsorActive 
                  ? 'border-purple-300 bg-gradient-to-br from-purple-50 via-white to-indigo-50/50' 
                  : 'border-dashed border-slate-300 bg-slate-50 opacity-60'
              }`}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1.5">
                    <span className="text-xs font-bold text-purple-700 bg-purple-100 px-2 py-0.5 rounded border border-purple-200">
                      PARTNER SPOTLIGHT
                    </span>
                    <span className="text-xs font-bold text-amber-600">{config.sponsorRating}</span>
                  </div>
                  <span className="text-xs text-slate-400 font-mono">{config.sponsorName}</span>
                </div>

                <div className="flex items-start gap-3">
                  <div className="w-11 h-11 rounded-xl bg-purple-600 text-white flex items-center justify-center text-xl shrink-0 shadow-md shadow-purple-500/20">
                    {config.sponsorEmoji || '🎯'}
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-slate-900">{config.sponsorHeadline}</h4>
                    <p className="text-xs text-slate-600 mt-1 leading-relaxed">{config.sponsorBody}</p>
                  </div>
                </div>

                <div className="pt-2 flex justify-end">
                  <button className="px-4 py-2 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white text-xs font-bold shadow-md shadow-purple-500/20 flex items-center gap-1.5">
                    {config.sponsorCtaText}
                    <ExternalLink className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </>
  );
}
