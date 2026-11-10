import { useState } from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField } from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { inspectionApi } from '../../api/endpoints';
import { PageHeader, QueryView } from '../../components/common';
import { OrderCard } from '../../components/OrderCard';
import { useFeedback } from '../../components/Feedback';

export function InspectorOrdersPage() {
  const feedback = useFeedback();
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['inspection', 'orders'], queryFn: inspectionApi.orders });
  const [reviewing, setReviewing] = useState<number | null>(null);
  const [comment, setComment] = useState('');
  const review = useMutation({
    mutationFn: (accepted: boolean) => inspectionApi.review(reviewing as number, accepted, comment.trim()),
    onSuccess: () => {
      setReviewing(null);
      setComment('');
      feedback.success('Решение сохранено');
      queryClient.invalidateQueries({ queryKey: ['inspection'] });
    },
    onError: feedback.error,
  });
  return (
    <>
      <PageHeader title="Выданные предписания" subtitle="Контроль исполнения управляющими компаниями" />
      <QueryView query={query} empty="Предписаний нет">
        {(orders) => (
          <Stack spacing={2}>
            {orders.map((o) => (
              <OrderCard key={o.id} order={o} actions={o.status === 'REPORT_SUBMITTED'
                ? <Button variant="contained" onClick={() => setReviewing(o.id)}>Проверить отчёт</Button> : undefined} />
            ))}
          </Stack>
        )}
      </QueryView>
      <Dialog open={reviewing !== null} onClose={() => setReviewing(null)} fullWidth maxWidth="sm">
        <DialogTitle>Проверка исполнения</DialogTitle>
        <DialogContent>
          <TextField fullWidth multiline minRows={2} label="Комментарий" value={comment}
                     onChange={(e) => setComment(e.target.value)} sx={{ mt: 1 }} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setReviewing(null)}>Отмена</Button>
          <Button color="error" disabled={review.isPending} onClick={() => review.mutate(false)}>Не исполнено</Button>
          <Button variant="contained" color="success" disabled={review.isPending} onClick={() => review.mutate(true)}>Исполнено</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
