import { create } from 'zustand';

import { DEFAULT_FILTERS, type JobFilters } from './logic';

/** Jobs-tab filters. Shared so Home can open the Jobs tab with a filter applied. */
type FiltersState = {
  filters: JobFilters;
  set: (patch: Partial<JobFilters>) => void;
  reset: (patch?: Partial<JobFilters>) => void;
};

export const useJobFilters = create<FiltersState>()((set) => ({
  filters: DEFAULT_FILTERS,
  set: (patch) => set((s) => ({ filters: { ...s.filters, ...patch } })),
  reset: (patch) => set({ filters: { ...DEFAULT_FILTERS, ...patch } }),
}));
