import { Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow } from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { companyApi } from '../../api/endpoints';
import { PageHeader, QueryView } from '../../components/common';
import { useFeedback } from '../../components/Feedback';
import { fmtDateTime, ownershipLabels } from '../../i18n/ru';

export function ResidentsPage() {
  const feedback = useFeedback();
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['company', 'pending'], queryFn: companyApi.pendingResidents });
  const confirm = useMutation({
    mutationFn: (v: { userId: number; apartmentId: number }) => companyApi.confirmResident(v.userId, v.apartmentId),
    onSuccess: () => { feedback.success('Житель подтверждён'); queryClient.invalidateQueries({ queryKey: ['company', 'pending'] }); },
    onError: feedback.error,
  });
  return (
    <>
      <PageHeader title="Подтверждение жителей" subtitle="Новые пользователи, указавшие квартиры в ваших домах" />
      <QueryView query={query} empty="Нет заявок на подтверждение">
        {(rows) => (
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead><TableRow>
                <TableCell>ФИО</TableCell><TableCell>Телефон</TableCell><TableCell>Квартира</TableCell>
                <TableCell>Основание</TableCell><TableCell>Дата</TableCell><TableCell />
              </TableRow></TableHead>
              <TableBody>
                {rows.map((r) => (
                  <TableRow key={`${r.userId}-${r.apartmentId}`}>
                    <TableCell>{r.fullName}</TableCell>
                    <TableCell>{r.phone ?? '—'}</TableCell>
                    <TableCell>{r.address}, кв. {r.apartmentNumber}</TableCell>
                    <TableCell>{ownershipLabels[r.ownership]}</TableCell>
                    <TableCell>{fmtDateTime(r.linkedAt)}</TableCell>
                    <TableCell align="right">
                      <Button variant="contained" size="small" disabled={confirm.isPending}
                              onClick={() => confirm.mutate({ userId: r.userId, apartmentId: r.apartmentId })}>Подтвердить</Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </QueryView>
    </>
  );
}
