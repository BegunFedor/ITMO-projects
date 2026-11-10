import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';
import { Alert, Snackbar } from '@mui/material';
import { errorText } from '../i18n/ru';

interface Message { text: string; severity: 'success' | 'error' }
interface FeedbackApi { success: (text: string) => void; error: (e: unknown) => void }

const FeedbackContext = createContext<FeedbackApi | null>(null);

export function FeedbackProvider({ children }: { children: ReactNode }) {
  const [message, setMessage] = useState<Message | null>(null);
  const success = useCallback((text: string) => setMessage({ text, severity: 'success' }), []);
  const error = useCallback((e: unknown) => setMessage({ text: errorText(e), severity: 'error' }), []);
  return (
    <FeedbackContext.Provider value={{ success, error }}>
      {children}
      <Snackbar open={message !== null} autoHideDuration={5000} onClose={() => setMessage(null)}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        {message ? <Alert severity={message.severity} variant="filled" onClose={() => setMessage(null)}>
          {message.text}</Alert> : undefined}
      </Snackbar>
    </FeedbackContext.Provider>
  );
}

export function useFeedback(): FeedbackApi {
  const ctx = useContext(FeedbackContext);
  if (!ctx) throw new Error('FeedbackProvider missing');
  return ctx;
}
