import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Button, Checkbox, Chip, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { inspectionApi } from '../../api/endpoints';
import type { OverdueRequest } from '../../api/types';
import { PageHeader, QueryView, StatusChip } from '../../components/common';
import { fmtDateTime } from '../../i18n/ru';
import { IssueOrderDialog, type OrderDraft } from './IssueOrderDialog';

export function OverduePage() {
  const navigate = useNavigate();
  const query = useQuery({ queryKey: ['inspection', 'overdue'], queryFn: inspectionApi.overdue });
  const [selected, setSelected] = useState<OverdueRequest[]>([]);
  const [draft, setDraft] = useState<OrderDraft | null>(null);
  const company = selected[0]?.companyId;
  const toggle = (r: OverdueRequest) => setSelected(selected.some((s) => s.requestId === r.requestId)
    ? selected.filter((s) => s.requestId !== r.requestId) : [...selected, r]);

  return (
    <>
      <PageHeader title="Просроченные заявки" subtitle="Заявки в районе надзора, не выполненные в нормативный срок"
                  action={<Button variant="contained" disabled={selected.length === 0}
                                  onClick={() => setDraft({ companyId: selected[0].companyId, companyName: selected[0].companyName,
                                    requestIds: selected.map((s) => s.requestId), complaintIds: [] })}>
                    Выдать предписание{selected.length ? ` (${selected.length})` : ''}</Button>} />
      <QueryView query={query} empty="Просроченных заявок нет">
        {(rows) => (
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead><TableRow>
                <TableCell padding="checkbox" /><TableCell>№</TableCell><TableCell>УК</TableCell><TableCell>Адрес</TableCell>
                <TableCell>Категория</TableCell><TableCell>Статус</TableCell><TableCell>Срок</TableCell>
                <TableCell align="right">Просрочка</TableCell><TableCell />
              </TableRow></TableHead>
              <TableBody>
                {rows.map((r) => (
                  <TableRow key={r.requestId} hover>
                    <TableCell padding="checkbox">
                      <Checkbox checked={selected.some((s) => s.requestId === r.requestId)}
                                disabled={company !== undefined && company !== r.companyId} onChange={() => toggle(r)} />
                    </TableCell>
                    <TableCell><Button size="small" onClick={() => navigate(`/requests/${r.requestId}`)}>№{r.requestId}</Button></TableCell>
                    <TableCell>{r.companyName}</TableCell>
                    <TableCell>{r.address}</TableCell>
                    <TableCell>{r.categoryName}</TableCell>
                    <TableCell><StatusChip status={r.status} /></TableCell>
                    <TableCell>{fmtDateTime(r.deadlineAt)}</TableCell>
                    <TableCell align="right">{Math.round(Number(r.hoursOverdue))} ч</TableCell>
                    <TableCell>{r.hasComplaint ? <Chip size="small" color="warning" label="жалоба" /> : null}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </QueryView>
      {draft ? <IssueOrderDialog draft={draft} onClose={() => setDraft(null)}
                                 onIssued={() => { setDraft(null); setSelected([]); }} /> : null}
    </>
  );
}
