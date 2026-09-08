import { useCallback, useEffect, useState } from 'react'
import { activateSubscription, checkoutSubscription, getSubscription } from '@/api/billing'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Subscription } from '@/types/models'

export function BillingPanel() {
  const { storeId } = useAdminContext()
  const [subscription, setSubscription] = useState<Subscription | null>(null)
  const [plan, setPlan] = useState<'STARTER' | 'PRO'>('STARTER')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [checkoutBusy, setCheckoutBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setSubscription(await getSubscription(storeId))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load subscription')
    } finally {
      setLoading(false)
    }
  }, [storeId])

  useEffect(() => {
    void load()
  }, [load])

  async function handleActivate() {
    setBusy(true)
    setError(null)
    setMessage(null)
    try {
      const result = await activateSubscription({ storeId, plan })
      setSubscription(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Activation failed')
    } finally {
      setBusy(false)
    }
  }

  async function handleStripeCheckout() {
    setCheckoutBusy(true)
    setError(null)
    setMessage(null)
    try {
      const result = await checkoutSubscription({
        storeId,
        plan,
        successUrl: `${window.location.origin}/admin/billing?success=1`,
        cancelUrl: `${window.location.origin}/admin/billing?canceled=1`,
      })
      if (result.url) {
        window.location.href = result.url
        return
      }
      if (result.mode === 'local' || result.message) {
        setMessage(result.message ?? 'Subscription updated in local mode.')
        await load()
      } else {
        setMessage('Checkout completed without a redirect URL.')
        await load()
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Checkout failed')
    } finally {
      setCheckoutBusy(false)
    }
  }

  return (
    <div className="admin-panel panel">
      <div className="panel-header row-between">
        <div>
          <h2>Billing / Subscription</h2>
          <p>Plan status for store #{storeId}</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => void load()}>
          Refresh
        </button>
      </div>
      <div className="panel-body">
        {error ? <div className="app-alert error">{error}</div> : null}
        {message ? <div className="app-alert">{message}</div> : null}
        {loading ? <p className="muted">Loading…</p> : null}

        {subscription?.message ? <div className="app-alert">{subscription.message}</div> : null}

        <div className="stat-grid">
          <div className="stat-tile">
            <span>Plan</span>
            <strong>{subscription?.plan ?? '—'}</strong>
          </div>
          <div className="stat-tile">
            <span>Status</span>
            <strong>{subscription?.status ?? '—'}</strong>
          </div>
          <div className="stat-tile">
            <span>Entitled</span>
            <strong>{subscription?.entitled == null ? '—' : subscription.entitled ? 'Yes' : 'No'}</strong>
          </div>
        </div>

        <div className="stack-form">
          <h3>Activate plan</h3>
          <div className="form-grid">
            <label className="field">
              <span className="field-label">Plan</span>
              <select value={plan} onChange={(e) => setPlan(e.target.value as 'STARTER' | 'PRO')}>
                <option value="STARTER">STARTER</option>
                <option value="PRO">PRO</option>
              </select>
            </label>
          </div>
          <div className="inline-actions">
            <button type="button" className="btn" disabled={busy || checkoutBusy} onClick={() => void handleActivate()}>
              {busy ? 'Activating…' : 'Activate subscription'}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              disabled={busy || checkoutBusy}
              onClick={() => void handleStripeCheckout()}
            >
              {checkoutBusy ? 'Starting…' : 'Start Stripe checkout'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
