import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { resetDemo } from '@/api/demo'
import { clearAuthSession, useAuth } from '@/stores/auth-store'
import { homePathForRole } from '@/lib/roles'

const DEMO_ACCOUNTS = [
  { role: 'Owner', email: 'owner@renko.demo', path: 'Admin console' },
  { role: 'Cashier', email: 'cashier@renko.demo', path: 'POS sell screen' },
  { role: 'Manager', email: 'manager@renko.demo', path: 'Back office' },
  { role: 'Simple user', email: 'user@renko.demo', path: 'Limited workspace' },
] as const

export function LandingPage() {
  const { token, user } = useAuth()
  const navigate = useNavigate()
  const [resetting, setResetting] = useState(false)
  const [resetMessage, setResetMessage] = useState<string | null>(null)
  const [resetError, setResetError] = useState<string | null>(null)

  async function handleResetDemo() {
    if (
      !window.confirm(
        'Reset the demo database? This wipes all data and recreates the fixed Renko demo store.',
      )
    ) {
      return
    }
    setResetting(true)
    setResetError(null)
    setResetMessage(null)
    try {
      const result = await resetDemo()
      clearAuthSession()
      setResetMessage(
        result.message ??
          'Demo ready. Sign in with owner@renko.demo / cashier@renko.demo — password Demo1234!',
      )
    } catch (err) {
      setResetError(err instanceof Error ? err.message : 'Could not reset demo')
    } finally {
      setResetting(false)
    }
  }

  return (
    <div className="landing">
      <header className="landing-top">
        <Link to="/" className="landing-brand">
          <span className="landing-mark">R</span>
          <span>Renko</span>
        </Link>
        <nav className="landing-nav">
          {token ? (
            <button
              type="button"
              className="landing-link-btn"
              onClick={() => navigate(homePathForRole(user?.role))}
            >
              Open app
            </button>
          ) : null}
          <Link to="/login">Sign in</Link>
          <Link to="/signup" className="landing-cta-ghost">
            Sign up
          </Link>
        </nav>
      </header>

      <section className="landing-hero">
        <div className="landing-hero-copy">
          <p className="landing-kicker">SaaS point of sale</p>
          <h1 className="landing-title">Renko</h1>
          <p className="landing-lede">
            Sell at the counter. Run the back office. One calm system for stores that need
            inventory, shifts, and receipts without the circus.
          </p>
          <div className="landing-actions">
            <Link to="/signup" className="landing-cta">
              Start as owner
            </Link>
            <Link to="/login" className="landing-cta-secondary">
              Sign in
            </Link>
          </div>
        </div>
        <div className="landing-hero-visual" aria-hidden="true">
          <div className="landing-hero-glow" />
          <div className="landing-ticket">
            <span>Open ticket</span>
            <strong>$42.74</strong>
            <em>3 items · Main Floor</em>
          </div>
        </div>
      </section>

      <section className="landing-demo" id="demo">
        <div className="landing-demo-copy">
          <h2>Interview demo</h2>
          <p>
            Fixed accounts, sample catalog, three branches. Reset anytime to a clean known state.
            Password for every demo user: <code>Demo1234!</code>
          </p>
          <div className="landing-demo-actions">
            <button
              type="button"
              className="landing-cta"
              disabled={resetting}
              onClick={() => void handleResetDemo()}
            >
              {resetting ? 'Resetting…' : 'Reset demo'}
            </button>
            <Link to="/login" className="landing-cta-secondary">
              Go to sign in
            </Link>
          </div>
          {resetMessage ? <p className="landing-ok">{resetMessage}</p> : null}
          {resetError ? <p className="landing-err">{resetError}</p> : null}
        </div>
        <ul className="landing-accounts">
          {DEMO_ACCOUNTS.map((account) => (
            <li key={account.email}>
              <strong>{account.role}</strong>
              <span>{account.email}</span>
              <em>{account.path}</em>
            </li>
          ))}
        </ul>
      </section>

      <section className="landing-strip">
        <div>
          <h3>Counter POS</h3>
          <p>SKU search, stock guards, cash tender, demo card, PDF receipts.</p>
        </div>
        <div>
          <h3>Back office</h3>
          <p>Products, inventory, staff, orders, reports, and billing in one console.</p>
        </div>
        <div>
          <h3>Safeguards</h3>
          <p>Role routes, store tenancy checks, and Playground kept for API exploration.</p>
        </div>
      </section>

      <footer className="landing-foot">
        <span>Renko · local demo</span>
        <Link to="/playground">API Playground</Link>
      </footer>
    </div>
  )
}
