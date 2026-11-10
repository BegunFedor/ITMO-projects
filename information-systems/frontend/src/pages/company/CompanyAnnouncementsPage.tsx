import { useState } from 'react';
import {
  Button, Card, CardContent, Checkbox, Chip, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel,
  FormGroup, FormLabel, Stack, TextField, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { companyApi, referenceApi } from '../../api/endpoints';
import { PageHeader, QueryView } from '../../components/common';
import { useFeedback } from '../../components/Feedback';
import { fmtDateTime } from '../../i18n/ru';

export function CompanyAnnouncementsPage() {
  const [open, setOpen] = useState(false);
  const query = useQuery({ queryKey: ['company', 'announcements'], queryFn: companyApi.announcements });
  return (
    <>
      <PageHeader title="Объявления" subtitle="Публикация сообщений для жителей домов"
                  action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>Опубликовать</Button>} />
      <QueryView query={query} empty="Объявлений пока нет">
        {(list) => (
          <Stack spacing={2}>
            {list.map((a) => (
              <Card key={a.id} variant="outlined">
                <CardContent>
                  <Typography variant="h6">{a.title}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {fmtDateTime(a.visibleFrom)}{a.visibleTo ? ` — ${fmtDateTime(a.visibleTo)}` : ' — бессрочно'}
                  </Typography>
                  <Typography sx={{ my: 1, whiteSpace: 'pre-line' }}>{a.body}</Typography>
                  <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
                    {(a.houses ?? []).map((h) => <Chip key={h.id} size="small" label={h.address} />)}
                  </Stack>
                </CardContent>
              </Card>
            ))}
          </Stack>
        )}
      </QueryView>
      {open ? <PublishDialog onClose={() => setOpen(false)} /> : null}
    </>
  );
}

function PublishDialog({ onClose }: { onClose: () => void }) {
  const feedback = useFeedback();
  const queryClient = useQueryClient();
  const houses = useQuery({ queryKey: ['company-houses'], queryFn: referenceApi.companyHouses });
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [visibleTo, setVisibleTo] = useState('');
  const [selected, setSelected] = useState<number[]>([]);
  const toggle = (id: number) => setSelected(selected.includes(id) ? selected.filter((x) => x !== id) : [...selected, id]);
  const mutation = useMutation({
    mutationFn: () => companyApi.publish({
      title: title.trim(), body: body.trim(), houseIds: selected, visibleFrom: null,
      visibleTo: visibleTo ? new Date(visibleTo).toISOString() : null,
    }),
    onSuccess: () => {
      feedback.success('Объявление опубликовано');
      queryClient.invalidateQueries({ queryKey: ['company', 'announcements'] });
      onClose();
    },
    onError: feedback.error,
  });
  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Новое объявление</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          <TextField label="Заголовок" value={title} onChange={(e) => setTitle(e.target.value)} />
          <TextField label="Текст" multiline minRows={4} value={body} onChange={(e) => setBody(e.target.value)} />
          <TextField label="Показывать до" type="datetime-local" value={visibleTo}
                     onChange={(e) => setVisibleTo(e.target.value)} InputLabelProps={{ shrink: true }} />
          <FormLabel>Дома</FormLabel>
          <FormGroup>
            {(houses.data ?? []).map((h) => (
              <FormControlLabel key={h.id} label={h.address}
                                control={<Checkbox checked={selected.includes(h.id)} onChange={() => toggle(h.id)} />} />
            ))}
          </FormGroup>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" disabled={!title.trim() || !body.trim() || selected.length === 0 || mutation.isPending}
                onClick={() => mutation.mutate()}>Опубликовать</Button>
      </DialogActions>
    </Dialog>
  );
}
