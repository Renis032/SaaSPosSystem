import { useId, useState } from 'react'

type PasswordFieldProps = {
  label?: string
  value: string
  onChange: (value: string) => void
  required?: boolean
  minLength?: number
  autoComplete?: string
  id?: string
}

export function PasswordField({
  label = 'Password',
  value,
  onChange,
  required = true,
  minLength,
  autoComplete = 'current-password',
  id,
}: PasswordFieldProps) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const toggleId = `${inputId}-show`
  const [visible, setVisible] = useState(false)

  return (
    <div className="field password-field">
      <label htmlFor={inputId}>
        <span className="field-label">
          {label}
          {required ? <span className="field-required"> *</span> : null}
        </span>
      </label>
      <div className="password-field-row">
        <input
          id={inputId}
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          required={required}
          minLength={minLength}
          autoComplete={autoComplete}
        />
      </div>
      <label className="password-show-option" htmlFor={toggleId}>
        <input
          id={toggleId}
          type="checkbox"
          checked={visible}
          onChange={(e) => setVisible(e.target.checked)}
        />
        <span>Show password</span>
      </label>
    </div>
  )
}
