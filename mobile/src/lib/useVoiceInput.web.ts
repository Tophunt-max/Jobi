import { useCallback, useRef, useState } from 'react';

import { i18n } from '@/i18n';

type Recognition = {
  lang: string;
  interimResults: boolean;
  maxAlternatives: number;
  onresult: ((e: { results: ArrayLike<ArrayLike<{ transcript: string }>> }) => void) | null;
  onend: (() => void) | null;
  onerror: (() => void) | null;
  start: () => void;
  stop: () => void;
};

type RecognitionCtor = new () => Recognition;

function getCtor(): RecognitionCtor | null {
  if (typeof window === 'undefined') return null;
  const w = window as unknown as { SpeechRecognition?: RecognitionCtor; webkitSpeechRecognition?: RecognitionCtor };
  return w.SpeechRecognition ?? w.webkitSpeechRecognition ?? null;
}

/** Browser speech recognition (Chrome, Edge, Safari). Falls back to typing when unavailable. */
export function useVoiceInput(onResult: (text: string) => void) {
  const [listening, setListening] = useState(false);
  const active = useRef<Recognition | null>(null);
  const Ctor = getCtor();

  const start = useCallback(() => {
    if (!Ctor) return;
    if (active.current) {
      active.current.stop();
      return;
    }
    const rec = new Ctor();
    rec.lang = i18n.language === 'en' ? 'en-IN' : `${i18n.language}-IN`;
    rec.interimResults = false;
    rec.maxAlternatives = 1;
    rec.onresult = (e) => {
      const text = e.results[0]?.[0]?.transcript?.trim();
      if (text) onResult(text);
    };
    rec.onend = () => {
      active.current = null;
      setListening(false);
    };
    rec.onerror = rec.onend;
    active.current = rec;
    setListening(true);
    rec.start();
  }, [Ctor, onResult]);

  return { supported: !!Ctor, listening, start };
}
