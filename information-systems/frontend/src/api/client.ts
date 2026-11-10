export interface FieldViolation { field: string; code: string }

export class ApiError extends Error {
  constructor(public readonly status: number, public readonly code: string,
              public readonly fields: FieldViolation[] = []) {
    super(code);
  }
}

async function request<T>(method: string, url: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = { 'X-Requested-With': 'XMLHttpRequest' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  let response: Response;
  try {
    response = await fetch('/api' + url, {
      method, headers, credentials: 'same-origin',
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, 'NETWORK');
  }
  const text = await response.text();
  const data = text ? JSON.parse(text) : undefined;
  if (!response.ok) {
    throw new ApiError(response.status, data?.code ?? 'INTERNAL_ERROR', data?.fields ?? []);
  }
  return data as T;
}

export const http = {
  get: <T>(url: string) => request<T>('GET', url),
  post: <T = void>(url: string, body: unknown = {}) => request<T>('POST', url, body),
};
