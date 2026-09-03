import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'

type Props = { onRun: <T>(action: () => Promise<T>) => Promise<T> }

export function BillingSection({ onRun }: Props) {
  const [amountCents, setAmountCents] = useState('1000')
  const [refund, setRefund] = useState({ paymentIntentId: '', amountCents: '', reason: 'requested_by_customer' })

  return (
    <Section title="Billing (Stripe)" description="/api/billing">
      <ActionRow title="Payment intent">
        <div className="form-grid">
          <Field label="Amount (cents)" value={amountCents} onChange={(e) => setAmountCents(e.target.value)} />
        </div>
        <button
          type="button"
          className="btn"
          onClick={() =>
            onRun(() =>
              apiClient('/api/billing/create-payment-intent', {
                method: 'POST',
                body: { amountCents: Number(amountCents) },
              }),
            )
          }
        >
          Create payment intent
        </button>
      </ActionRow>

      <ActionRow title="Refund payment intent">
        <div className="form-grid">
          <Field label="Payment intent id" value={refund.paymentIntentId} onChange={(e) => setRefund({ ...refund, paymentIntentId: e.target.value })} />
          <Field label="Amount (cents)" value={refund.amountCents} onChange={(e) => setRefund({ ...refund, amountCents: e.target.value })} />
          <Field label="Reason" value={refund.reason} onChange={(e) => setRefund({ ...refund, reason: e.target.value })} />
        </div>
        <button
          type="button"
          className="btn"
          onClick={() =>
            onRun(() =>
              apiClient('/api/billing/refund', {
                method: 'POST',
                body: {
                  paymentIntentId: refund.paymentIntentId,
                  amountCents: Number(refund.amountCents),
                  reason: refund.reason,
                },
              }),
            )
          }
        >
          Refund
        </button>
      </ActionRow>
    </Section>
  )
}
