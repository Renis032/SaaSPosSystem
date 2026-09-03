import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function CustomerSection({ onRun }: Props) {
  const [form, setForm] = useState({ fullName: '', email: '', phone: '' })
  const [id, setId] = useState('')
  const [keyword, setKeyword] = useState('')

  return (
    <Section title="Customers" description="/api/customers">
      <ActionRow title="Create / update">
        <div className="form-grid">
          <Field label="Full name" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
          <Field label="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <Field label="Phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <Field label="Customer id (update)" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/customers', { method: 'POST', body: form }))}>
          Create
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/customers/${id}`, { method: 'PUT', body: form }))}>
          Update
        </button>
      </ActionRow>

      <ActionRow title="List / search / delete">
        <div className="form-grid">
          <Field label="Keyword" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          <Field label="Customer id" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/customers'))}>
          GET all
        </button>
        <button
          type="button"
          className="btn"
          onClick={() => onRun(() => apiClient(`/api/customers/search?keyword=${encodeURIComponent(keyword)}`))}
        >
          Search
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/customers/${id}`))}>
          GET by id
        </button>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/customers/${id}`, { method: 'DELETE' }))}>
          Delete
        </button>
      </ActionRow>
    </Section>
  )
}
