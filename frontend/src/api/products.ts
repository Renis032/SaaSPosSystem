import { apiClient } from '@/lib/api-client'
import { isPageResponse, paginateClient, toQueryString, type PageQuery } from '@/lib/page-query'
import type { PageResponse, Product } from '@/types/models'

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

export async function listProductsByStorePaged(
  storeId: number,
  params: PageQuery = {},
): Promise<PageResponse<Product>> {
  const qs = toQueryString(params)
  if (qs) {
    try {
      const res = await apiClient<PageResponse<Product> | Product[]>(`/api/products/store/${storeId}${qs}`)
      if (isPageResponse<Product>(res)) return res
      if (Array.isArray(res)) {
        return paginateClient(res, params, (product, q) => {
          const hay = [product.name, product.sku ?? '', product.brand ?? '', String(product.id)]
            .join(' ')
            .toLowerCase()
          return hay.includes(q)
        })
      }
    } catch {
      // Backend page/q not ready — fall back
    }
  }

  const list = await listProductsByStore(storeId)
  return paginateClient(Array.isArray(list) ? list : [], params, (product, q) => {
    const hay = [product.name, product.sku ?? '', product.brand ?? '', String(product.id)]
      .join(' ')
      .toLowerCase()
    return hay.includes(q)
  })
}

export function createProduct(body: ProductCreateBody) {
  return apiClient<Product>('/api/products', { method: 'POST', body })
}

export function deleteProduct(id: number) {
  return apiClient(`/api/products/${id}`, { method: 'DELETE' })
}
