import { Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import type { RequestSummary } from '../api/types';
import { fmtDateTime } from '../i18n/ru';
import { StatusChip } from './common';

export function RequestTable({ rows, showExecutor = true }: { rows: RequestSummary[]; showExecutor?: boolean }) {
  const navigate = useNavigate();
  return (
    <TableContainer component={Paper} variant="outlined">
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>№</TableCell>
            <TableCell>Категория и описание</TableCell>
            <TableCell>Адрес</TableCell>
            <TableCell>Статус</TableCell>
            {showExecutor ? <TableCell>Исполнитель</TableCell> : null}
            <TableCell>Создана</TableCell>
            <TableCell>Срок</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((r) => (
            <TableRow key={r.id} hover sx={{ cursor: 'pointer' }} onClick={() => navigate(`/requests/${r.id}`)}>
              <TableCell>{r.id}</TableCell>
              <TableCell sx={{ maxWidth: 360 }}>
                <Typography variant="body2" fontWeight={600}>{r.categoryName}</Typography>
                <Typography variant="body2" color="text.secondary" noWrap>{r.description}</Typography>
              </TableCell>
              <TableCell>{r.address}{r.apartmentNumber ? `, кв. ${r.apartmentNumber}` : ''}</TableCell>
              <TableCell><StatusChip status={r.status} overdue={r.overdue} /></TableCell>
              {showExecutor ? <TableCell>{r.executorName ?? '—'}</TableCell> : null}
              <TableCell>{fmtDateTime(r.createdAt)}</TableCell>
              <TableCell>{fmtDateTime(r.deadlineAt)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
