import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Alert, Button, Card, CardContent, FormControlLabel, MenuItem, Stack, Switch, TextField } from '@mui/material';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { referenceApi, requestApi, residentApi } from '../../api/endpoints';
import { PageHeader } from '../../components/common';
import { useFeedback } from '../../components/Feedback';

export function NewRequestPage() {
  const navigate = useNavigate();
  const feedback = useFeedback();
  const queryClient = useQueryClient();
  const repeatId = Number(useSearchParams()[0].get('repeat')) || null;
  const apartments = useQuery({ queryKey: ['my-apartments'], queryFn: referenceApi.myApartments });
  const categories = useQuery({ queryKey: ['categories'], queryFn: referenceApi.categories });
  const previous = useQuery({ queryKey: ['request', repeatId], queryFn: () => requestApi.card(repeatId as number), enabled: repeatId !== null });

  const confirmed = useMemo(() => (apartments.data ?? []).filter((a) => a.confirmed), [apartments.data]);
  const [apartmentId, setApartmentId] = useState<number | ''>('');
  const [common, setCommon] = useState(false);
  const [categoryId, setCategoryId] = useState<number | ''>('');
  const [description, setDescription] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (apartmentId === '' && confirmed.length > 0) setApartmentId(confirmed[0].apartmentId);
  }, [confirmed, apartmentId]);

  useEffect(() => {
    const p = previous.data?.request;
    if (!p) return;
    setCategoryId(p.categoryId);
    setDescription(p.description);
    setCommon(p.apartmentId === null);
    const own = confirmed.find((a) => a.houseId === p.houseId && (p.apartmentId === null || a.apartmentId === p.apartmentId));
    if (own) setApartmentId(own.apartmentId);
  }, [previous.data, confirmed]);

  const selected = confirmed.find((a) => a.apartmentId === apartmentId);
  const category = categories.data?.find((c) => c.id === categoryId);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (!selected || categoryId === '') return;
    setBusy(true);
    try {
      const created = await residentApi.create({
        houseId: selected.houseId, apartmentId: common ? null : selected.apartmentId,
        categoryId, description: description.trim(), previousRequestId: repeatId,
      });
      queryClient.invalidateQueries({ queryKey: ['resident'] });
      feedback.success(`Заявка №${created.id} создана`);
      navigate(`/requests/${created.id}`);
    } catch (err) {
      feedback.error(err);
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <PageHeader title={repeatId ? `Повторная заявка по №${repeatId}` : 'Новая заявка'} />
      <Card variant="outlined" sx={{ maxWidth: 720 }}>
        <CardContent>
          {apartments.isSuccess && confirmed.length === 0 ? (
            <Alert severity="info">Управляющая компания ещё не подтвердила вашу квартиру. Подать заявку можно после подтверждения.</Alert>
          ) : (
            <Stack component="form" spacing={2} onSubmit={submit}>
              <TextField select label="Квартира" value={apartmentId} onChange={(e) => setApartmentId(Number(e.target.value))} required>
                {confirmed.map((a) => <MenuItem key={a.apartmentId} value={a.apartmentId}>{a.address}, кв. {a.number}</MenuItem>)}
              </TextField>
              <FormControlLabel control={<Switch checked={common} onChange={(e) => setCommon(e.target.checked)} />}
                                label="Проблема в общедомовом имуществе (подъезд, лифт, кровля…)" />
              <TextField select label="Категория" value={categoryId} onChange={(e) => setCategoryId(Number(e.target.value))} required
                         helperText={category ? `Нормативный срок: ${category.normHours} ч${category.emergency ? ' · аварийная' : ''}` : ' '}>
                {(categories.data ?? []).map((c) => <MenuItem key={c.id} value={c.id}>{c.name}{c.emergency ? ' (аварийная)' : ''}</MenuItem>)}
              </TextField>
              <TextField label="Описание проблемы" multiline minRows={4} value={description} required
                         onChange={(e) => setDescription(e.target.value)} helperText="Не менее 10 символов" />
              <Stack direction="row" spacing={1}>
                <Button type="submit" variant="contained" disabled={busy || description.trim().length < 10}>Отправить</Button>
                <Button onClick={() => navigate(-1)}>Отмена</Button>
              </Stack>
            </Stack>
          )}
        </CardContent>
      </Card>
    </>
  );
}
