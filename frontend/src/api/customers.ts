import { apiClient } from '@/lib/api-client'
import { isPageResponse, paginateClient, toQueryString, type PageQuery } from '@/lib/page-query'
import type { Customer, PageResponse } from '@/types/models'

export type CustomerCreateBody = {
  fullName: string
  email?: string
  phone?: string
  storeEntity?: { id: number }
}

export function listCustomersByStore(storeId: number) {
  return apiClient<Customer[]>(`/api/customers/store/${storeId}`)
}

export async function listCustomersByStorePaged(
  storeId: number,
  params: PageQuery = {},
): Promise<PageResponse<Customer>> {
  const qs = toQueryString(params)
  if (qs) {
    try {
      const res = await apiClient<PageResponse<Customer> | Customer[]>(
        `/api/customers/store/${storeId}${qs}`,
      )
      if (isPageResponse<Customer>(res)) return res
      if (Array.isArray(res)) {
        return paginateClient(res, params, (customer, q) => {
          const hay = [customer.fullName, customer.email ?? '', customer.phone ?? '', String(customer.id)]
            .join(' ')
            .toLowerCase()
          return hay.includes(q)
        })
      }
    } catch {
      // Backend page/q not ready — fall back
    }
  }

  const list = await listCustomersByStore(storeId)
  return paginateClient(Array.isArray(list) ? list : [], params, (customer, q) => {
    const hay = [customer.fullName, customer.email ?? '', customer.phone ?? '', String(customer.id)]
      .join(' ')
      .toLowerCase()
    return hay.includes(q)
  })
}

export function listCustomers() {
  return apiClient<Customer[]>('/api/customers')
}

export function searchCustomers(keyword: string) {
  return apiClient<Customer[]>(`/api/customers/search?keyword=${encodeURIComponent(keyword)}`)
}

export function createCustomer(body: CustomerCreateBody) {
  return apiClient<Customer>('/api/customers', { method: 'POST', body })
}
