'use client';

import React from 'react';
import { Badge, Tone } from './ui';

const STATUS: Record<string, { tone: Tone; label: string }> = {
  pending_review: { tone: 'warning', label: 'In review' },
  ACTIVE: { tone: 'success', label: 'Published' },
  CLOSING_SOON: { tone: 'warning', label: 'Closing soon' },
  REJECTED: { tone: 'danger', label: 'Rejected' },
  EXPIRED: { tone: 'neutral', label: 'Expired' },
  SUCCESS: { tone: 'success', label: 'Success' },
  PARTIAL: { tone: 'warning', label: 'Partial' },
  FAILED: { tone: 'danger', label: 'Failed' },
  RUNNING: { tone: 'info', label: 'Running' },
  OPEN: { tone: 'warning', label: 'Open' },
  RESOLVED: { tone: 'success', label: 'Resolved' },
  DISMISSED: { tone: 'neutral', label: 'Dismissed' },
};

export function StatusBadge({ status }: { status: string }) {
  const s = STATUS[status] || { tone: 'neutral' as Tone, label: status };
  return (
    <Badge tone={s.tone} dot>
      {s.label}
    </Badge>
  );
}

const CATEGORY_TONE: Record<string, Tone> = {
  'Latest Job': 'primary',
  'Admit Card': 'info',
  Result: 'success',
  'Answer Key': 'warning',
  Syllabus: 'neutral',
  Admission: 'info',
};

export function CategoryBadge({ category }: { category: string }) {
  return <Badge tone={CATEGORY_TONE[category] || 'neutral'}>{category}</Badge>;
}
