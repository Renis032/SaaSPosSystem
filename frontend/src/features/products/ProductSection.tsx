import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function ProductSection({ onRun }: Props) {
  const [form, setForm] = useState({
    name: '',
    sku: '',
    description: '',
    brand: '',
    imageUrl: '',
    maxRetailPrice: '',
    sellingPrice: '',
    storeId: '',
    categoryId: '',
  })
  const [id, setId] = useState('')
  const [keyword, setKeyword] = useState('')

  const body = {
    name: form.name,
    sku: form.sku,
    description: form.description,
    brand: form.brand,
    imageUrl: form.imageUrl,
    maxRetailPrice: Number(form.maxRetailPrice),
    sellingPrice: Number(form.sellingPrice),
    storeId: Number(form.storeId),
    categoryId: form.categoryId ? Number(form.categoryId) : null,
  }

  return (
    <Section title="Products" description="/api/products">
      <ActionRow title="Create / update">
        <div className="form-grid">
          <Field label="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <Field label="SKU" value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} />
          <Field label="Brand" value={form.brand} onChange={(e) => setForm({ ...form, brand: e.target.value })} />
          <Field label="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          <Field label="Image URL" value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} />
          <Field label="MRP" value={form.maxRetailPrice} onChange={(e) => setForm({ ...form, maxRetailPrice: e.target.value })} />
          <Field label="Selling price" value={form.sellingPrice} onChange={(e) => setForm({ ...form, sellingPrice: e.target.value })} />
          <Field label="Store id" value={form.storeId} onChange={(e) => setForm({ ...form, storeId: e.target.value })} />
          <Field label="Category id" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} />
          <Field label="Product id (update)" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/products', { method: 'POST', body }))}>
          Create
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/products/${id}`, { method: 'PATCH', body }))}>
          Patch update
        </button>
      </ActionRow>

      <ActionRow title="List / search / delete">
        <div className="form-grid">
          <Field label="Store id" value={form.storeId} onChange={(e) => setForm({ ...form, storeId: e.target.value })} />
          <Field label="Keyword" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          <Field label="Product id" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/products/store/${form.storeId}`))}>
          GET by store
        </button>
        <button
          type="button"
          className="btn"
          onClick={() => onRun(() => apiClient(`/api/products/store/${form.storeId}/search?keyword=${encodeURIComponent(keyword)}`))}
        >
          Search
        </button>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/products/${id}`, { method: 'DELETE' }))}>
          Delete
        </button>
      </ActionRow>
    </Section>
  )
}
