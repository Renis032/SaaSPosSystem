import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function ShiftReportSection({ onRun }: Props) {
  const [cashierId, setCashierId] = useState('')
  const [storeId, setStoreId] = useState('')
  const [id, setId] = useState('')
  const [date, setDate] = useState('')

  return (
    <Section title="Shift reports" description="/api/shift-report — start/end use the logged-in user">
      <ActionRow title="Current shift">
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/shift-report/start', { method: 'POST' }))}>
          Start shift
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/shift-report/end', { method: 'PATCH' }))}>
          End shift
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/shift-report/current'))}>
          Current
        </button>
      </ActionRow>

      <ActionRow title="Queries">
        <div className="form-grid">
          <Field label="Shift id" value={id} onChange={(e) => setId(e.target.value)} />
          <Field label="Cashier id" value={cashierId} onChange={(e) => setCashierId(e.target.value)} />
          <Field label="Store id" value={storeId} onChange={(e) => setStoreId(e.target.value)} />
          <Field label="Date" value={date} onChange={(e) => setDate(e.target.value)} hint="ISO datetime" />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/shift-report/${id}`))}>
          GET by id
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/shift-report/cashier/${cashierId}`))}>
          By cashier
        </button>
        <button
          type="button"
          className="btn"
          onClick={() =>
            onRun(() =>
              apiClient(`/api/shift-report/cashier/${cashierId}/by-date?date=${encodeURIComponent(date)}`),
            )
          }
        >
          By cashier + date
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/shift-report/store/${storeId}`))}>
          By store
        </button>
      </ActionRow>
    </Section>
  )
}
