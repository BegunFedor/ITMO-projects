import { useState, type FormEvent } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { Alert, Button, Link, MenuItem, Stack, TextField, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { authApi, referenceApi } from '../api/endpoints';
import type { OwnershipType } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { errorText, ownershipLabels } from '../i18n/ru';
import { AuthShell } from './LoginPage';

export function RegisterPage() {
  const { setUser } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ login: '', password: '', fullName: '', phone: '', email: '' });
  const [houseId, setHouseId] = useState<number | ''>('');
  const [apartmentId, setApartmentId] = useState<number | ''>('');
  const [ownership, setOwnership] = useState<OwnershipType>('OWNER');
  const [error, setError] = useState<string | null>(null);
  const houses = useQuery({ queryKey: ['houses'], queryFn: referenceApi.houses });
  const apartments = useQuery({
    queryKey: ['apartments', houseId], queryFn: () => referenceApi.apartments(houseId as number), enabled: houseId !== '',
  });
  const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [key]: e.target.value });

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      setUser(await authApi.register({ ...form, apartmentId: apartmentId as number, ownership }));
      navigate('/');
    } catch (err) {
      setError(errorText(err));
    }
  };

  return (
    <AuthShell title="Регистрация жителя">
      <Stack component="form" spacing={2} onSubmit={submit}>
        {error ? <Alert severity="error">{error}</Alert> : null}
        <TextField label="ФИО" value={form.fullName} onChange={set('fullName')} required />
        <TextField label="Логин" value={form.login} onChange={set('login')} required
                   helperText="Латиница, цифры, точка, дефис; от 3 символов" />
        <TextField label="Пароль" type="password" value={form.password} onChange={set('password')} required
                   helperText="Не менее 6 символов" />
        <Stack direction="row" spacing={2}>
          <TextField label="Телефон" value={form.phone} onChange={set('phone')} fullWidth />
          <TextField label="E-mail" value={form.email} onChange={set('email')} fullWidth />
        </Stack>
        <TextField select label="Дом" value={houseId} required
                   onChange={(e) => { setHouseId(Number(e.target.value)); setApartmentId(''); }}>
          {(houses.data ?? []).map((h) => <MenuItem key={h.id} value={h.id}>{h.address}</MenuItem>)}
        </TextField>
        <Stack direction="row" spacing={2}>
          <TextField select label="Квартира" value={apartmentId} required fullWidth disabled={houseId === ''}
                     onChange={(e) => setApartmentId(Number(e.target.value))}>
            {(apartments.data ?? []).map((a) => <MenuItem key={a.id} value={a.id}>кв. {a.number}</MenuItem>)}
          </TextField>
          <TextField select label="Основание" value={ownership} fullWidth
                     onChange={(e) => setOwnership(e.target.value as OwnershipType)}>
            {(Object.keys(ownershipLabels) as OwnershipType[]).map((o) =>
              <MenuItem key={o} value={o}>{ownershipLabels[o]}</MenuItem>)}
          </TextField>
        </Stack>
        <Typography variant="body2" color="text.secondary">
          После регистрации управляющая компания подтвердит, что вы проживаете в квартире.
        </Typography>
        <Button type="submit" variant="contained" size="large">Зарегистрироваться</Button>
        <Typography variant="body2" textAlign="center">
          Уже есть аккаунт? <Link component={RouterLink} to="/login">Войти</Link>
        </Typography>
      </Stack>
    </AuthShell>
  );
}
