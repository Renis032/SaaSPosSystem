import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function BranchSection({ onRun }: Props) {
  const [form, setForm] = useState({
    name: '',
    address: '',
    phone: '',
    email: '',
    storeId: '',
    openTime: '09:00:00',
    closeTime: '18:00:00',
    workdays: 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',
  })
  const [id, setId] = useState('')
  const [storeId, setStoreId] = useState('')

  const body = {
    name: form.name,
    address: form.address,
    phone: form.phone,
    email: form.email,
    storeId: Number(form.storeId),
    openTime: form.openTime,
    closeTime: form.closeTime,
    workdays: form.workdays.split(',').map((d) => d.trim()).filter(Boolean),
  }

  return (
    <Section title="Branches" description="/api/branches">
      <ActionRow title="Create / update">
        <div className="form-grid">
          <Field label="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <Field label="Address" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
          <Field label="Phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <Field label="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <Field label="Store id" value={form.storeId} onChange={(e) => setForm({ ...form, storeId: e.target.value })} />
          <Field label="Open time" value={form.openTime} onChange={(e) => setForm({ ...form, openTime: e.target.value })} hint="HH:mm:ss" />
          <Field label="Close time" value={form.closeTime} onChange={(e) => setForm({ ...form, closeTime: e.target.value })} hint="HH:mm:ss" />
          <Field label="Workdays" value={form.workdays} onChange={(e) => setForm({ ...form, workdays: e.target.value })} hint="Comma separated" />
          <Field label="Branch id (update)" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/branches', { method: 'POST', body }))}>
          Create
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/branches/${id}`, { method: 'PATCH', body }))}>
          Patch update
        </button>
      </ActionRow>

      <ActionRow title="Read / delete">
        <div className="form-grid">
          <Field label="Branch id" value={id} onChange={(e) => setId(e.target.value)} />
          <Field label="Store id" value={storeId} onChange={(e) => setStoreId(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/branches/${id}`))}>
          GET branch
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/branches/stores/${storeId}`))}>
          GET by store
        </button>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/branches/${id}`, { method: 'DELETE' }))}>
          Delete
        </button>
      </ActionRow>
    </Section>
  )
}
