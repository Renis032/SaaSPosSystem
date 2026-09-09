import { FormEvent, useEffect, useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { login } from '@/api/auth'
import { PasswordField } from '@/components/ui/PasswordField'
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
    id: 'manager',
    label: 'Manager',
    email: 'manager@renko.demo',
    blurb: 'Back office without ownership',
  },
  {
    id: 'cashier',
    label: 'Cashier',
    email: 'cashier@renko.demo',
    blurb: 'POS sell screen and shifts',
  },
] as const

type DemoRoleId = (typeof DEMO_ROLES)[number]['id']

export function LoginPage() {
  const { token, user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string; demoRole?: DemoRoleId } | null)?.from
  const demoRoleFromNav = (location.state as { demoRole?: DemoRoleId } | null)?.demoRole

  const [selectedRole, setSelectedRole] = useState<DemoRoleId | null>(demoRoleFromNav ?? null)
  const [email, setEmail] = useState(() => {
    const role = DEMO_ROLES.find((r) => r.id === demoRoleFromNav)
    return role?.email ?? ''
  })
  const [password, setPassword] = useState(() => (demoRoleFromNav ? DEMO_PASSWORD : ''))
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!demoRoleFromNav) return
    const role = DEMO_ROLES.find((r) => r.id === demoRoleFromNav)
    if (!role) return
    setSelectedRole(demoRoleFromNav)
    setEmail(role.email)
    setPassword(DEMO_PASSWORD)
  }, [demoRoleFromNav])

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

    if (!email.trim()) {
      setError('Email is required')
      return
    }
    if (!password) {
      setError('Password is required')
      return
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }

    setLoading(true)
    try {
      const response = await login({ email: email.trim(), password })
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
      <form className="auth-card auth-card-wide" onSubmit={handleSubmit} noValidate>
        <div className="auth-card-header">
          <Link to="/" className="app-brand-mark" aria-label="Renko home">
            R
          </Link>
          <h1>Sign in</h1>
          <p>Choose Owner, Manager, or Cashier</p>
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
              Using <code>{selected.email}</code>
            </p>
          ) : (
            <p className="demo-role-hint muted">Select a role to auto-fill demo credentials.</p>
          )}
        </fieldset>

        <label className="field">
          <span className="field-label">
            Email<span className="field-required"> *</span>
          </span>
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
        <PasswordField
          value={password}
          onChange={(value) => {
            setPassword(value)
            setSelectedRole(null)
          }}
          required
          minLength={6}
          autoComplete="current-password"
        />
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
