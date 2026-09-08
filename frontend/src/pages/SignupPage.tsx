import { FormEvent, useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { signup } from '@/api/auth'
import { PhoneInput } from '@/components/ui/PhoneInput'
import { ApiError } from '@/lib/api-client'
import { DEFAULT_COUNTRY_DIAL, formatInternationalPhone } from '@/lib/country-codes'
import { homePathForRole } from '@/lib/roles'
import { setAuthSession, useAuth } from '@/stores/auth-store'

export function SignupPage() {
  const { token, user } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phoneDigits: '',
  })
  const [countryDial, setCountryDial] = useState(DEFAULT_COUNTRY_DIAL)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (token) {
    return <Navigate to={homePathForRole(user?.role)} replace />
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)

    if (!form.phoneDigits) {
      setError('Phone number is required')
      return
    }

    setLoading(true)
    try {
      const response = await signup({
        fullName: form.fullName,
        email: form.email,
        password: form.password,
        phoneNumber: formatInternationalPhone(countryDial, form.phoneDigits),
        role: 'OWNER',
      })
      if (!response.jwt) {
        throw new Error(response.message || 'Signup did not return a token')
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
      navigate(homePathForRole(authUser?.role), { replace: true })
    } catch (err) {
      setError(err instanceof ApiError || err instanceof Error ? err.message : 'Signup failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <div className="auth-card-header">
          <span className="app-brand-mark">R</span>
          <h1>Create owner account</h1>
          <p>Sign up as a store owner to manage your POS</p>
        </div>
        {error ? <div className="app-alert error">{error}</div> : null}
        <label className="field">
          <span className="field-label">Full name</span>
          <input
            value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })}
            required
            autoComplete="name"
          />
        </label>
        <label className="field">
          <span className="field-label">Email</span>
          <input
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            required
            autoComplete="email"
          />
        </label>
        <label className="field">
          <span className="field-label">Password</span>
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
            minLength={6}
            autoComplete="new-password"
          />
        </label>
        <PhoneInput
          countryDial={countryDial}
          phoneDigits={form.phoneDigits}
          onCountryDialChange={setCountryDial}
          onPhoneDigitsChange={(phoneDigits) => setForm({ ...form, phoneDigits })}
          required
        />
        <button type="submit" className="btn btn-block" disabled={loading}>
          {loading ? 'Creating…' : 'Create account'}
        </button>
        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
          {' · '}
          <Link to="/">Home</Link>
        </p>
      </form>
    </div>
  )
}
