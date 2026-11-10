import { useQuery } from '@tanstack/react-query';
import { residentApi } from '../../api/endpoints';
import { PageHeader, QueryView } from '../../components/common';
import { ComplaintTable } from '../../components/ComplaintTable';

export function MyComplaintsPage() {
  const query = useQuery({ queryKey: ['resident', 'complaints'], queryFn: residentApi.complaints });
  return (
    <>
      <PageHeader title="Жалобы в жилищную инспекцию"
                  subtitle="Подать жалобу можно из карточки просроченной, отклонённой или плохо выполненной заявки" />
      <QueryView query={query} empty="Жалоб нет">{(rows) => <ComplaintTable rows={rows} />}</QueryView>
    </>
  );
}
