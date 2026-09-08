import { apiClient } from '@/lib/api-client'
import type { Order, Receipt } from '@/types/models'

export type CreateOrderBody = {
  storeId: number
  customerId?: number
  paymentType: 'CASH' | 'CARD' | 'UPI'
  stripePaymentIntentId?: string
  items: Array<{ productId: number; quantity: number }>
}

export function createOrder(body: CreateOrderBody) {
  return apiClient<Order>('/api/orders', { method: 'POST', body })
}

export function getOrderReceipt(orderId: number) {
  return apiClient<Receipt>(`/api/orders/${orderId}/receipt`)
}

export function listOrdersByStore(storeId: number) {
  return apiClient<Order[]>(`/api/orders/store/${storeId}`)
}

export function listTodayOrdersByStore(storeId: number) {
  return apiClient<Order[]>(`/api/orders/today/store/${storeId}`)
}
