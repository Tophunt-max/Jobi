import { create } from 'zustand';

import { makeId } from '@/lib/format';
import { ApiError } from '@/services/api/client';
import { generateAi, type AiTask } from '@/services/api/endpoints';
import { useUserStore } from '@/store/userStore';
import type { Job } from '@/types/domain';

import { offlineEligibility, offlineQuery, offlineStrategy } from './offline';
import { eligibilityPrompt, queryPrompt, strategyPrompt } from './prompts';

export type MentorMessage = {
  id: string;
  role: 'user' | 'assistant';
  text: string;
  /** 'offline' answers come from local rules because the AI service failed. */
  source?: 'ai' | 'offline';
  notice?: string;
};

type MentorState = {
  messages: MentorMessage[];
  busy: boolean;
  askEligibility: (job: Job) => Promise<void>;
  askStrategy: (examName: string, job?: Job | null) => Promise<void>;
  /** `display` is shown in the chat instead of the full question (e.g. when notice details are attached). */
  askQuestion: (question: string, display?: string) => Promise<void>;
  clear: () => void;
};

let controller: AbortController | null = null;

function noticeFor(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.kind === 'rate_limited') return 'The AI mentor is busy. Showing an offline answer; try again in a minute.';
    if (e.kind === 'not_configured') return 'AI is not configured in this build. Showing an offline answer.';
  }
  return 'Could not reach the AI mentor. Showing an offline answer.';
}

export const useMentorStore = create<MentorState>()((set, get) => {
  async function run(task: AiTask, userText: string, prompt: string, fallback: () => string) {
    if (get().busy) return;
    controller?.abort();
    controller = new AbortController();
    set((s) => ({ busy: true, messages: [...s.messages, { id: makeId('m'), role: 'user', text: userText }] }));
    let reply: MentorMessage;
    try {
      const text = await generateAi(task, prompt, controller.signal);
      reply = { id: makeId('m'), role: 'assistant', text, source: 'ai' };
    } catch (e) {
      if (controller.signal.aborted) {
        set({ busy: false });
        return;
      }
      reply = { id: makeId('m'), role: 'assistant', text: fallback(), source: 'offline', notice: noticeFor(e) };
    }
    set((s) => ({ busy: false, messages: [...s.messages, reply] }));
  }

  return {
    messages: [],
    busy: false,
    askEligibility: (job) => {
      const profile = useUserStore.getState().profile;
      return run('eligibility', `Am I eligible for "${job.title}"?`, eligibilityPrompt(profile, job), () =>
        offlineEligibility(profile, job),
      );
    },
    askStrategy: (examName, job) =>
      run('strategy', `Preparation strategy for ${examName}`, strategyPrompt(examName, job), () => offlineStrategy(examName)),
    askQuestion: (question, display) => {
      const profile = useUserStore.getState().profile;
      return run('query', display ?? question, queryPrompt(question, profile), offlineQuery);
    },
    clear: () => {
      controller?.abort();
      set({ messages: [], busy: false });
    },
  };
});
