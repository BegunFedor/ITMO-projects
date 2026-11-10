import { useQuery } from '@tanstack/react-query';
import { workApi } from '../../api/endpoints';
import { PageHeader, QueryView } from '../../components/common';
import { RequestTable } from '../../components/RequestTable';

export function TasksPage() {
  const query = useQuery({ queryKey: ['work', 'tasks'], queryFn: workApi.tasks });
  return (
    <>
      <PageHeader title="Мои задачи" subtitle="Назначенные вам заявки — откройте карточку, чтобы начать или завершить работу" />
      <QueryView query={query} empty="Назначенных заявок нет">{(rows) => <RequestTable rows={rows} showExecutor={false} />}</QueryView>
    </>
  );
}
