import { apiClient } from '@/lib/api-client'
import type { Store } from '@/types/models'

export type StoreCreateBody = {
  brandName: string
  description?: string
  storeType?: string
  contact?: {
    email?: string
    phone?: string
    address?: string
  }
}

export function getAdminStore() {
  return apiClient<Store>('/api/stores/admin')
}

export function getEmployeeStore() {
  return apiClient<Store>('/api/stores/employee')
}

export function createStore(body: StoreCreateBody) {
  return apiClient<Store>('/api/stores', { method: 'POST', body })
}
