import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function CategorySection({ onRun }: Props) {
  const [name, setName] = useState('')
  const [storeId, setStoreId] = useState('')
  const [id, setId] = useState('')

  return (
    <Section title="Categories" description="/api/categories">
      <ActionRow title="Create / update">
        <div className="form-grid">
          <Field label="Name" value={name} onChange={(e) => setName(e.target.value)} />
          <Field label="Store id" value={storeId} onChange={(e) => setStoreId(e.target.value)} />
          <Field label="Category id (update)" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button
          type="button"
          className="btn"
          onClick={() => onRun(() => apiClient('/api/categories', { method: 'POST', body: { name, storeId: Number(storeId) } }))}
        >
          Create
        </button>
        <button
          type="button"
          className="btn"
          onClick={() => onRun(() => apiClient(`/api/categories/${id}`, { method: 'PUT', body: { name, storeId: Number(storeId) } }))}
        >
          Update
        </button>
      </ActionRow>

      <ActionRow title="List / delete">
        <div className="form-grid">
          <Field label="Store id" value={storeId} onChange={(e) => setStoreId(e.target.value)} />
          <Field label="Category id" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/categories/store/${storeId}`))}>
          GET by store
        </button>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/categories/${id}`, { method: 'DELETE' }))}>
          Delete
        </button>
      </ActionRow>
    </Section>
  )
}
