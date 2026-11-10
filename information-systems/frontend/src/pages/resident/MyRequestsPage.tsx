import { Link as RouterLink } from 'react-router-dom';
import { Button } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useQuery } from '@tanstack/react-query';
import { residentApi } from '../../api/endpoints';
import { PageHeader, QueryView } from '../../components/common';
import { RequestTable } from '../../components/RequestTable';

export function MyRequestsPage() {
  const query = useQuery({ queryKey: ['resident', 'requests'], queryFn: residentApi.requests });
  return (
    <>
      <PageHeader title="Мои заявки" subtitle="Обращения в управляющую компанию"
                  action={<Button variant="contained" startIcon={<AddIcon />} component={RouterLink} to="/my/requests/new">
                    Новая заявка</Button>} />
      <QueryView query={query} empty="Вы ещё не подавали заявок">
        {(rows) => <RequestTable rows={rows} />}
      </QueryView>
    </>
  );
}
