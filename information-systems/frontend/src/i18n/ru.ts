import type {
  ComplaintStatus, Notification, OrderStatus, OwnershipType, RequestAction, RequestStatus, ResourceType, UserRole,
} from '../api/types';
import { ApiError } from '../api/client';

export const roleLabels: Record<UserRole, string> = {
  RESIDENT: 'Житель',
  EMPLOYEE: 'Сотрудник УК',
  COMPANY_ADMIN: 'Администратор УК',
  INSPECTOR: 'Инспектор ГЖИ',
};

export const statusLabels: Record<RequestStatus, string> = {
  NEW: 'Новая',
  ACCEPTED: 'Принята',
  ASSIGNED: 'Назначена',
  IN_PROGRESS: 'В работе',
  DONE: 'Выполнена',
  CLOSED: 'Закрыта',
  CANCELLED: 'Отменена',
};

export const statusColors: Record<RequestStatus, 'default' | 'primary' | 'secondary' | 'info' | 'success' | 'warning' | 'error'> = {
  NEW: 'info',
  ACCEPTED: 'primary',
  ASSIGNED: 'secondary',
  IN_PROGRESS: 'warning',
  DONE: 'success',
  CLOSED: 'default',
  CANCELLED: 'default',
};

export const resourceLabels: Record<ResourceType, string> = {
  COLD_WATER: 'Холодная вода',
  HOT_WATER: 'Горячая вода',
  ELECTRICITY: 'Электроэнергия',
  HEAT: 'Отопление',
  GAS: 'Газ',
};

export const resourceUnits: Record<ResourceType, string> = {
  COLD_WATER: 'м³', HOT_WATER: 'м³', ELECTRICITY: 'кВт·ч', HEAT: 'Гкал', GAS: 'м³',
};

export const complaintStatusLabels: Record<ComplaintStatus, string> = {
  NEW: 'Новая',
  UNDER_REVIEW: 'На рассмотрении',
  REJECTED: 'Отклонена',
  ORDER_ISSUED: 'Выдано предписание',
};

export const orderStatusLabels: Record<OrderStatus, string> = {
  ISSUED: 'Выдано',
  REPORT_SUBMITTED: 'Отчёт на проверке',
  FULFILLED: 'Исполнено',
  NOT_FULFILLED: 'Не исполнено',
  OVERDUE: 'Просрочено',
};

export const ownershipLabels: Record<OwnershipType, string> = {
  OWNER: 'Собственник',
  TENANT: 'Наниматель',
};

export const actionLabels: Record<RequestAction, string> = {
  CANCEL: 'Отменить заявку',
  RATE: 'Принять и оценить',
  REJECT_RESULT: 'Не устраивает результат',
  CREATE_REPEAT: 'Подать повторную заявку',
  COMPLAIN: 'Пожаловаться в ГЖИ',
  COMMENT: 'Комментировать',
  ACCEPT: 'Принять в работу',
  ASSIGN: 'Назначить исполнителя',
  START: 'Начать работу',
  COMPLETE: 'Завершить работу',
};

const errorMessages: Record<string, string> = {
  NETWORK: 'Сервер недоступен. Проверьте подключение.',
  BAD_CREDENTIALS: 'Неверный логин или пароль.',
  UNAUTHENTICATED: 'Сессия истекла, войдите снова.',
  FORBIDDEN: 'Недостаточно прав для этого действия.',
  CSRF_HEADER_MISSING: 'Запрос отклонён из соображений безопасности.',
  NOT_FOUND: 'Объект не найден.',
  VALIDATION_FAILED: 'Проверьте правильность заполнения полей.',
  CONFLICT: 'Данные изменились, обновите страницу.',
  BUSINESS_RULE: 'Действие нарушает правила системы.',
  INTERNAL_ERROR: 'Внутренняя ошибка сервера.',
  STATE_CONFLICT: 'Заявка уже в другом статусе — обновите страницу.',
  ROLE_REQUIRED: 'Действие недоступно для вашей роли.',
  LOGIN_TAKEN: 'Такой логин уже занят.',
  APARTMENT_NOT_IN_HOUSE: 'Квартира не относится к выбранному дому.',
  NOT_CONFIRMED_RESIDENT: 'Вы не подтверждены как житель этого дома. Дождитесь подтверждения от УК.',
  PREVIOUS_REQUEST_INVALID: 'Повторную заявку можно подать только по своей отменённой заявке.',
  CATEGORY_INACTIVE: 'Категория больше не используется.',
  REQUEST_CLOSED: 'Заявка уже закрыта.',
  REQUEST_IMMUTABLE_FIELDS: 'Эти поля заявки нельзя изменить.',
  TRANSITION_FORBIDDEN: 'Такой переход статуса недопустим.',
  NOT_REQUEST_AUTHOR: 'Действие доступно только автору заявки.',
  NOT_REQUEST_EXECUTOR: 'Действие доступно только назначенному исполнителю.',
  FOREIGN_COMPANY: 'Объект принадлежит другой управляющей компании.',
  FOREIGN_DISTRICT: 'Объект находится вне вашего района надзора.',
  EXECUTOR_CHANGE_FORBIDDEN: 'Исполнителя нельзя сменить на этом этапе.',
  EXECUTOR_INVALID: 'Выбранный исполнитель не работает в этой УК.',
  SPECIALIZATION_MISMATCH: 'У исполнителя нет нужной специализации.',
  APPEND_ONLY: 'Записи журнала нельзя изменять.',
  RATING_REQUIRES_DONE: 'Оценить можно только выполненную заявку.',
  METER_INACTIVE: 'Счётчик выведен из эксплуатации.',
  METER_NOT_OWNED: 'Счётчик не относится к вашей квартире.',
  READING_DECREASES: 'Показание не может быть меньше предыдущего.',
  READING_EXCEEDS_NEXT: 'Показание больше последующего.',
  COMPLAINT_NOT_ALLOWED: 'Жалобу можно подать только на просроченную, отклонённую или плохо выполненную заявку.',
  NOT_PARTICIPANT: 'Вы не участвуете в этой заявке.',
  REASON_REQUIRED: 'Укажите причину.',
  RESULT_REQUIRED: 'Опишите выполненные работы.',
  REPORT_REQUIRED: 'Заполните отчёт.',
  HOUSES_REQUIRED: 'Выберите хотя бы один дом.',
};

const fieldLabels: Record<string, string> = {
  login: 'Логин', password: 'Пароль', fullName: 'ФИО', phone: 'Телефон', email: 'E-mail',
  apartmentId: 'Квартира', houseId: 'Дом', categoryId: 'Категория', description: 'Описание',
  reason: 'Причина', score: 'Оценка', review: 'Отзыв', body: 'Текст', result: 'Результат',
  value: 'Показание', title: 'Заголовок', houseIds: 'Дома', executorId: 'Исполнитель',
  violation: 'Нарушение', dueDate: 'Срок исполнения', report: 'Отчёт', ownership: 'Основание',
};

const constraintMessages: Record<string, string> = {
  NotBlank: 'обязательно', NotNull: 'обязательно', NotEmpty: 'обязательно', Size: 'недопустимая длина',
  Pattern: 'неверный формат', Email: 'неверный e-mail', Min: 'слишком маленькое значение',
  Max: 'слишком большое значение', Future: 'дата должна быть в будущем', DecimalMin: 'отрицательное значение',
  Digits: 'слишком много знаков',
};

export function errorText(error: unknown): string {
  if (!(error instanceof ApiError)) return errorMessages.INTERNAL_ERROR;
  if (error.code === 'VALIDATION_FAILED' && error.fields.length > 0) {
    return error.fields
      .map((f) => `${fieldLabels[f.field] ?? f.field}: ${constraintMessages[f.code] ?? 'неверное значение'}`)
      .join('; ');
  }
  if (error.code.startsWith('CONSTRAINT:')) return errorMessages.BUSINESS_RULE;
  return errorMessages[error.code] ?? errorMessages.INTERNAL_ERROR;
}

export function notificationText(n: Notification): string {
  switch (n.event) {
    case 'REQUEST_CREATED': return `Поступила новая заявка №${n.entityId}`;
    case 'STATUS_CHANGED':
      return `Заявка №${n.entityId}: статус «${n.status ? statusLabels[n.status] : '—'}»`;
    case 'REQUEST_ASSIGNED': return `Вам назначена заявка №${n.entityId}`;
    case 'RESULT_REJECTED': return `Житель не принял результат по заявке №${n.entityId}`;
    case 'COMPLAINT_FILED': return `Подана жалоба №${n.entityId}`;
    case 'ORDER_ISSUED': return `Выдано предписание №${n.entityId}`;
    case 'ORDER_REPORTED': return `УК отчиталась по предписанию №${n.entityId}`;
    case 'ORDER_REVIEWED': return `Проверено исполнение предписания №${n.entityId}`;
    case 'ANNOUNCEMENT_PUBLISHED': return 'Новое объявление управляющей компании';
    case 'RESIDENT_CONFIRMED': return 'Ваша привязка к квартире подтверждена';
  }
}

const dateTime = new Intl.DateTimeFormat('ru-RU', { dateStyle: 'short', timeStyle: 'short' });
const dateOnly = new Intl.DateTimeFormat('ru-RU', { dateStyle: 'medium' });

export const fmtDateTime = (iso: string | null) => (iso ? dateTime.format(new Date(iso)) : '—');
export const fmtDate = (iso: string | null) => (iso ? dateOnly.format(new Date(iso)) : '—');
