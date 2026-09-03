import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'
import { toOptions, USER_ROLES } from '@/lib/enums'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function EmployeeSection({ onRun }: Props) {
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phoneNumber: '',
    role: 'CASHIER',
  })
  const [storeId, setStoreId] = useState('')
  const [id, setId] = useState('')
  const [filterRole, setFilterRole] = useState('')

  return (
    <Section title="Employees" description="/api/employees">
      <ActionRow title="Create / update">
        <div className="form-grid">
          <Field label="Store id" value={storeId} onChange={(e) => setStoreId(e.target.value)} />
          <Field label="Full name" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
          <Field label="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <Field label="Password" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          <Field label="Phone" value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} />
          <Field as="select" label="Role" value={form.role} options={toOptions(USER_ROLES)} onChange={(e) => setForm({ ...form, role: e.target.value })} />
          <Field label="Employee id (update)" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button
          type="button"
          className="btn"
          onClick={() => onRun(() => apiClient(`/api/employees/store/${storeId}`, { method: 'POST', body: form }))}
        >
          Create
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/employees/${id}`, { method: 'PUT', body: form }))}>
          Update
        </button>
      </ActionRow>

      <ActionRow title="List / delete">
        <div className="form-grid">
          <Field label="Store id" value={storeId} onChange={(e) => setStoreId(e.target.value)} />
          <Field
            as="select"
            label="Filter role (optional)"
            value={filterRole}
            options={[{ value: '', label: '(any)' }, ...toOptions(USER_ROLES)]}
            onChange={(e) => setFilterRole(e.target.value)}
          />
          <Field label="Employee id" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button
          type="button"
          className="btn"
          onClick={() =>
            onRun(() =>
              apiClient(`/api/employees/store/${storeId}${filterRole ? `?role=${filterRole}` : ''}`),
            )
          }
        >
          GET by store
        </button>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/employees/${id}`, { method: 'DELETE' }))}>
          Delete
        </button>
      </ActionRow>
    </Section>
  )
}
