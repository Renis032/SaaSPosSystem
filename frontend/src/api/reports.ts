import { apiClient } from '@/lib/api-client'
import type { AuditLog, StoreReport } from '@/types/models'

export function getStoreReport(storeId: number) {
  return apiClient<StoreReport>(`/api/reports/store/${storeId}`)
}

export function getStoreAuditLog(storeId: number) {
  return apiClient<AuditLog[]>(`/api/reports/store/${storeId}/audit`)
}
