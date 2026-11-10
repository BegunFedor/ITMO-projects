import { http } from './client';
import type {
  Announcement, Apartment, Category, Complaint, Created, ExecutorOption, House, Meter, MyApartment,
  Notification, Order, OverdueRequest, OwnershipType, PendingResident, RequestCard, RequestStatus,
  RequestSummary, User,
} from './types';

export const authApi = {
  me: () => http.get<User>('/auth/me'),
  login: (login: string, password: string) => http.post<User>('/auth/login', { login, password }),
  register: (body: {
    login: string; password: string; fullName: string; phone: string; email: string;
    apartmentId: number; ownership: OwnershipType;
  }) => http.post<User>('/auth/register', body),
  logout: () => http.post('/auth/logout'),
};

export const referenceApi = {
  houses: () => http.get<House[]>('/reference/houses'),
  apartments: (houseId: number) => http.get<Apartment[]>(`/reference/houses/${houseId}/apartments`),
  categories: () => http.get<Category[]>('/reference/categories'),
  myApartments: () => http.get<MyApartment[]>('/reference/my-apartments'),
  companyHouses: () => http.get<House[]>('/reference/company-houses'),
};

export const requestApi = {
  card: (id: number) => http.get<RequestCard>(`/requests/${id}`),
  comment: (id: number, body: string) => http.post<Created>(`/requests/${id}/comments`, { body }),
};

export const residentApi = {
  requests: () => http.get<RequestSummary[]>('/resident/requests'),
  create: (body: {
    houseId: number; apartmentId: number | null; categoryId: number; description: string;
    previousRequestId: number | null;
  }) => http.post<Created>('/resident/requests', body),
  cancel: (id: number, reason: string) => http.post(`/resident/requests/${id}/cancel`, { reason }),
  rate: (id: number, score: number, review: string) => http.post(`/resident/requests/${id}/rating`, { score, review }),
  rejectResult: (id: number, reason: string) => http.post(`/resident/requests/${id}/reject-result`, { reason }),
  meters: () => http.get<Meter[]>('/resident/meters'),
  submitReading: (meterId: number, value: number) => http.post(`/resident/meters/${meterId}/readings`, { value }),
  announcements: () => http.get<Announcement[]>('/resident/announcements'),
  complaints: () => http.get<Complaint[]>('/resident/complaints'),
  fileComplaint: (requestId: number, body: string) => http.post<Created>('/resident/complaints', { requestId, body }),
};

export const dispatchApi = {
  requests: (statuses: RequestStatus[]) =>
    http.get<RequestSummary[]>('/dispatch/requests' + (statuses.length
      ? '?' + statuses.map((s) => 'status=' + s).join('&') : '')),
  executors: (id: number) => http.get<ExecutorOption[]>(`/dispatch/requests/${id}/executors`),
  accept: (id: number) => http.post(`/dispatch/requests/${id}/accept`),
  assign: (id: number, executorId: number, comment: string) =>
    http.post(`/dispatch/requests/${id}/assign`, { executorId, comment }),
  cancel: (id: number, reason: string) => http.post(`/dispatch/requests/${id}/cancel`, { reason }),
};

export const companyApi = {
  pendingResidents: () => http.get<PendingResident[]>('/company/residents/pending'),
  confirmResident: (userId: number, apartmentId: number) =>
    http.post('/company/residents/confirm', { userId, apartmentId }),
  announcements: () => http.get<Announcement[]>('/company/announcements'),
  publish: (body: { title: string; body: string; houseIds: number[]; visibleFrom: string | null; visibleTo: string | null }) =>
    http.post<Created>('/company/announcements', body),
  orders: () => http.get<Order[]>('/company/orders'),
  submitReport: (id: number, report: string) => http.post(`/company/orders/${id}/report`, { report }),
};

export const workApi = {
  tasks: () => http.get<RequestSummary[]>('/work/tasks'),
  start: (id: number) => http.post(`/work/tasks/${id}/start`),
  complete: (id: number, result: string) => http.post(`/work/tasks/${id}/complete`, { result }),
};

export const inspectionApi = {
  overdue: () => http.get<OverdueRequest[]>('/inspection/overdue'),
  complaints: () => http.get<Complaint[]>('/inspection/complaints'),
  rejectComplaint: (id: number, reason: string) => http.post(`/inspection/complaints/${id}/reject`, { reason }),
  orders: () => http.get<Order[]>('/inspection/orders'),
  issue: (body: { companyId: number; violation: string; dueDate: string; requestIds: number[]; complaintIds: number[] }) =>
    http.post<Created>('/inspection/orders', body),
  review: (id: number, accepted: boolean, comment: string) =>
    http.post(`/inspection/orders/${id}/review`, { accepted, comment }),
};

export const notificationApi = {
  latest: () => http.get<Notification[]>('/notifications'),
  unread: () => http.get<{ unread: number }>('/notifications/unread-count'),
  readAll: () => http.post('/notifications/read-all'),
};
