import { apiClient } from '@/lib/api-client'

export type DemoAccount = {
  role: string
  email: string
  path?: string
  userId?: number
}

export type DemoResetResult = {
  message?: string
  storeId?: number
  password?: string
  accounts?: DemoAccount[]
  truncatedTables?: number
}

export type DemoInfo = {
  password: string
  accounts: DemoAccount[]
  hint?: string
}

export function getDemoInfo() {
  return apiClient<DemoInfo>('/api/dev/demo-info', { auth: false, token: null })
}

export function resetDemo() {
  return apiClient<DemoResetResult>('/api/dev/reset-demo', {
    method: 'POST',
    auth: false,
    token: null,
    signal: AbortSignal.timeout(60_000),
  })
}
