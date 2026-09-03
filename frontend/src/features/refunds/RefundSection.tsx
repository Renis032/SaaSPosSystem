import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'
import { PAYMENT_TYPES, toOptions } from '@/lib/enums'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function RefundSection({ onRun }: Props) {
  const [form, setForm] = useState({
    orderId: '',
    reason: '',
    amount: '',
    paymentType: 'CASH',
    shiftReportId: '',
  })
  const [ids, setIds] = useState({ id: '', cashierId: '', storeId: '', shiftId: '', start: '', end: '' })

  const body = {
    orderId: Number(form.orderId),
    reason: form.reason,
    amount: Number(form.amount),
    paymentType: form.paymentType,
    shiftReportId: form.shiftReportId ? Number(form.shiftReportId) : undefined,
  }

  return (
    <Section title="Refunds" description="/api/refunds">
      <ActionRow title="Create refund">
        <div className="form-grid">
          <Field label="Order id" value={form.orderId} onChange={(e) => setForm({ ...form, orderId: e.target.value })} />
          <Field label="Reason" value={form.reason} onChange={(e) => setForm({ ...form, reason: e.target.value })} />
          <Field label="Amount" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} />
          <Field
            as="select"
            label="Payment type"
            value={form.paymentType}
            options={toOptions(PAYMENT_TYPES)}
            onChange={(e) => setForm({ ...form, paymentType: e.target.value })}
          />
          <Field label="Shift report id" value={form.shiftReportId} onChange={(e) => setForm({ ...form, shiftReportId: e.target.value })} />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/refunds', { method: 'POST', body }))}>
          Create
        </button>
      </ActionRow>

      <ActionRow title="Queries / delete">
        <div className="form-grid">
          <Field label="Refund id" value={ids.id} onChange={(e) => setIds({ ...ids, id: e.target.value })} />
          <Field label="Cashier id" value={ids.cashierId} onChange={(e) => setIds({ ...ids, cashierId: e.target.value })} />
          <Field label="Store id" value={ids.storeId} onChange={(e) => setIds({ ...ids, storeId: e.target.value })} />
          <Field label="Shift id" value={ids.shiftId} onChange={(e) => setIds({ ...ids, shiftId: e.target.value })} />
          <Field label="Range start" value={ids.start} onChange={(e) => setIds({ ...ids, start: e.target.value })} hint="ISO datetime" />
          <Field label="Range end" value={ids.end} onChange={(e) => setIds({ ...ids, end: e.target.value })} hint="ISO datetime" />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/refunds/${ids.id}`))}>
          GET by id
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/refunds/cashier/${ids.cashierId}`))}>
          By cashier
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/refunds/store/${ids.storeId}`))}>
          By store
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/refunds/shift/${ids.shiftId}`))}>
          By shift
        </button>
        <button
          type="button"
          className="btn"
          onClick={() =>
            onRun(() =>
              apiClient(
                `/api/refunds/cashier/${ids.cashierId}/range?start=${encodeURIComponent(ids.start)}&end=${encodeURIComponent(ids.end)}`,
              ),
            )
          }
        >
          Cashier range
        </button>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/refunds/${ids.id}`, { method: 'DELETE' }))}>
          Delete
        </button>
      </ActionRow>
    </Section>
  )
}
