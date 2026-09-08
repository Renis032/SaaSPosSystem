import { apiClient } from '@/lib/api-client'
import type { Customer } from '@/types/models'

export type CustomerCreateBody = {
  fullName: string
  email?: string
  phone?: string
  storeEntity?: { id: number }
}

export function listCustomersByStore(storeId: number) {
  return apiClient<Customer[]>(`/api/customers/store/${storeId}`)
}

export function listCustomers() {
  return apiClient<Customer[]>('/api/customers')
}

export function createCustomer(body: CustomerCreateBody) {
  return apiClient<Customer>('/api/customers', { method: 'POST', body })
}
