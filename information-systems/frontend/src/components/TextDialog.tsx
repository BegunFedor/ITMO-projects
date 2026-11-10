import { useEffect, useState } from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from '@mui/material';

interface Props {
  open: boolean;
  title: string;
  label: string;
  hint?: string;
  submitLabel?: string;
  minLength?: number;
  busy?: boolean;
  onClose: () => void;
  onSubmit: (text: string) => void;
}

export function TextDialog({ open, title, label, hint, submitLabel = 'Отправить', minLength = 1, busy, onClose, onSubmit }: Props) {
  const [text, setText] = useState('');
  useEffect(() => { if (open) setText(''); }, [open]);
  const valid = text.trim().length >= minLength;
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        {hint ? <DialogContentText sx={{ mb: 2 }}>{hint}</DialogContentText> : null}
        <TextField autoFocus fullWidth multiline minRows={3} label={label} value={text}
                   onChange={(e) => setText(e.target.value)}
                   helperText={minLength > 1 ? `Не менее ${minLength} символов` : undefined} />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" disabled={!valid || busy} onClick={() => onSubmit(text.trim())}>{submitLabel}</Button>
      </DialogActions>
    </Dialog>
  );
}
