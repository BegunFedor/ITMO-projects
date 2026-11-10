import { useState } from 'react';
import { Link as RouterLink, NavLink, Outlet, useNavigate } from 'react-router-dom';
import {
  AppBar, Badge, Box, Button, Container, Divider, IconButton, Menu, MenuItem, Stack, Toolbar, Tooltip, Typography,
} from '@mui/material';
import NotificationsIcon from '@mui/icons-material/NotificationsOutlined';
import ApartmentIcon from '@mui/icons-material/ApartmentOutlined';
import AccountIcon from '@mui/icons-material/AccountCircleOutlined';
import { useQuery } from '@tanstack/react-query';
import { notificationApi } from '../api/endpoints';
import type { UserRole } from '../api/types';
import { useAuth, useCurrentUser } from '../auth/AuthContext';
import { roleLabels } from '../i18n/ru';

export const navigation: Record<UserRole, { to: string; label: string }[]> = {
  RESIDENT: [
    { to: '/my/requests', label: 'Мои заявки' },
    { to: '/my/meters', label: 'Счётчики' },
    { to: '/my/announcements', label: 'Объявления' },
    { to: '/my/complaints', label: 'Жалобы' },
  ],
  COMPANY_ADMIN: [
    { to: '/company/requests', label: 'Заявки' },
    { to: '/company/residents', label: 'Жители' },
    { to: '/company/announcements', label: 'Объявления' },
    { to: '/company/orders', label: 'Предписания' },
  ],
  EMPLOYEE: [{ to: '/work/tasks', label: 'Мои задачи' }],
  INSPECTOR: [
    { to: '/inspection/overdue', label: 'Просрочки' },
    { to: '/inspection/complaints', label: 'Жалобы' },
    { to: '/inspection/orders', label: 'Предписания' },
  ],
};

export function Layout() {
  const user = useCurrentUser();
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [anchor, setAnchor] = useState<HTMLElement | null>(null);
  const unread = useQuery({ queryKey: ['notifications', 'unread'], queryFn: notificationApi.unread, refetchInterval: 30000 });

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="sticky" elevation={0} color="inherit" sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Toolbar sx={{ gap: 2 }}>
          <Stack direction="row" alignItems="center" spacing={1} component={RouterLink} to="/"
                 sx={{ color: 'primary.main', textDecoration: 'none' }}>
            <ApartmentIcon />
            <Typography variant="h6" fontWeight={700} sx={{ display: { xs: 'none', md: 'block' } }}>МКД·Обращения</Typography>
          </Stack>
          <Stack direction="row" spacing={0.5} sx={{ flexGrow: 1, overflowX: 'auto' }}>
            {navigation[user.role].map((item) => (
              <Button key={item.to} component={NavLink} to={item.to} color="inherit"
                      sx={{ whiteSpace: 'nowrap', '&.active': { color: 'primary.main', bgcolor: 'action.selected' } }}>
                {item.label}
              </Button>
            ))}
          </Stack>
          <Tooltip title="Уведомления">
            <IconButton onClick={() => navigate('/notifications')}>
              <Badge badgeContent={unread.data?.unread ?? 0} color="error"><NotificationsIcon /></Badge>
            </IconButton>
          </Tooltip>
          <IconButton onClick={(e) => setAnchor(e.currentTarget)}><AccountIcon /></IconButton>
          <Menu anchorEl={anchor} open={anchor !== null} onClose={() => setAnchor(null)}>
            <Box sx={{ px: 2, py: 1 }}>
              <Typography fontWeight={600}>{user.fullName}</Typography>
              <Typography variant="body2" color="text.secondary">{roleLabels[user.role]} · {user.login}</Typography>
            </Box>
            <Divider />
            <MenuItem onClick={async () => { setAnchor(null); await logout(); navigate('/login'); }}>Выйти</MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Outlet />
      </Container>
    </Box>
  );
}
