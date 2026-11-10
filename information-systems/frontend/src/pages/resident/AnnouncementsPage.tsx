import { Card, CardContent, Stack, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { residentApi } from '../../api/endpoints';
import { PageHeader, QueryView } from '../../components/common';
import { fmtDateTime } from '../../i18n/ru';

export function AnnouncementsPage() {
  const query = useQuery({ queryKey: ['resident', 'announcements'], queryFn: residentApi.announcements });
  return (
    <>
      <PageHeader title="Объявления" subtitle="Сообщения управляющей компании для вашего дома" />
      <QueryView query={query} empty="Актуальных объявлений нет">
        {(list) => (
          <Stack spacing={2}>
            {list.map((a) => (
              <Card key={a.id} variant="outlined">
                <CardContent>
                  <Typography variant="h6">{a.title}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {fmtDateTime(a.visibleFrom)}{a.visibleTo ? ` — ${fmtDateTime(a.visibleTo)}` : ''}
                  </Typography>
                  <Typography sx={{ mt: 1, whiteSpace: 'pre-line' }}>{a.body}</Typography>
                </CardContent>
              </Card>
            ))}
          </Stack>
        )}
      </QueryView>
    </>
  );
}
