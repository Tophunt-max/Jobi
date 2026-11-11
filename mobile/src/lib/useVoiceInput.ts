import { ExpoSpeechRecognitionModule, useSpeechRecognitionEvent } from 'expo-speech-recognition';
import { useCallback, useState } from 'react';
import { Alert } from 'react-native';

import { i18n } from '@/i18n';

/** Speech locale for the app language, e.g. "hi" -> "hi-IN". */
export function speechLocale(language: string): string {
  return language === 'en' ? 'en-IN' : `${language}-IN`;
}

function isAvailable(): boolean {
  try {
    return ExpoSpeechRecognitionModule.isRecognitionAvailable();
  } catch {
    // The native module is missing in builds made before it was added.
    return false;
  }
}

/**
 * On-device speech to text (Android SpeechRecognizer, iOS SFSpeechRecognizer).
 * Asks for microphone and speech permission on first use. The web build uses `useVoiceInput.web.ts`.
 */
export function useVoiceInput(onResult: (text: string) => void) {
  const [listening, setListening] = useState(false);
  const [supported] = useState(isAvailable);

  useSpeechRecognitionEvent('start', () => setListening(true));
  useSpeechRecognitionEvent('end', () => setListening(false));
  useSpeechRecognitionEvent('result', (event) => {
    if (!event.isFinal) return;
    const text = event.results[0]?.transcript?.trim();
    if (text) onResult(text);
  });
  useSpeechRecognitionEvent('error', (event) => {
    setListening(false);
    if (event.error === 'not-allowed') Alert.alert(i18n.t('voice.deniedTitle'), i18n.t('voice.deniedBody'));
    else if (event.error === 'language-not-supported' || event.error === 'service-not-allowed') Alert.alert(i18n.t('voice.unavailable'));
  });

  const start = useCallback(async () => {
    if (listening) {
      ExpoSpeechRecognitionModule.stop();
      return;
    }
    const permission = await ExpoSpeechRecognitionModule.requestPermissionsAsync();
    if (!permission.granted) {
      Alert.alert(i18n.t('voice.deniedTitle'), i18n.t('voice.deniedBody'));
      return;
    }
    ExpoSpeechRecognitionModule.start({ lang: speechLocale(i18n.language), interimResults: false, continuous: false });
  }, [listening]);

  return { supported, listening, start: () => void start() };
}
