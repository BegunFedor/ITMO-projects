import { useState } from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Stack, TextField } from '@mui/material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { inspectionApi } from '../../api/endpoints';
import { useFeedback } from '../../components/Feedback';

export interface OrderDraft { companyId: number; companyName: string; requestIds: number[]; complaintIds: number[] }

function defaultDueDate(): string {
  const d = new Date();
  d.setDate(d.getDate() + 14);
  return d.toISOString().slice(0, 10);
}

export function IssueOrderDialog({ draft, onClose, onIssued }: { draft: OrderDraft; onClose: () => void; onIssued: () => void }) {
  const feedback = useFeedback();
  const queryClient = useQueryClient();
  const [violation, setViolation] = useState('');
  const [dueDate, setDueDate] = useState(defaultDueDate());
  const mutation = useMutation({
    mutationFn: () => inspectionApi.issue({
      companyId: draft.companyId, violation: violation.trim(), dueDate,
      requestIds: draft.requestIds, complaintIds: draft.complaintIds,
    }),
    onSuccess: () => {
      feedback.success('Предписание выдано');
      queryClient.invalidateQueries({ queryKey: ['inspection'] });
      onIssued();
    },
    onError: feedback.error,
  });
  const parts = [
    draft.requestIds.length ? `заявки ${draft.requestIds.map((i) => '№' + i).join(', ')}` : '',
    draft.complaintIds.length ? `жалобы ${draft.complaintIds.map((i) => '№' + i).join(', ')}` : '',
  ].filter(Boolean).join('; ');
  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Предписание для {draft.companyName}</DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>Основание: {parts}</DialogContentText>
        <Stack spacing={2}>
          <TextField label="Выявленное нарушение" multiline minRows={3} value={violation} onChange={(e) => setViolation(e.target.value)} />
          <TextField label="Срок исполнения" type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)}
                     InputLabelProps={{ shrink: true }} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" disabled={!violation.trim() || !dueDate || mutation.isPending} onClick={() => mutation.mutate()}>
          Выдать
        </Button>
      </DialogActions>
    </Dialog>
  );
}
