import type { PageResponse } from '@/types/models'

export type PageQuery = {
  page?: number
  size?: number
  q?: string
}

export function toQueryString(params?: PageQuery): string {
  if (!params) return ''
  const sp = new URLSearchParams()
  if (params.page != null) sp.set('page', String(params.page))
  if (params.size != null) sp.set('size', String(params.size))
  if (params.q?.trim()) sp.set('q', params.q.trim())
  const s = sp.toString()
  return s ? `?${s}` : ''
}

export function isPageResponse<T>(value: unknown): value is PageResponse<T> {
  return (
    typeof value === 'object' &&
    value != null &&
    'content' in value &&
    Array.isArray((value as PageResponse<T>).content) &&
    typeof (value as PageResponse<T>).totalElements === 'number'
  )
}

export function paginateClient<T>(
  items: T[],
  params: PageQuery = {},
  filter?: (item: T, q: string) => boolean,
): PageResponse<T> {
  const q = params.q?.trim().toLowerCase() ?? ''
  let filtered = items
  if (q && filter) {
    filtered = items.filter((item) => filter(item, q))
  }
  const size = params.size && params.size > 0 ? params.size : filtered.length || 10
  const page = params.page != null && params.page >= 0 ? params.page : 0
  const totalElements = filtered.length
  const totalPages = size > 0 ? Math.max(1, Math.ceil(totalElements / size)) : 0
  const start = page * size
  const content = filtered.slice(start, start + size)
  return { content, page, size, totalElements, totalPages }
}
