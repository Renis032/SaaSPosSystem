import { useState } from 'react'
import { Field } from '@/components/ui/Field'
import { PasswordField } from '@/components/ui/PasswordField'
import { PhoneInput } from '@/components/ui/PhoneInput'
import { ActionRow, Section } from '@/components/ui/Section'
import { apiClient } from '@/lib/api-client'
import { DEFAULT_COUNTRY_DIAL, formatInternationalPhone } from '@/lib/country-codes'
import { clearAuthSession, getAuthToken, setAuthSession } from '@/stores/auth-store'
import type { AuthUser } from '@/types/models'

type AuthPayload = {
  jwt?: string
  message?: string
  user?: AuthUser
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
    phoneDigits: '',
    role: 'OWNER',
  })
  const [countryDial, setCountryDial] = useState(DEFAULT_COUNTRY_DIAL)
  const [login, setLogin] = useState({ email: '', password: '' })

  function handleAuthSuccess(data: AuthPayload) {
    if (data.jwt) {
      const user = data.user
        ? {
            id: Number(data.user.id),
            email: String(data.user.email ?? ''),
            fullName: String(data.user.fullName ?? ''),
            role: String(data.user.role ?? ''),
            storeId: data.user.storeId ?? null,
          }
        : null
      setAuthSession(data.jwt, user)
      onTokenChange(data.jwt)
    }
    return data
  }

  return (
    <Section title="Auth" description="Signup / login. JWT is saved and sent on later /api calls.">
      <ActionRow title="Signup">
        <div className="form-grid">
          <Field
            label="Full name *"
            value={signup.fullName}
            onChange={(e) => setSignup({ ...signup, fullName: e.target.value })}
          />
          <Field
            label="Email *"
            type="email"
            value={signup.email}
            onChange={(e) => setSignup({ ...signup, email: e.target.value })}
          />
          <PasswordField
            value={signup.password}
            onChange={(password) => setSignup({ ...signup, password })}
            required
            minLength={6}
            autoComplete="new-password"
          />
          <PhoneInput
            label="Phone (optional)"
            countryDial={countryDial}
            phoneDigits={signup.phoneDigits}
            onCountryDialChange={setCountryDial}
            onPhoneDigitsChange={(phoneDigits) => setSignup({ ...signup, phoneDigits })}
          />
          <Field
            as="select"
            label="Role"
            value={signup.role}
            options={[{ value: 'OWNER', label: 'OWNER' }]}
            onChange={(e) => setSignup({ ...signup, role: e.target.value })}
          />
        </div>
        <button
          type="button"
          className="btn"
          onClick={() =>
            onRun(() =>
              apiClient<AuthPayload>('/auth/signup', {
                method: 'POST',
                body: {
                  fullName: signup.fullName,
                  email: signup.email,
                  password: signup.password,
                  phoneNumber: signup.phoneDigits
                    ? formatInternationalPhone(countryDial, signup.phoneDigits)
                    : undefined,
                  role: signup.role,
                },
                auth: false,
              }).then(handleAuthSuccess),
            )
          }
        >
          Signup
        </button>
      </ActionRow>

      <ActionRow title="Login">
        <div className="form-grid">
          <Field
            label="Email *"
            type="email"
            value={login.email}
            onChange={(e) => setLogin({ ...login, email: e.target.value })}
          />
          <PasswordField
            value={login.password}
            onChange={(password) => setLogin({ ...login, password })}
            required
            minLength={6}
            autoComplete="current-password"
          />
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
            clearAuthSession()
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
