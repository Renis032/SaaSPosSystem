import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function UserSection({ onRun }: Props) {
  const [id, setId] = useState('')

  return (
    <Section title="Users" description="/api/users">
      <ActionRow title="List">
        <div className="form-grid">
          <Field label="User id" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/users'))}>
          GET all
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/users/${id}`))}>
          GET by id
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/users/profile'))}>
          My profile
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/users/admin'))}>
          Admin user
        </button>
      </ActionRow>

      <ActionRow title="Delete">
        <div className="form-grid">
          <Field label="User id" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/users/${id}`, { method: 'DELETE' }))}>
          Delete by id
        </button>
        <button
          type="button"
          className="btn btn-danger"
          onClick={() => {
            if (
              window.confirm(
                'Delete ALL users? This also deletes all refunds, shift reports, and orders, and clears store-admin / branch-manager links.',
              )
            ) {
              onRun(() => apiClient('/api/users', { method: 'DELETE' }))
            }
          }}
        >
          Delete all
        </button>
      </ActionRow>
    </Section>
  )
}
