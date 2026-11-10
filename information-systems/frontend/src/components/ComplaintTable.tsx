import type { ReactNode } from 'react';
import { Button, Chip, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import type { Complaint } from '../api/types';
import { complaintStatusLabels, fmtDateTime } from '../i18n/ru';

export function ComplaintTable({ rows, showResident, leading, trailing }: {
  rows: Complaint[]; showResident?: boolean;
  leading?: (c: Complaint) => ReactNode; trailing?: (c: Complaint) => ReactNode;
}) {
  const navigate = useNavigate();
  return (
    <TableContainer component={Paper} variant="outlined">
      <Table size="small">
        <TableHead>
          <TableRow>
            {leading ? <TableCell padding="checkbox" /> : null}
            <TableCell>№</TableCell>
            <TableCell>Заявка</TableCell>
            {showResident ? <TableCell>Житель</TableCell> : null}
            <TableCell>УК</TableCell>
            <TableCell>Суть</TableCell>
            <TableCell>Статус</TableCell>
            <TableCell>Подана</TableCell>
            {trailing ? <TableCell /> : null}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((c) => (
            <TableRow key={c.id}>
              {leading ? <TableCell padding="checkbox">{leading(c)}</TableCell> : null}
              <TableCell>{c.id}</TableCell>
              <TableCell>
                <Button size="small" onClick={() => navigate(`/requests/${c.requestId}`)}>№{c.requestId}</Button>
                <Typography variant="caption" display="block" color="text.secondary">{c.categoryName}, {c.address}</Typography>
              </TableCell>
              {showResident ? <TableCell>{c.residentName}</TableCell> : null}
              <TableCell>{c.companyName}</TableCell>
              <TableCell sx={{ maxWidth: 300 }}>
                {c.body}
                {c.decisionComment ? <Typography variant="caption" display="block" color="text.secondary">Решение: {c.decisionComment}</Typography> : null}
              </TableCell>
              <TableCell><Chip size="small" label={complaintStatusLabels[c.status]}
                               color={c.status === 'ORDER_ISSUED' ? 'success' : c.status === 'REJECTED' ? 'default' : 'warning'} /></TableCell>
              <TableCell>{fmtDateTime(c.createdAt)}</TableCell>
              {trailing ? <TableCell>{trailing(c)}</TableCell> : null}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
