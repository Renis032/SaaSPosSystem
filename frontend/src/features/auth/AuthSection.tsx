import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'
import { toOptions, USER_ROLES } from '@/lib/enums'
import { clearAuthToken, getAuthToken, setAuthToken } from '@/stores/auth-store'

type AuthPayload = {
  jwt?: string
  message?: string
  user?: unknown
}

type AuthSectionProps = {
  onRun: <T>(action: () => Promise<T>) => Promise<T>
  onTokenChange: (token: string | null) => void
}

export function AuthSection({ onRun, onTokenChange }: AuthSectionProps) {
  const [signup, setSignup] = useState({
    fullName: '',
    email: '',
    password: '',
    phoneNumber: '',
    role: 'OWNER',
  })
  const [login, setLogin] = useState({ email: '', password: '' })

  function handleAuthSuccess(data: AuthPayload) {
    if (data.jwt) {
      setAuthToken(data.jwt)
      onTokenChange(data.jwt)
    }
    return data
  }

  return (
    <Section title="Auth" description="Signup / login. JWT is saved and sent on later /api calls.">
      <ActionRow title="Signup">
        <div className="form-grid">
          <Field label="Full name" value={signup.fullName} onChange={(e) => setSignup({ ...signup, fullName: e.target.value })} />
          <Field label="Email" type="email" value={signup.email} onChange={(e) => setSignup({ ...signup, email: e.target.value })} />
          <Field label="Password" type="password" value={signup.password} onChange={(e) => setSignup({ ...signup, password: e.target.value })} />
          <Field label="Phone" value={signup.phoneNumber} onChange={(e) => setSignup({ ...signup, phoneNumber: e.target.value })} />
          <Field
            as="select"
            label="Role"
            value={signup.role}
            options={toOptions(USER_ROLES)}
            onChange={(e) => setSignup({ ...signup, role: e.target.value })}
          />
        </div>
        <button
          type="button"
          className="btn"
          onClick={() =>
            onRun(() =>
              apiClient<AuthPayload>('/auth/signup', { method: 'POST', body: signup, auth: false }).then(
                handleAuthSuccess,
              ),
            )
          }
        >
          Signup
        </button>
      </ActionRow>

      <ActionRow title="Login">
        <div className="form-grid">
          <Field label="Email" type="email" value={login.email} onChange={(e) => setLogin({ ...login, email: e.target.value })} />
          <Field label="Password" type="password" value={login.password} onChange={(e) => setLogin({ ...login, password: e.target.value })} />
        </div>
        <button
          type="button"
          className="btn"
          onClick={() =>
            onRun(() =>
              apiClient<AuthPayload>('/auth/login', { method: 'POST', body: login, auth: false }).then(
                handleAuthSuccess,
              ),
            )
          }
        >
          Login
        </button>
        <button
          type="button"
          className="btn btn-secondary"
          onClick={() => {
            clearAuthToken()
            onTokenChange(null)
          }}
        >
          Clear token
        </button>
        <button
          type="button"
          className="btn btn-secondary"
          onClick={() => onRun(async () => ({ token: getAuthToken() }))}
        >
          Show saved token
        </button>
      </ActionRow>
    </Section>
  )
}
