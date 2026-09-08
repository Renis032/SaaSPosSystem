import { FormEvent, useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { login } from '@/api/auth'
import { ApiError } from '@/lib/api-client'
import { homePathForRole } from '@/lib/roles'
import { setAuthSession, useAuth } from '@/stores/auth-store'

const DEMO_PASSWORD = 'Demo1234!'

const DEMO_ROLES = [
  {
    id: 'owner',
    label: 'Owner',
    email: 'owner@renko.demo',
    blurb: 'Full store admin, billing, staff',
  },
  {
    id: 'cashier',
    label: 'Cashier',
    email: 'cashier@renko.demo',
    blurb: 'POS sell screen and shifts',
  },
  {
    id: 'manager',
    label: 'Manager',
    email: 'manager@renko.demo',
    blurb: 'Back office without ownership',
  },
  {
    id: 'user',
    label: 'Simple user',
    email: 'user@renko.demo',
    blurb: 'Limited workspace access only',
  },
] as const

type DemoRoleId = (typeof DEMO_ROLES)[number]['id']

export function LoginPage() {
  const { token, user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from

  const [selectedRole, setSelectedRole] = useState<DemoRoleId | null>(null)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (token) {
    return <Navigate to={from || homePathForRole(user?.role)} replace />
  }

  function selectRole(roleId: DemoRoleId) {
    const role = DEMO_ROLES.find((r) => r.id === roleId)
    if (!role) return
    setSelectedRole(roleId)
    setEmail(role.email)
    setPassword(DEMO_PASSWORD)
    setError(null)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const response = await login({ email, password })
      if (!response.jwt) {
        throw new Error(response.message || 'Login did not return a token')
      }
      const authUser = response.user
        ? {
            id: Number(response.user.id),
            email: String(response.user.email ?? ''),
            fullName: String(response.user.fullName ?? ''),
            role: String(response.user.role ?? ''),
            storeId: response.user.storeId ?? null,
          }
        : null
      setAuthSession(response.jwt, authUser)
      navigate(from || homePathForRole(authUser?.role), { replace: true })
    } catch (err) {
      setError(err instanceof ApiError || err instanceof Error ? err.message : 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  const selected = DEMO_ROLES.find((r) => r.id === selectedRole)

  return (
    <div className="auth-page">
      <form className="auth-card auth-card-wide" onSubmit={handleSubmit}>
        <div className="auth-card-header">
          <Link to="/" className="app-brand-mark" aria-label="Renko home">
            R
          </Link>
          <h1>Sign in</h1>
          <p>Choose a demo role, or enter any account</p>
        </div>

        {error ? <div className="app-alert error">{error}</div> : null}

        <fieldset className="demo-role-picker">
          <legend className="field-label">Enter as</legend>
          <div className="demo-role-grid" role="radiogroup" aria-label="Demo role">
            {DEMO_ROLES.map((role) => {
              const active = selectedRole === role.id
              return (
                <button
                  key={role.id}
                  type="button"
                  role="radio"
                  aria-checked={active}
                  className={`demo-role-card${active ? ' active' : ''}`}
                  onClick={() => selectRole(role.id)}
                >
                  <strong>{role.label}</strong>
                  <span>{role.blurb}</span>
                </button>
              )
            })}
          </div>
          {selected ? (
            <p className="demo-role-hint muted">
              Using <code>{selected.email}</code> · password <code>{DEMO_PASSWORD}</code>
            </p>
          ) : (
            <p className="demo-role-hint muted">Select a role to auto-fill demo credentials.</p>
          )}
        </fieldset>

        <label className="field">
          <span className="field-label">Email</span>
          <input
            type="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value)
              setSelectedRole(null)
            }}
            required
            autoComplete="email"
          />
        </label>
        <label className="field">
          <span className="field-label">Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
          />
        </label>
        <button type="submit" className="btn btn-block" disabled={loading || !email || !password}>
          {loading
            ? 'Signing in…'
            : selected
              ? `Continue as ${selected.label}`
              : 'Sign in'}
        </button>
        <p className="auth-footer">
          New owner? <Link to="/signup">Create an account</Link>
          {' · '}
          <Link to="/">Home</Link>
        </p>
      </form>
    </div>
  )
}
