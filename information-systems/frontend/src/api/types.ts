export type UserRole = 'RESIDENT' | 'EMPLOYEE' | 'COMPANY_ADMIN' | 'INSPECTOR';
export type RequestStatus = 'NEW' | 'ACCEPTED' | 'ASSIGNED' | 'IN_PROGRESS' | 'DONE' | 'CLOSED' | 'CANCELLED';
export type ResourceType = 'COLD_WATER' | 'HOT_WATER' | 'ELECTRICITY' | 'HEAT' | 'GAS';
export type ComplaintStatus = 'NEW' | 'UNDER_REVIEW' | 'REJECTED' | 'ORDER_ISSUED';
export type OrderStatus = 'ISSUED' | 'REPORT_SUBMITTED' | 'FULFILLED' | 'NOT_FULFILLED' | 'OVERDUE';
export type OwnershipType = 'OWNER' | 'TENANT';
export type NotificationEvent =
  | 'REQUEST_CREATED' | 'STATUS_CHANGED' | 'REQUEST_ASSIGNED' | 'RESULT_REJECTED' | 'COMPLAINT_FILED'
  | 'ORDER_ISSUED' | 'ORDER_REPORTED' | 'ORDER_REVIEWED' | 'ANNOUNCEMENT_PUBLISHED' | 'RESIDENT_CONFIRMED';
export type NotificationTarget = 'REQUEST' | 'ANNOUNCEMENT' | 'COMPLAINT' | 'ORDER' | 'APARTMENT';
export type RequestAction =
  | 'CANCEL' | 'RATE' | 'REJECT_RESULT' | 'CREATE_REPEAT' | 'COMPLAIN' | 'COMMENT'
  | 'ACCEPT' | 'ASSIGN' | 'START' | 'COMPLETE';

export interface User { id: number; login: string; fullName: string; role: UserRole }
export interface House { id: number; address: string }
export interface Apartment { id: number; number: string; entrance: number | null; floor: number | null }
export interface Category { id: number; name: string; normHours: number; emergency: boolean }
export interface MyApartment {
  apartmentId: number; number: string; houseId: number; address: string; ownership: OwnershipType; confirmed: boolean;
}
export interface PendingResident {
  userId: number; fullName: string; phone: string | null; apartmentId: number; apartmentNumber: string;
  address: string; ownership: OwnershipType; linkedAt: string;
}
export interface RequestSummary {
  id: number; status: RequestStatus; categoryName: string; houseId: number; address: string;
  apartmentNumber: string | null; description: string; executorName: string | null;
  createdAt: string; deadlineAt: string | null; overdue: boolean;
}
export interface RequestDetails {
  id: number; status: RequestStatus; categoryId: number; categoryName: string; houseId: number; address: string;
  companyName: string; apartmentId: number | null; apartmentNumber: string | null; description: string;
  authorName: string; authorPhone: string | null; executorId: number | null; executorName: string | null;
  resultText: string | null; previousRequestId: number | null; createdAt: string; deadlineAt: string | null;
  statusChangedAt: string; overdue: boolean;
}
export interface StatusHistory {
  status: RequestStatus; changedByName: string | null;
  changedByRole: UserRole | null; changedAt: string; comment: string | null;
}
export interface Comment { id: number; authorName: string; authorRole: UserRole; body: string; createdAt: string }
export interface Rating { score: number; review: string | null }
export interface RequestCard {
  request: RequestDetails; rating: Rating | null; hasComplaint: boolean;
  history: StatusHistory[]; comments: Comment[]; actions: RequestAction[];
}
export interface ExecutorOption { id: number; fullName: string; matchesSpecialization: boolean; activeRequests: number }
export interface Meter {
  id: number; resource: ResourceType; serialNumber: string; address: string; apartmentNumber: string;
  lastValue: number | null; lastDate: string | null;
}
export interface Announcement {
  id: number; title: string; body: string; visibleFrom: string; visibleTo: string | null; houses: House[] | null;
}
export interface Complaint {
  id: number; requestId: number; status: ComplaintStatus; body: string; residentName: string; address: string;
  categoryName: string; companyId: number; companyName: string; createdAt: string; decidedAt: string | null;
  decisionComment: string | null; orderId: number | null;
}
export interface Order {
  id: number; number: string; companyId: number; companyName: string; violation: string; issuedAt: string;
  dueDate: string; status: OrderStatus; reportText: string | null; reportAt: string | null;
  reviewedAt: string | null; reviewComment: string | null; requestIds: number[];
}
export interface OverdueRequest {
  requestId: number; companyId: number; companyName: string; address: string; categoryName: string;
  status: RequestStatus; createdAt: string; deadlineAt: string; hoursOverdue: number; hasComplaint: boolean;
}
export interface Notification {
  id: number; event: NotificationEvent; entityType: NotificationTarget; entityId: number;
  status: RequestStatus | null; read: boolean; createdAt: string;
}
export interface Created { id: number }
