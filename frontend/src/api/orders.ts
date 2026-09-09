import { apiClient } from '@/lib/api-client'
import { isPageResponse, paginateClient, toQueryString, type PageQuery } from '@/lib/page-query'
import type { Order, PageResponse, Receipt } from '@/types/models'

export type CreateOrderBody = {
  storeId: number
  customerId?: number
  branchId?: number
  paymentType: 'CASH' | 'CARD' | 'UPI'
  stripePaymentIntentId?: string
  taxRate?: number
  orderDiscountPercent?: number
  items: Array<{ productId: number; quantity: number; discountPercent?: number }>
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

export async function listOrdersByStorePaged(
  storeId: number,
  params: PageQuery = {},
): Promise<PageResponse<Order>> {
  const qs = toQueryString(params)
  if (qs) {
    try {
      const res = await apiClient<PageResponse<Order> | Order[]>(`/api/orders/store/${storeId}${qs}`)
      if (isPageResponse<Order>(res)) return res
      if (Array.isArray(res)) {
        return paginateClient(res, params, (order, q) => {
          const hay = [
            String(order.id),
            order.paymentType ?? '',
            order.customerName ?? '',
            order.customerPhone ?? '',
          ]
            .join(' ')
            .toLowerCase()
          return hay.includes(q)
        })
      }
    } catch {
      // Backend page/q not ready — fall back to full list
    }
  }

  const list = await listOrdersByStore(storeId)
  return paginateClient(Array.isArray(list) ? list : [], params, (order, q) => {
    const hay = [
      String(order.id),
      order.paymentType ?? '',
      order.customerName ?? '',
      order.customerPhone ?? '',
    ]
      .join(' ')
      .toLowerCase()
    return hay.includes(q)
  })
}

export function listTodayOrdersByStore(storeId: number) {
  return apiClient<Order[]>(`/api/orders/today/store/${storeId}`)
}

export function listRecentOrdersByStore(storeId: number) {
  return apiClient<Order[]>(`/api/orders/recent/store/${storeId}`)
}
