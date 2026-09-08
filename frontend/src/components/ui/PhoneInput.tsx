import {
  COUNTRY_CODES,
  DEFAULT_COUNTRY_DIAL,
  digitsOnly,
} from '@/lib/country-codes'

type PhoneInputProps = {
  label?: string
  countryDial: string
  phoneDigits: string
  onCountryDialChange: (dial: string) => void
  onPhoneDigitsChange: (digits: string) => void
  required?: boolean
  id?: string
}

export function PhoneInput({
  label = 'Phone',
  countryDial,
  phoneDigits,
  onCountryDialChange,
  onPhoneDigitsChange,
  required = false,
  id = 'phone',
}: PhoneInputProps) {
  return (
    <div className="field phone-field">
      <span className="field-label">{label}</span>
      <div className="phone-input-row">
        <select
          aria-label="Country code"
          className="phone-country"
          value={countryDial || DEFAULT_COUNTRY_DIAL}
          onChange={(e) => onCountryDialChange(e.target.value)}
        >
          {COUNTRY_CODES.map((country) => (
            <option key={`${country.code}-${country.dial}`} value={country.dial}>
              {country.label}
            </option>
          ))}
        </select>
        <input
          id={id}
          type="tel"
          inputMode="numeric"
          pattern="[0-9]*"
          autoComplete="tel-national"
          placeholder="Phone number"
          value={phoneDigits}
          required={required}
          onChange={(e) => onPhoneDigitsChange(digitsOnly(e.target.value))}
        />
      </div>
    </div>
  )
}
