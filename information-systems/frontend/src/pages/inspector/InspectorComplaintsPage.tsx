import { useState } from 'react';
import { Button, Checkbox, Stack } from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { inspectionApi } from '../../api/endpoints';
import type { Complaint } from '../../api/types';
import { PageHeader, QueryView } from '../../components/common';
import { ComplaintTable } from '../../components/ComplaintTable';
import { TextDialog } from '../../components/TextDialog';
import { useFeedback } from '../../components/Feedback';
import { IssueOrderDialog, type OrderDraft } from './IssueOrderDialog';

const open = (c: Complaint) => c.status === 'NEW' || c.status === 'UNDER_REVIEW';

export function InspectorComplaintsPage() {
  const feedback = useFeedback();
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['inspection', 'complaints'], queryFn: inspectionApi.complaints });
  const [selected, setSelected] = useState<Complaint[]>([]);
  const [draft, setDraft] = useState<OrderDraft | null>(null);
  const [rejecting, setRejecting] = useState<number | null>(null);
  const company = selected[0]?.companyId;
  const toggle = (c: Complaint) => setSelected(selected.some((s) => s.id === c.id)
    ? selected.filter((s) => s.id !== c.id) : [...selected, c]);
  const reject = useMutation({
    mutationFn: (reason: string) => inspectionApi.rejectComplaint(rejecting as number, reason),
    onSuccess: () => {
      setRejecting(null);
      feedback.success('Жалоба отклонена');
      queryClient.invalidateQueries({ queryKey: ['inspection'] });
    },
    onError: feedback.error,
  });

  return (
    <>
      <PageHeader title="Жалобы жителей" subtitle="Рассмотрение жалоб на управляющие компании района"
                  action={<Button variant="contained" disabled={selected.length === 0}
                                  onClick={() => setDraft({ companyId: selected[0].companyId, companyName: selected[0].companyName,
                                    requestIds: [...new Set(selected.map((s) => s.requestId))], complaintIds: selected.map((s) => s.id) })}>
                    Выдать предписание{selected.length ? ` (${selected.length})` : ''}</Button>} />
      <QueryView query={query} empty="Жалоб нет">
        {(rows) => (
          <ComplaintTable rows={rows} showResident
                          leading={(c) => open(c) ? (
                            <Checkbox checked={selected.some((s) => s.id === c.id)}
                                      disabled={company !== undefined && company !== c.companyId} onChange={() => toggle(c)} />) : null}
                          trailing={(c) => open(c) ? (
                            <Stack direction="row"><Button size="small" color="inherit" onClick={() => setRejecting(c.id)}>Отклонить</Button></Stack>) : null} />
        )}
      </QueryView>
      <TextDialog open={rejecting !== null} title="Отклонение жалобы" label="Обоснование" submitLabel="Отклонить"
                  busy={reject.isPending} onClose={() => setRejecting(null)} onSubmit={(t) => reject.mutate(t)} />
      {draft ? <IssueOrderDialog draft={draft} onClose={() => setDraft(null)}
                                 onIssued={() => { setDraft(null); setSelected([]); }} /> : null}
    </>
  );
}
