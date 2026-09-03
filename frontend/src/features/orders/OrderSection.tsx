import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'
import { ORDER_STATUSES, PAYMENT_TYPES, toOptions } from '@/lib/enums'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function OrderSection({ onRun }: Props) {
  const [form, setForm] = useState({
    storeId: '',
    customerId: '',
    customerName: '',
    customerPhone: '',
    paymentType: 'CASH',
    productId: '',
    quantity: '1',
    stripePaymentIntentId: '',
  })
  const [id, setId] = useState('')
  const [cashierId, setCashierId] = useState('')
  const [orderStatus, setOrderStatus] = useState('')

  const createBody = {
    storeId: form.storeId ? Number(form.storeId) : undefined,
    customerId: form.customerId ? Number(form.customerId) : undefined,
    customerName: form.customerName || undefined,
    customerPhone: form.customerPhone || undefined,
    paymentType: form.paymentType,
    stripePaymentIntentId: form.stripePaymentIntentId || undefined,
    items: [
      {
        productId: Number(form.productId),
        quantity: Number(form.quantity),
      },
    ],
  }

  const listQuery = new URLSearchParams()
  if (form.customerId) listQuery.set('customerId', form.customerId)
  if (cashierId) listQuery.set('cashierId', cashierId)
  if (form.paymentType) listQuery.set('paymentType', form.paymentType)
  if (orderStatus) listQuery.set('orderStatus', orderStatus)
  const listQs = listQuery.toString()

  return (
    <Section title="Orders" description="/api/orders — create with one line item for quick testing">
      <ActionRow title="List">
        <div className="form-grid">
          <Field label="Order id" value={id} onChange={(e) => setId(e.target.value)} />
          <Field label="Store id" value={form.storeId} onChange={(e) => setForm({ ...form, storeId: e.target.value })} />
          <Field label="Customer id" value={form.customerId} onChange={(e) => setForm({ ...form, customerId: e.target.value })} />
          <Field label="Cashier id" value={cashierId} onChange={(e) => setCashierId(e.target.value)} />
          <Field
            as="select"
            label="Order status filter"
            value={orderStatus}
            options={[{ value: '', label: '(any)' }, ...toOptions(ORDER_STATUSES)]}
            onChange={(e) => setOrderStatus(e.target.value)}
          />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/orders'))}>
          GET all
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/orders/${id}`))}>
          GET by id
        </button>
        <button
          type="button"
          className="btn"
          onClick={() => onRun(() => apiClient(`/api/orders/store/${form.storeId}${listQs ? `?${listQs}` : ''}`))}
        >
          GET by store
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/orders/customer/${form.customerId}`))}>
          GET by customer
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/orders/cashier/${cashierId}`))}>
          GET by cashier
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/orders/today/store/${form.storeId}`))}>
          Today
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/orders/recent/store/${form.storeId}`))}>
          Recent
        </button>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient(`/api/orders/${id}/receipt`))}>
          Receipt
        </button>
      </ActionRow>

      <ActionRow title="Create">
        <div className="form-grid">
          <Field label="Store id" value={form.storeId} onChange={(e) => setForm({ ...form, storeId: e.target.value })} />
          <Field label="Customer id" value={form.customerId} onChange={(e) => setForm({ ...form, customerId: e.target.value })} />
          <Field label="Customer name" value={form.customerName} onChange={(e) => setForm({ ...form, customerName: e.target.value })} />
          <Field label="Customer phone" value={form.customerPhone} onChange={(e) => setForm({ ...form, customerPhone: e.target.value })} />
          <Field
            as="select"
            label="Payment type"
            value={form.paymentType}
            options={toOptions(PAYMENT_TYPES)}
            onChange={(e) => setForm({ ...form, paymentType: e.target.value })}
          />
          <Field label="Product id" value={form.productId} onChange={(e) => setForm({ ...form, productId: e.target.value })} />
          <Field label="Quantity" value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} />
          <Field
            label="Stripe payment intent id"
            value={form.stripePaymentIntentId}
            onChange={(e) => setForm({ ...form, stripePaymentIntentId: e.target.value })}
            hint="Needed for CARD"
          />
        </div>
        <button type="button" className="btn" onClick={() => onRun(() => apiClient('/api/orders', { method: 'POST', body: createBody }))}>
          Create
        </button>
      </ActionRow>

      <ActionRow title="Delete">
        <div className="form-grid">
          <Field label="Order id" value={id} onChange={(e) => setId(e.target.value)} />
        </div>
        <button type="button" className="btn btn-danger" onClick={() => onRun(() => apiClient(`/api/orders/${id}`, { method: 'DELETE' }))}>
          Delete by id
        </button>
        <button
          type="button"
          className="btn btn-danger"
          onClick={() => {
            if (window.confirm('Delete ALL orders?')) onRun(() => apiClient('/api/orders', { method: 'DELETE' }))
          }}
        >
          Delete all
        </button>
      </ActionRow>
    </Section>
  )
}
