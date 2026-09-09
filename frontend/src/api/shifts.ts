import { apiClient } from '@/lib/api-client'
import type { ShiftReport } from '@/types/models'

export type StartShiftBody = {
  branchId?: number
}

export function startShift(body?: StartShiftBody) {
  return apiClient<ShiftReport>('/api/shift-report/start', {
    method: 'POST',
    body: body?.branchId != null ? { branchId: body.branchId } : undefined,
  })
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
