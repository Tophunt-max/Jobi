'use client';

import React from 'react';
import { LucideIcon } from 'lucide-react';

interface StatsCardProps {
  title: string;
  value: number | string;
  subtitle?: string;
  icon: LucideIcon;
  colorScheme: 'blue' | 'amber' | 'emerald' | 'indigo' | 'purple' | 'rose';
  trend?: string;
}

const colorMap = {
  blue: {
    bg: 'bg-blue-50',
    text: 'text-blue-700',
    iconBg: 'bg-blue-600',
    border: 'border-blue-100',
  },
  amber: {
    bg: 'bg-amber-50',
    text: 'text-amber-700',
    iconBg: 'bg-amber-500',
    border: 'border-amber-100',
  },
  emerald: {
    bg: 'bg-emerald-50',
    text: 'text-emerald-700',
    iconBg: 'bg-emerald-600',
    border: 'border-emerald-100',
  },
  indigo: {
    bg: 'bg-indigo-50',
    text: 'text-indigo-700',
    iconBg: 'bg-indigo-600',
    border: 'border-indigo-100',
  },
  purple: {
    bg: 'bg-purple-50',
    text: 'text-purple-700',
    iconBg: 'bg-purple-600',
    border: 'border-purple-100',
  },
  rose: {
    bg: 'bg-rose-50',
    text: 'text-rose-700',
    iconBg: 'bg-rose-600',
    border: 'border-rose-100',
  },
};

export function StatsCard({ title, value, subtitle, icon: Icon, colorScheme, trend }: StatsCardProps) {
  const c = colorMap[colorScheme] || colorMap.blue;

  return (
    <div className="bg-white rounded-xl p-5 border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{title}</p>
          <div className="flex items-baseline gap-2 mt-1.5">
            <h3 className="text-2xl font-black text-slate-900 tracking-tight">{value}</h3>
            {trend && (
              <span className={`text-[11px] font-bold px-1.5 py-0.5 rounded ${c.bg} ${c.text}`}>
                {trend}
              </span>
            )}
          </div>
          {subtitle && <p className="text-xs text-slate-500 mt-1">{subtitle}</p>}
        </div>
        <div className={`w-12 h-12 rounded-xl ${c.iconBg} flex items-center justify-center text-white shadow-md shadow-slate-200`}>
          <Icon className="w-6 h-6" />
        </div>
      </div>
    </div>
  );
}
