import { useState } from 'react';
import { ToggleButton, ToggleButtonGroup } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { dispatchApi } from '../../api/endpoints';
import type { RequestStatus } from '../../api/types';
import { PageHeader, QueryView } from '../../components/common';
import { RequestTable } from '../../components/RequestTable';

const filters: Record<string, { label: string; statuses: RequestStatus[] }> = {
  incoming: { label: 'Требуют действий', statuses: ['NEW', 'ACCEPTED'] },
  active: { label: 'В работе', statuses: ['ASSIGNED', 'IN_PROGRESS', 'DONE'] },
  archive: { label: 'Архив', statuses: ['CLOSED', 'CANCELLED'] },
  all: { label: 'Все', statuses: [] },
};

export function CompanyRequestsPage() {
  const [filter, setFilter] = useState('incoming');
  const statuses = filters[filter].statuses;
  const query = useQuery({ queryKey: ['dispatch', filter], queryFn: () => dispatchApi.requests(statuses) });
  return (
    <>
      <PageHeader title="Заявки жителей" subtitle="Приём, назначение исполнителей и контроль сроков"
                  action={
                    <ToggleButtonGroup size="small" exclusive value={filter} onChange={(_, v) => v && setFilter(v)}>
                      {Object.entries(filters).map(([k, f]) => <ToggleButton key={k} value={k}>{f.label}</ToggleButton>)}
                    </ToggleButtonGroup>} />
      <QueryView query={query} empty="Заявок нет">{(rows) => <RequestTable rows={rows} />}</QueryView>
    </>
  );
}
