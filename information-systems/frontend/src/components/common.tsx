import type { ReactNode } from 'react';
import { Alert, Box, Chip, CircularProgress, Paper, Stack, Typography } from '@mui/material';
import type { UseQueryResult } from '@tanstack/react-query';
import type { RequestStatus } from '../api/types';
import { errorText, statusColors, statusLabels } from '../i18n/ru';

export function StatusChip({ status, overdue }: { status: RequestStatus; overdue?: boolean }) {
  return (
    <Stack direction="row" spacing={0.5}>
      <Chip size="small" label={statusLabels[status]} color={statusColors[status]}
            variant={status === 'CLOSED' || status === 'CANCELLED' ? 'outlined' : 'filled'} />
      {overdue ? <Chip size="small" label="Просрочена" color="error" variant="outlined" /> : null}
    </Stack>
  );
}

export function PageHeader({ title, subtitle, action }: { title: string; subtitle?: string; action?: ReactNode }) {
  return (
    <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between"
           alignItems={{ xs: 'flex-start', sm: 'center' }} spacing={2} sx={{ mb: 3 }}>
      <Box>
        <Typography variant="h5" component="h1" fontWeight={600}>{title}</Typography>
        {subtitle ? <Typography color="text.secondary">{subtitle}</Typography> : null}
      </Box>
      {action}
    </Stack>
  );
}

export function QueryView<T>({ query, empty, children }: {
  query: UseQueryResult<T>; empty?: string; children: (data: T) => ReactNode;
}) {
  if (query.isPending) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}><CircularProgress /></Box>;
  }
  if (query.isError) return <Alert severity="error">{errorText(query.error)}</Alert>;
  const data = query.data as T;
  if (empty && Array.isArray(data) && data.length === 0) {
    return <Paper variant="outlined" sx={{ p: 4, textAlign: 'center', color: 'text.secondary' }}>{empty}</Paper>;
  }
  return <>{children(data)}</>;
}
