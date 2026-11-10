import { useState } from 'react';
import { Button, Card, CardActions, CardContent, Grid2 as Grid, Stack, TextField, Typography } from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { residentApi } from '../../api/endpoints';
import type { Meter } from '../../api/types';
import { PageHeader, QueryView } from '../../components/common';
import { useFeedback } from '../../components/Feedback';
import { fmtDate, resourceLabels, resourceUnits } from '../../i18n/ru';

export function MetersPage() {
  const query = useQuery({ queryKey: ['resident', 'meters'], queryFn: residentApi.meters });
  return (
    <>
      <PageHeader title="Показания счётчиков" subtitle="Передайте текущие показания приборов учёта" />
      <QueryView query={query} empty="Счётчики не найдены">
        {(meters) => (
          <Grid container spacing={2}>
            {meters.map((m) => <Grid key={m.id} size={{ xs: 12, sm: 6, md: 4 }}><MeterCard meter={m} /></Grid>)}
          </Grid>
        )}
      </QueryView>
    </>
  );
}

function MeterCard({ meter }: { meter: Meter }) {
  const [value, setValue] = useState('');
  const feedback = useFeedback();
  const queryClient = useQueryClient();
  const mutation = useMutation({
    mutationFn: () => residentApi.submitReading(meter.id, Number(value.replace(',', '.'))),
    onSuccess: () => {
      setValue('');
      feedback.success('Показание принято');
      queryClient.invalidateQueries({ queryKey: ['resident', 'meters'] });
    },
    onError: feedback.error,
  });
  return (
    <Card variant="outlined">
      <CardContent>
        <Typography fontWeight={600}>{resourceLabels[meter.resource]}</Typography>
        <Typography variant="body2" color="text.secondary">№ {meter.serialNumber} · {meter.address}, кв. {meter.apartmentNumber}</Typography>
        <Typography variant="h5" sx={{ mt: 2 }}>
          {meter.lastValue ?? '—'} <Typography component="span" color="text.secondary">{resourceUnits[meter.resource]}</Typography>
        </Typography>
        <Typography variant="caption" color="text.secondary">последнее от {fmtDate(meter.lastDate)}</Typography>
      </CardContent>
      <CardActions sx={{ px: 2, pb: 2 }}>
        <Stack direction="row" spacing={1} sx={{ width: '100%' }}>
          <TextField size="small" label="Новое показание" value={value} onChange={(e) => setValue(e.target.value)}
                     inputProps={{ inputMode: 'decimal' }} fullWidth />
          <Button variant="contained" disabled={!value || mutation.isPending} onClick={() => mutation.mutate()}>Передать</Button>
        </Stack>
      </CardActions>
    </Card>
  );
}
