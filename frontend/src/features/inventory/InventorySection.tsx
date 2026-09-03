import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function InventorySection({ onRun }: Props) {
  const [form, setForm] = useState({
    storeId: '',
    productId: '',
    quantity: '',
    lowStockThreshold: '',
  })
  const [id, setId] = useState('')
  const [addQty, setAddQty] = useState('')
  const [threshold, setThreshold] = useState('')

  const body = {
    storeId: Number(form.storeId),
    productId: Number(form.productId),
    quantity: Number(form.quantity),
    lowStockThreshold: Number(form.lowStockThreshold),
  }

  return (
    <Section title="Inventory" description="/api/inventories">
      <ActionRow title="Create / update">
        <div className="form-grid">
          <Field label="Store id" value={form.storeId} onChange={(e) => setForm({ ...form, storeId: e.target.value })} />
          <Field label="Product id" value={form.productId} onChange={(e) => setForm({ ...form, productId: e.target.value })} />
          <Field label="Quantity" value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} />
          <Field label="Low stock threshold" value={form.lowStockThreshold} onChange={(e) => setForm({ ...form, lowStockThreshold: e.target.value })} />
          <Field label="Inventory id (update)" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/inventories', { method: 'POST', body }))}>
          Create
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/inventories/${id}`, { method: 'PUT', body }))}>
          Update
        </button>
      </ActionRow>

      <ActionRow title="Queries / stock ops">
        <div className="form-grid">
          <Field label="Store id" value={form.storeId} onChange={(e) => setForm({ ...form, storeId: e.target.value })} />
          <Field label="Product id" value={form.productId} onChange={(e) => setForm({ ...form, productId: e.target.value })} />
          <Field label="Inventory id" value={id} onChange={(e) => setId(e.target.value)} />
          <Field label="Add quantity" value={addQty} onChange={(e) => setAddQty(e.target.value)} />
          <Field label="New threshold" value={threshold} onChange={(e) => setThreshold(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/inventories/store/${form.storeId}`))}>
          GET by store
        </button>
        <button
          type="button"
          className="btn"
          onClick={() => onRun(() => apiClient(`/api/inventories/store/${form.storeId}/product/${form.productId}`))}
        >
          GET by product
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/inventories/store/${form.storeId}/low-stock`))}>
          Low stock
        </button>
        <button
          type="button"
          className="btn"
          onClick={() => onRun(() => apiClient(`/api/inventories/${id}/add-stock?quantity=${addQty}`, { method: 'POST' }))}
        >
          Add stock
        </button>
        <button
          type="button"
          className="btn"
          onClick={() => onRun(() => apiClient(`/api/inventories/${id}/threshold?threshold=${threshold}`))}
        >
          Set threshold
        </button>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/inventories/${id}`, { method: 'DELETE' }))}>
          Delete
        </button>
      </ActionRow>
    </Section>
  )
}
