import { apiClient } from '@/lib/api-client'
import type { Product } from '@/types/models'

export type ProductCreateBody = {
  name: string
  sku: string
  description?: string
  brand?: string
  imageUrl?: string
  maxRetailPrice: number
  sellingPrice: number
  storeId: number
  categoryId?: number | null
}

export function listProductsByStore(storeId: number) {
  return apiClient<Product[]>(`/api/products/store/${storeId}`)
}

export function createProduct(body: ProductCreateBody) {
  return apiClient<Product>('/api/products', { method: 'POST', body })
}

export function deleteProduct(id: number) {
  return apiClient(`/api/products/${id}`, { method: 'DELETE' })
}
