import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box, Button, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, Grid2 as Grid, List,
  ListItemButton, ListItemText, Rating, Stack, TextField, Typography, Chip, Divider, Alert,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { dispatchApi, requestApi, residentApi, workApi } from '../api/endpoints';
import type { RequestAction, RequestCard } from '../api/types';
import { useCurrentUser } from '../auth/AuthContext';
import { PageHeader, QueryView, StatusChip } from '../components/common';
import { TextDialog } from '../components/TextDialog';
import { useFeedback } from '../components/Feedback';
import { actionLabels, fmtDateTime, roleLabels, statusLabels } from '../i18n/ru';

type DialogKind = 'CANCEL' | 'REJECT_RESULT' | 'COMPLAIN' | 'COMPLETE' | 'RATE' | 'ASSIGN' | null;

export function RequestCardPage() {
  const id = Number(useParams().id);
  const query = useQuery({ queryKey: ['request', id], queryFn: () => requestApi.card(id) });
  return (
    <QueryView query={query}>
      {(card) => <CardView card={card} />}
    </QueryView>
  );
}

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary">{label}</Typography>
      <Typography component="div">{value}</Typography>
    </Box>
  );
}

function CardView({ card }: { card: RequestCard }) {
  const r = card.request;
  const user = useCurrentUser();
  const navigate = useNavigate();
  const feedback = useFeedback();
  const queryClient = useQueryClient();
  const [dialog, setDialog] = useState<DialogKind>(null);
  const [comment, setComment] = useState('');
  const can = (a: RequestAction) => card.actions.includes(a);

  const mutation = useMutation({
    mutationFn: (fn: () => Promise<unknown>) => fn(),
    onSuccess: () => {
      setDialog(null);
      feedback.success('Готово');
      queryClient.invalidateQueries();
    },
    onError: feedback.error,
  });
  const run = (fn: () => Promise<unknown>) => mutation.mutate(fn);

  const cancel = (reason: string) => run(() => user.role === 'RESIDENT'
    ? residentApi.cancel(r.id, reason) : dispatchApi.cancel(r.id, reason));

  const buttons: { action: RequestAction; onClick: () => void; color?: 'error' | 'primary' | 'warning' }[] = [
    { action: 'ACCEPT', onClick: () => run(() => dispatchApi.accept(r.id)) },
    { action: 'ASSIGN', onClick: () => setDialog('ASSIGN') },
    { action: 'START', onClick: () => run(() => workApi.start(r.id)) },
    { action: 'COMPLETE', onClick: () => setDialog('COMPLETE') },
    { action: 'RATE', onClick: () => setDialog('RATE') },
    { action: 'REJECT_RESULT', onClick: () => setDialog('REJECT_RESULT'), color: 'warning' },
    { action: 'CREATE_REPEAT', onClick: () => navigate(`/my/requests/new?repeat=${r.id}`) },
    { action: 'COMPLAIN', onClick: () => setDialog('COMPLAIN'), color: 'warning' },
    { action: 'CANCEL', onClick: () => setDialog('CANCEL'), color: 'error' },
  ];
  const visible = buttons.filter((b) => can(b.action));

  return (
    <>
      <PageHeader title={`Заявка №${r.id}`} subtitle={`${r.categoryName} · ${r.companyName}`}
                  action={<StatusChip status={r.status} overdue={r.overdue} />} />
      {visible.length > 0 ? (
        <Stack direction="row" spacing={1} sx={{ mb: 3, flexWrap: 'wrap', gap: 1 }}>
          {visible.map((b, i) => (
            <Button key={b.action} variant={i === 0 ? 'contained' : 'outlined'} color={b.color ?? 'primary'}
                    onClick={b.onClick} disabled={mutation.isPending}>{actionLabels[b.action]}</Button>
          ))}
        </Stack>
      ) : null}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 7 }}>
          <Card variant="outlined">
            <CardContent>
              <Stack spacing={2}>
                <Field label="Описание" value={r.description} />
                <Grid container spacing={2}>
                  <Grid size={6}><Field label="Адрес" value={r.address + (r.apartmentNumber ? `, кв. ${r.apartmentNumber}` : ' (общедомовая)')} /></Grid>
                  <Grid size={6}><Field label="Заявитель" value={`${r.authorName}${r.authorPhone ? ', ' + r.authorPhone : ''}`} /></Grid>
                  <Grid size={6}><Field label="Создана" value={fmtDateTime(r.createdAt)} /></Grid>
                  <Grid size={6}><Field label="Нормативный срок" value={fmtDateTime(r.deadlineAt)} /></Grid>
                  <Grid size={6}><Field label="Исполнитель" value={r.executorName ?? 'не назначен'} /></Grid>
                  <Grid size={6}><Field label="Статус установлен" value={fmtDateTime(r.statusChangedAt)} /></Grid>
                </Grid>
                {r.previousRequestId ? (
                  <Field label="Повторная по заявке" value={
                    <Button size="small" onClick={() => navigate(`/requests/${r.previousRequestId}`)}>№{r.previousRequestId}</Button>} />
                ) : null}
                {r.resultText ? <Alert severity="success" icon={false}><b>Результат работ:</b> {r.resultText}</Alert> : null}
                {card.rating ? (
                  <Stack direction="row" spacing={1} alignItems="center">
                    <Rating value={card.rating.score} readOnly />
                    <Typography color="text.secondary">{card.rating.review}</Typography>
                  </Stack>
                ) : null}
                {card.hasComplaint ? <Chip label="По заявке подана жалоба в ГЖИ" color="warning" variant="outlined" /> : null}
              </Stack>
            </CardContent>
          </Card>
          <Card variant="outlined" sx={{ mt: 3 }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Комментарии</Typography>
              {card.comments.length === 0 ? <Typography color="text.secondary">Комментариев пока нет</Typography> : null}
              <Stack spacing={2} divider={<Divider />}>
                {card.comments.map((c) => (
                  <Box key={c.id}>
                    <Typography variant="body2" fontWeight={600}>{c.authorName}
                      <Typography component="span" variant="body2" color="text.secondary"> · {roleLabels[c.authorRole]} · {fmtDateTime(c.createdAt)}</Typography>
                    </Typography>
                    <Typography>{c.body}</Typography>
                  </Box>
                ))}
              </Stack>
              {can('COMMENT') ? (
                <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
                  <TextField size="small" fullWidth placeholder="Написать комментарий" value={comment}
                             onChange={(e) => setComment(e.target.value)} />
                  <Button variant="contained" disabled={!comment.trim() || mutation.isPending}
                          onClick={() => run(async () => { await requestApi.comment(r.id, comment.trim()); setComment(''); })}>
                    Отправить
                  </Button>
                </Stack>
              ) : null}
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 5 }}>
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>История</Typography>
              <Stack spacing={2}>
                {card.history.map((h, i) => (
                  <Box key={i} sx={{ borderLeft: 3, borderColor: 'primary.light', pl: 2 }}>
                    <Typography variant="body2" fontWeight={600}>
                      {statusLabels[h.status]}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {fmtDateTime(h.changedAt)}{h.changedByName ? ` · ${h.changedByName}` : ' · система'}
                    </Typography>
                    {h.comment ? <Typography variant="body2">{h.comment}</Typography> : null}
                  </Box>
                ))}
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <TextDialog open={dialog === 'CANCEL'} title="Отмена заявки" label="Причина отмены" submitLabel="Отменить заявку"
                  busy={mutation.isPending} onClose={() => setDialog(null)} onSubmit={cancel} />
      <TextDialog open={dialog === 'REJECT_RESULT'} title="Результат не устраивает" label="Что не так?"
                  hint="Заявка будет отменена. После этого можно подать повторную заявку или жалобу в жилищную инспекцию."
                  busy={mutation.isPending} onClose={() => setDialog(null)}
                  onSubmit={(t) => run(() => residentApi.rejectResult(r.id, t))} />
      <TextDialog open={dialog === 'COMPLAIN'} title="Жалоба в жилищную инспекцию" label="Суть жалобы" minLength={10}
                  busy={mutation.isPending} onClose={() => setDialog(null)}
                  onSubmit={(t) => run(() => residentApi.fileComplaint(r.id, t))} />
      <TextDialog open={dialog === 'COMPLETE'} title="Завершение работ" label="Что сделано" submitLabel="Завершить"
                  busy={mutation.isPending} onClose={() => setDialog(null)}
                  onSubmit={(t) => run(() => workApi.complete(r.id, t))} />
      <RateDialog open={dialog === 'RATE'} busy={mutation.isPending} onClose={() => setDialog(null)}
                  onSubmit={(score, review) => run(() => residentApi.rate(r.id, score, review))} />
      {dialog === 'ASSIGN' ? (
        <AssignDialog requestId={r.id} busy={mutation.isPending} onClose={() => setDialog(null)}
                      onSubmit={(executorId, c) => run(() => dispatchApi.assign(r.id, executorId, c))} />
      ) : null}
    </>
  );
}

function RateDialog({ open, busy, onClose, onSubmit }: {
  open: boolean; busy: boolean; onClose: () => void; onSubmit: (score: number, review: string) => void;
}) {
  const [score, setScore] = useState<number | null>(5);
  const [review, setReview] = useState('');
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>Оценка выполнения</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          <Rating size="large" value={score} onChange={(_, v) => setScore(v)} />
          <TextField label="Отзыв (необязательно)" multiline minRows={2} value={review}
                     onChange={(e) => setReview(e.target.value)} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" disabled={!score || busy} onClick={() => onSubmit(score as number, review.trim())}>
          Закрыть заявку
        </Button>
      </DialogActions>
    </Dialog>
  );
}

function AssignDialog({ requestId, busy, onClose, onSubmit }: {
  requestId: number; busy: boolean; onClose: () => void; onSubmit: (executorId: number, comment: string) => void;
}) {
  const executors = useQuery({ queryKey: ['executors', requestId], queryFn: () => dispatchApi.executors(requestId) });
  const [selected, setSelected] = useState<number | null>(null);
  const [comment, setComment] = useState('');
  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Назначение исполнителя</DialogTitle>
      <DialogContent>
        <QueryView query={executors} empty="В компании нет сотрудников">
          {(list) => (
            <List dense>
              {list.map((e) => (
                <ListItemButton key={e.id} selected={selected === e.id} disabled={!e.matchesSpecialization}
                                onClick={() => setSelected(e.id)}>
                  <ListItemText primary={e.fullName}
                                secondary={`${e.matchesSpecialization ? 'подходит по специализации' : 'нет нужной специализации'} · в работе: ${e.activeRequests}`} />
                </ListItemButton>
              ))}
            </List>
          )}
        </QueryView>
        <TextField fullWidth label="Комментарий исполнителю" value={comment} onChange={(e) => setComment(e.target.value)} sx={{ mt: 2 }} />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" disabled={selected === null || busy}
                onClick={() => onSubmit(selected as number, comment.trim())}>Назначить</Button>
      </DialogActions>
    </Dialog>
  );
}
