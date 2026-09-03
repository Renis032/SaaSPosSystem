import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function UserSection({ onRun }: Props) {
  const [id, setId] = useState('')

  return (
    <Section title="Users" description="/api/users">
      <ActionRow title="Profile / list">
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/users/profile'))}>
          My profile
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/users/admin'))}>
          Admin user
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/users'))}>
          List users
        </button>
      </ActionRow>

      <ActionRow title="By id">
        <div className="form-grid">
          <Field label="User id" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/users/${id}`))}>
          GET by id
        </button>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/users/${id}`, { method: 'DELETE' }))}>
          Delete
        </button>
      </ActionRow>
    </Section>
  )
}
