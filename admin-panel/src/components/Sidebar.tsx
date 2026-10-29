'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useSession } from './AuthGate';
import { 
  LayoutDashboard, 
  Clock, 
  Globe2, 
  Briefcase, 
  History, 
  Settings, 
  ExternalLink,
  ShieldCheck,
  Server,
  Megaphone,
  Flag,
  LogOut
} from 'lucide-react';

const navigation = [
  { name: 'Dashboard', href: '/', icon: LayoutDashboard },
  { name: 'Pending Review', href: '/pending', icon: Clock, badge: 'Review' },
  { name: 'Official Sources', href: '/sources', icon: Globe2 },
  { name: 'Active Notices & Jobs', href: '/jobs', icon: Briefcase },
  { name: 'User Reports', href: '/reports', icon: Flag },
  { name: 'Ads & Monetization', href: '/ads', icon: Megaphone, badge: 'Revenue' },
  { name: 'Import Runs & Logs', href: '/runs', icon: History },
  { name: 'Settings & Team', href: '/settings', icon: Settings },
];

export function Sidebar() {
  const [currentPath, setCurrentPath] = useState('/');
  const { user, signOut } = useSession();

  useEffect(() => {
    if (typeof window !== 'undefined') {
      setCurrentPath(window.location.pathname);
    }
  }, []);

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col h-screen fixed left-0 top-0 z-30 select-none">
      {/* Brand Header */}
      <div className="h-16 flex items-center px-6 border-b border-slate-800 gap-3">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-500 flex items-center justify-center text-white shadow-lg shadow-blue-500/20">
          <ShieldCheck className="w-6 h-6" />
        </div>
        <div>
          <h1 className="text-sm font-bold text-white tracking-wide">SARKARI PORTAL</h1>
          <p className="text-[11px] text-blue-400 font-medium">Cloudflare Admin Panel</p>
        </div>
      </div>

      {/* Cloudflare Tech Badge */}
      <div className="px-4 pt-4 pb-2">
        <div className="bg-slate-800/60 rounded-lg p-2.5 border border-slate-700/60 flex items-center gap-2">
          <Server className="w-4 h-4 text-emerald-400 shrink-0" />
          <div className="text-[11px] text-slate-300">
            <span className="font-semibold text-white">Workers + D1 + R2</span>
            <p className="text-slate-400 text-[10px]">Production Edge Pipeline</p>
          </div>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {navigation.map((item) => {
          const isActive = currentPath === item.href || (item.href !== '/' && currentPath.startsWith(item.href));
          const Icon = item.icon;
          return (
            <Link
              key={item.name}
              href={item.href}
              onClick={() => setCurrentPath(item.href)}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                isActive
                  ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/80'
              }`}
            >
              <Icon className={`w-4 h-4 ${isActive ? 'text-white' : 'text-slate-400'}`} />
              <span className="flex-1">{item.name}</span>
              {item.badge && (
                <span className={`text-[10px] px-1.5 py-0.5 rounded font-bold uppercase tracking-wider ${
                  isActive ? 'bg-blue-800 text-blue-100' : 'bg-amber-500/20 text-amber-300'
                }`}>
                  {item.badge}
                </span>
              )}
            </Link>
          );
        })}
      </nav>

      {/* Signed-in user */}
      <div className="px-4 py-3 border-t border-slate-800/80 flex items-center justify-between gap-2">
        <div className="min-w-0">
          <div className="text-[11px] text-white font-semibold truncate" title={user.email}>{user.email}</div>
          <div className="text-[10px] text-slate-500 capitalize">{user.role}</div>
        </div>
        <button onClick={signOut} title="Sign out" aria-label="Sign out" className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800">
          <LogOut className="w-4 h-4" />
        </button>
      </div>

      {/* Footer Info */}
      <div className="p-4 border-t border-slate-800/80 text-xs text-slate-400">
        <div className="flex items-center justify-between">
          <span className="text-[11px] text-slate-500">Cron Schedule</span>
          <span className="text-[11px] text-emerald-400 font-mono font-medium">Every 3 Hours</span>
        </div>
        <p className="text-[10px] text-slate-500 mt-1">
          SSC • UPSC • RRB • OPSC • OSSC
        </p>
      </div>
    </aside>
  );
}
