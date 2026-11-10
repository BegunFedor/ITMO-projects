import { useState, type FormEvent } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { Alert, Box, Button, Link, Paper, Stack, TextField, Typography } from '@mui/material';
import ApartmentIcon from '@mui/icons-material/ApartmentOutlined';
import { authApi } from '../api/endpoints';
import { useAuth } from '../auth/AuthContext';
import { errorText } from '../i18n/ru';

export function AuthShell({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 2,
               bgcolor: 'background.default' }}>
      <Paper variant="outlined" sx={{ p: 4, width: '100%', maxWidth: 440 }}>
        <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1, color: 'primary.main' }}>
          <ApartmentIcon /><Typography fontWeight={700}>МКД·Обращения</Typography>
        </Stack>
        <Typography variant="h5" fontWeight={600} sx={{ mb: 3 }}>{title}</Typography>
        {children}
      </Paper>
    </Box>
  );
}

export function LoginPage() {
  const { setUser } = useAuth();
  const navigate = useNavigate();
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      setUser(await authApi.login(login, password));
      navigate('/');
    } catch (err) {
      setError(errorText(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <AuthShell title="Вход в систему">
      <Stack component="form" spacing={2} onSubmit={submit}>
        {error ? <Alert severity="error">{error}</Alert> : null}
        <TextField label="Логин" value={login} onChange={(e) => setLogin(e.target.value)} autoFocus required />
        <TextField label="Пароль" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <Button type="submit" variant="contained" size="large" disabled={busy}>Войти</Button>
        <Typography variant="body2" textAlign="center">
          Житель дома? <Link component={RouterLink} to="/register">Зарегистрироваться</Link>
        </Typography>
      </Stack>
    </AuthShell>
  );
}
