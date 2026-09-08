import { apiClient } from '@/lib/api-client'
import type { Inventory } from '@/types/models'

export type InventoryCreateBody = {
  storeId: number
  productId: number
  quantity: number
  lowStockThreshold: number
}

export function listInventoriesByStore(storeId: number) {
  return apiClient<Inventory[]>(`/api/inventories/store/${storeId}`)
}

export function createInventory(body: InventoryCreateBody) {
  return apiClient<Inventory>('/api/inventories', { method: 'POST', body })
}

export function addStock(id: number, quantity: number) {
  return apiClient<Inventory>(`/api/inventories/${id}/add-stock?quantity=${quantity}`, {
    method: 'POST',
  })
}

export function adjustStock(id: number, delta: number, reason?: string) {
  const params = new URLSearchParams({ delta: String(delta) })
  if (reason?.trim()) params.set('reason', reason.trim())
  return apiClient<Inventory>(`/api/inventories/${id}/adjust?${params.toString()}`, {
    method: 'POST',
  })
}

export function listLowStock(storeId: number) {
  return apiClient<Inventory[]>(`/api/inventories/store/${storeId}/low-stock`)
}
