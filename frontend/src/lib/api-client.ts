const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export class ApiError extends Error {
  status: number
  body: unknown

  constructor(message: string, status: number, body: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: unknown
  token?: string | null
  auth?: boolean
}

function resolveToken(token: string | null | undefined, auth: boolean): string | null {
  if (token !== undefined) return token
  if (!auth) return null
  return localStorage.getItem('renko_pos_token')
}

export async function apiClient<T>(
  path: string,
  { body, token, auth = true, headers, ...options }: RequestOptions = {},
): Promise<T> {
  const resolvedToken = resolveToken(token, auth)
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...(resolvedToken ? { Authorization: `Bearer ${resolvedToken}` } : {}),
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  const raw = await response.text()
  const contentType = response.headers.get('content-type') ?? ''
  let payload: unknown = null
  if (raw) {
    payload = contentType.includes('application/json') ? JSON.parse(raw) : raw
  }

  if (!response.ok) {
    const message =
      typeof payload === 'object' && payload && 'message' in payload
        ? String((payload as { message: unknown }).message)
        : `Request failed with status ${response.status}`
    throw new ApiError(message, response.status, payload)
  }

  return payload as T
}
