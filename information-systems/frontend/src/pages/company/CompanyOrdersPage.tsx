import { useState } from 'react';
import { Button, Stack } from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { companyApi } from '../../api/endpoints';
import { PageHeader, QueryView } from '../../components/common';
import { OrderCard } from '../../components/OrderCard';
import { TextDialog } from '../../components/TextDialog';
import { useFeedback } from '../../components/Feedback';

export function CompanyOrdersPage() {
  const feedback = useFeedback();
  const queryClient = useQueryClient();
  const [reportFor, setReportFor] = useState<number | null>(null);
  const query = useQuery({ queryKey: ['company', 'orders'], queryFn: companyApi.orders });
  const mutation = useMutation({
    mutationFn: (report: string) => companyApi.submitReport(reportFor as number, report),
    onSuccess: () => {
      setReportFor(null);
      feedback.success('Отчёт отправлен инспектору');
      queryClient.invalidateQueries({ queryKey: ['company', 'orders'] });
    },
    onError: feedback.error,
  });
  return (
    <>
      <PageHeader title="Предписания ГЖИ" subtitle="Требования жилищной инспекции и отчёты об исполнении" />
      <QueryView query={query} empty="Предписаний нет">
        {(orders) => (
          <Stack spacing={2}>
            {orders.map((o) => (
              <OrderCard key={o.id} order={o} actions={o.status === 'ISSUED' || o.status === 'OVERDUE'
                ? <Button variant="contained" onClick={() => setReportFor(o.id)}>Отчитаться</Button> : undefined} />
            ))}
          </Stack>
        )}
      </QueryView>
      <TextDialog open={reportFor !== null} title="Отчёт об исполнении предписания" label="Какие меры приняты"
                  busy={mutation.isPending} onClose={() => setReportFor(null)} onSubmit={(t) => mutation.mutate(t)} />
    </>
  );
}
