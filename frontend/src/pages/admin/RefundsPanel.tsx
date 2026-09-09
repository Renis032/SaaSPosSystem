import { FormEvent, useCallback, useEffect, useState } from 'react'
import { createRefund, listRefundsByStore } from '@/api/refunds'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Refund } from '@/types/models'

export function RefundsPanel() {
  const { storeId } = useAdminContext()
  const [refunds, setRefunds] = useState<Refund[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({
    orderId: '',
    reason: '',
    amount: '',
    paymentType: 'CASH' as 'CASH' | 'CARD' | 'UPI',
  })

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const rows = await listRefundsByStore(storeId)
      setRefunds(Array.isArray(rows) ? rows : [])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load refunds')
    } finally {
      setLoading(false)
    }
  }, [storeId])

  useEffect(() => {
    void load()
  }, [load])

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await createRefund({
        orderId: Number(form.orderId),
        reason: form.reason || undefined,
        amount: form.amount !== '' ? Number(form.amount) : undefined,
        paymentType: form.paymentType,
      })
      setForm({ orderId: '', reason: '', amount: '', paymentType: 'CASH' })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Create refund failed')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="admin-panel panel">
      <div className="panel-header row-between">
        <div>
          <h2>Refunds</h2>
          <p>Refunds for store #{storeId}</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => void load()}>
          Refresh
        </button>
      </div>
      <div className="panel-body">
        {error ? <div className="app-alert error">{error}</div> : null}

        <form className="stack-form" onSubmit={handleCreate}>
          <h3>Create refund</h3>
          <div className="form-grid">
            <label className="field">
              <span className="field-label">Order ID</span>
              <input
                required
                type="number"
                min="1"
                value={form.orderId}
                onChange={(e) => setForm({ ...form, orderId: e.target.value })}
              />
            </label>
            <label className="field">
              <span className="field-label">Amount</span>
              <input
                type="number"
                step="0.01"
                min="0"
                value={form.amount}
                onChange={(e) => setForm({ ...form, amount: e.target.value })}
                placeholder="Optional"
              />
            </label>
            <label className="field">
              <span className="field-label">Payment type</span>
              <select
                value={form.paymentType}
                onChange={(e) =>
                  setForm({ ...form, paymentType: e.target.value as 'CASH' | 'CARD' | 'UPI' })
                }
              >
                <option value="CASH">CASH</option>
                <option value="CARD">CARD</option>
                <option value="UPI">UPI</option>
              </select>
            </label>
            <label className="field">
              <span className="field-label">Reason</span>
              <input
                value={form.reason}
                onChange={(e) => setForm({ ...form, reason: e.target.value })}
                placeholder="Optional"
              />
            </label>
          </div>
          <button type="submit" className="btn" disabled={saving}>
            {saving ? 'Creating…' : 'Create refund'}
          </button>
        </form>

        {loading ? <p className="muted loading-msg">Loading refunds…</p> : null}
        {!loading && refunds.length === 0 ? (
          <div className="empty-state">
            <p>No refunds yet</p>
            <p className="muted">Create a refund with an order ID above after a completed sale.</p>
          </div>
        ) : null}

        {!loading && refunds.length > 0 ? (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Order</th>
                  <th>Amount</th>
                  <th>Payment</th>
                  <th>Reason</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {refunds.map((refund) => (
                  <tr key={refund.id}>
                    <td>{refund.id}</td>
                    <td>{refund.orderId}</td>
                    <td>${(refund.amount ?? 0).toFixed(2)}</td>
                    <td>{refund.paymentType ?? '—'}</td>
                    <td>{refund.reason ?? '—'}</td>
                    <td>{refund.createdAt ? new Date(refund.createdAt).toLocaleString() : '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </div>
    </div>
  )
}
