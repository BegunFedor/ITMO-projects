import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { List, ListItemButton, ListItemText, Paper, Badge } from '@mui/material';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { notificationApi } from '../api/endpoints';
import type { Notification, UserRole } from '../api/types';
import { useCurrentUser } from '../auth/AuthContext';
import { PageHeader, QueryView } from '../components/common';
import { fmtDateTime, notificationText } from '../i18n/ru';

function targetPath(n: Notification, role: UserRole): string | null {
  switch (n.entityType) {
    case 'REQUEST': return `/requests/${n.entityId}`;
    case 'ANNOUNCEMENT': return role === 'RESIDENT' ? '/my/announcements' : '/company/announcements';
    case 'COMPLAINT': return role === 'INSPECTOR' ? '/inspection/complaints' : '/my/complaints';
    case 'ORDER': return role === 'INSPECTOR' ? '/inspection/orders' : '/company/orders';
    case 'APARTMENT': return null;
  }
}

export function NotificationsPage() {
  const user = useCurrentUser();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['notifications', 'list'], queryFn: notificationApi.latest });

  useEffect(() => {
    if (query.data?.some((n) => !n.read)) {
      notificationApi.readAll().then(() =>
        queryClient.invalidateQueries({ queryKey: ['notifications', 'unread'] }));
    }
  }, [query.data, queryClient]);

  return (
    <>
      <PageHeader title="Уведомления" />
      <QueryView query={query} empty="Уведомлений нет">
        {(list) => (
          <Paper variant="outlined">
            <List disablePadding>
              {list.map((n) => {
                const path = targetPath(n, user.role);
                return (
                  <ListItemButton key={n.id} divider disabled={!path} onClick={() => path && navigate(path)}>
                    <Badge color="primary" variant="dot" invisible={n.read} sx={{ mr: 2 }} />
                    <ListItemText primary={notificationText(n)} secondary={fmtDateTime(n.createdAt)}
                                  primaryTypographyProps={{ fontWeight: n.read ? 400 : 600 }} />
                  </ListItemButton>
                );
              })}
            </List>
          </Paper>
        )}
      </QueryView>
    </>
  );
}
