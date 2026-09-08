import { apiClient } from '@/lib/api-client'

export type Branch = {
  id: number
  name: string
  address?: string
  phone?: string
  email?: string
  storeId?: number
}

export function listBranchesByStore(storeId: number) {
  return apiClient<Branch[]>(`/api/branches/stores/${storeId}`)
}

export function listAllBranches() {
  return apiClient<Branch[]>('/api/branches')
}
