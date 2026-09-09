import { apiClient } from '@/lib/api-client'
import type { Refund } from '@/types/models'

export type CreateRefundBody = {
  orderId: number
  reason?: string
  amount?: number
  paymentType?: 'CASH' | 'CARD' | 'UPI'
  shiftReportId?: number
}

export function createRefund(body: CreateRefundBody) {
  return apiClient<Refund>('/api/refunds', { method: 'POST', body })
}

export function listRefundsByStore(storeId: number) {
  return apiClient<Refund[]>(`/api/refunds/store/${storeId}`)
}

export function getRefund(id: number) {
  return apiClient<Refund>(`/api/refunds/${id}`)
}

export function deleteRefund(id: number) {
  return apiClient(`/api/refunds/${id}`, { method: 'DELETE' })
}
