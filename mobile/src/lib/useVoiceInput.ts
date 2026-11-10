/**
 * Native builds do not request RECORD_AUDIO, so the mic button focuses the input and the user
 * can use the keyboard's own dictation key. The web build uses `useVoiceInput.web.ts`.
 */
export function useVoiceInput(_onResult: (text: string) => void) {
  return { supported: false, listening: false, start: () => {} };
}
