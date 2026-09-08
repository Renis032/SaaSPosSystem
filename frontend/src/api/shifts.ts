import { apiClient } from '@/lib/api-client'
import type { ShiftReport } from '@/types/models'

export function startShift() {
  return apiClient<ShiftReport>('/api/shift-report/start', { method: 'POST' })
}

export function endShift() {
  return apiClient<ShiftReport>('/api/shift-report/end', { method: 'PATCH' })
}

export function getCurrentShift() {
  return apiClient<ShiftReport>('/api/shift-report/current')
}

export function listShiftsByStore(storeId: number) {
  return apiClient<ShiftReport[]>(`/api/shift-report/store/${storeId}`)
}
