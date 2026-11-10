import type { ReactNode } from 'react';
import { Box, Card, CardActions, CardContent, Chip, Stack, Typography } from '@mui/material';
import type { Order } from '../api/types';
import { fmtDate, fmtDateTime, orderStatusLabels } from '../i18n/ru';

const colors = {
  ISSUED: 'info', REPORT_SUBMITTED: 'warning', FULFILLED: 'success', NOT_FULFILLED: 'error', OVERDUE: 'error',
} as const;

export function OrderCard({ order, actions }: { order: Order; actions?: ReactNode }) {
  return (
    <Card variant="outlined">
      <CardContent>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-start" spacing={2}>
          <Box>
            <Typography variant="h6">Предписание {order.number}</Typography>
            <Typography variant="body2" color="text.secondary">
              {order.companyName} · выдано {fmtDateTime(order.issuedAt)} · срок до {fmtDate(order.dueDate)}
            </Typography>
          </Box>
          <Chip label={orderStatusLabels[order.status]} color={colors[order.status]} />
        </Stack>
        <Typography sx={{ mt: 1 }}>{order.violation}</Typography>
        {order.requestIds.length > 0 ? (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Заявки: {order.requestIds.map((id) => `№${id}`).join(', ')}
          </Typography>
        ) : null}
        {order.reportText ? (
          <Box sx={{ mt: 2, p: 1.5, bgcolor: 'action.hover', borderRadius: 1 }}>
            <Typography variant="caption" color="text.secondary">Отчёт УК от {fmtDateTime(order.reportAt)}</Typography>
            <Typography>{order.reportText}</Typography>
          </Box>
        ) : null}
        {order.reviewedAt ? (
          <Typography variant="body2" sx={{ mt: 1 }}>
            Проверено {fmtDateTime(order.reviewedAt)}{order.reviewComment ? `: ${order.reviewComment}` : ''}
          </Typography>
        ) : null}
      </CardContent>
      {actions ? <CardActions sx={{ px: 2, pb: 2 }}>{actions}</CardActions> : null}
    </Card>
  );
}
