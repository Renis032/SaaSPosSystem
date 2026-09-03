import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'
import { STORE_STATUSES, toOptions } from '@/lib/enums'

type Props = {
  onRun: <T>(action: () => Promise<T>) => Promise<T>
}

export function StoreSection({ onRun }: Props) {
  const [create, setCreate] = useState({
    brandName: '',
    description: '',
    storeType: '',
    contactEmail: '',
    contactPhone: '',
    contactAddress: '',
  })
  const [id, setId] = useState('')
  const [status, setStatus] = useState('ACTIVE')

  return (
    <Section title="Stores" description="/api/stores">
      <ActionRow title="List / mine">
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/stores'))}>
          GET all
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/stores/admin'))}>
          GET admin store
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/stores/employee'))}>
          GET employee store
        </button>
      </ActionRow>

      <ActionRow title="Create store">
        <div className="form-grid">
          <Field label="Brand name" value={create.brandName} onChange={(e) => setCreate({ ...create, brandName: e.target.value })} />
          <Field label="Store type" value={create.storeType} onChange={(e) => setCreate({ ...create, storeType: e.target.value })} />
          <Field label="Description" value={create.description} onChange={(e) => setCreate({ ...create, description: e.target.value })} />
          <Field label="Contact email" value={create.contactEmail} onChange={(e) => setCreate({ ...create, contactEmail: e.target.value })} />
          <Field label="Contact phone" value={create.contactPhone} onChange={(e) => setCreate({ ...create, contactPhone: e.target.value })} />
          <Field label="Contact address" value={create.contactAddress} onChange={(e) => setCreate({ ...create, contactAddress: e.target.value })} />
        </div>
        <button
          type="button"
          className="btn"
          onClick={() =>
            onRun(() =>
              apiClient('/api/stores', {
                method: 'POST',
                body: {
                  brandName: create.brandName,
                  description: create.description,
                  storeType: create.storeType,
                  contact: {
                    email: create.contactEmail,
                    phone: create.contactPhone,
                    address: create.contactAddress,
                  },
                },
              }),
            )
          }
        >
          Create
        </button>
      </ActionRow>

      <ActionRow title="By id / moderate / delete">
        <div className="form-grid">
          <Field label="Store id" value={id} onChange={(e) => setId(e.target.value)} />
          <Field as="select" label="Status" value={status} options={toOptions(STORE_STATUSES)} onChange={(e) => setStatus(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/stores/${id}`))}>
          GET by id
        </button>
        <button
          type="button"
          className="btn"
          onClick={() =>
            onRun(() =>
              apiClient(`/api/stores/${id}`, {
                method: 'PUT',
                body: {
                  brandName: create.brandName,
                  description: create.description,
                  storeType: create.storeType,
                  contact: {
                    email: create.contactEmail,
                    phone: create.contactPhone,
                    address: create.contactAddress,
                  },
                },
              }),
            )
          }
        >
          Update (uses create fields)
        </button>
        <button
          type="button"
          className="btn"
          onClick={() => onRun(() => apiClient(`/api/stores/${id}/moderate?storeStatus=${status}`, { method: 'PUT' }))}
        >
          Moderate status
        </button>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/stores/${id}`, { method: 'DELETE' }))}>
          Delete
        </button>
      </ActionRow>
    </Section>
  )
}
