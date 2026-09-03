import type { InputHTMLAttributes, SelectHTMLAttributes, TextareaHTMLAttributes } from 'react'

type FieldProps = {
  label: string
  hint?: string
} & (
  | ({ as?: 'input' } & InputHTMLAttributes<HTMLInputElement>)
  | ({ as: 'select'; options: { value: string; label: string }[] } & SelectHTMLAttributes<HTMLSelectElement>)
  | ({ as: 'textarea' } & TextareaHTMLAttributes<HTMLTextAreaElement>)
)

export function Field(props: FieldProps) {
  const { label, hint, as = 'input', ...rest } = props

  return (
    <label className="field">
      <span className="field-label">{label}</span>
      {as === 'select' ? (
        <select {...(rest as SelectHTMLAttributes<HTMLSelectElement>)}>
          {(props as { options: { value: string; label: string }[] }).options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      ) : as === 'textarea' ? (
        <textarea rows={4} {...(rest as TextareaHTMLAttributes<HTMLTextAreaElement>)} />
      ) : (
        <input {...(rest as InputHTMLAttributes<HTMLInputElement>)} />
      )}
      {hint ? <span className="field-hint">{hint}</span> : null}
    </label>
  )
}
