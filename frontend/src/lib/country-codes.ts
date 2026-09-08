export type CountryCode = {
  code: string
  dial: string
  label: string
}

/** Common dial codes for signup phone input. */
export const COUNTRY_CODES: CountryCode[] = [
  { code: 'BG', dial: '+359', label: 'Bulgaria (+359)' },
  { code: 'US', dial: '+1', label: 'United States (+1)' },
  { code: 'GB', dial: '+44', label: 'United Kingdom (+44)' },
  { code: 'DE', dial: '+49', label: 'Germany (+49)' },
  { code: 'FR', dial: '+33', label: 'France (+33)' },
  { code: 'IT', dial: '+39', label: 'Italy (+39)' },
  { code: 'ES', dial: '+34', label: 'Spain (+34)' },
  { code: 'NL', dial: '+31', label: 'Netherlands (+31)' },
  { code: 'BE', dial: '+32', label: 'Belgium (+32)' },
  { code: 'AT', dial: '+43', label: 'Austria (+43)' },
  { code: 'CH', dial: '+41', label: 'Switzerland (+41)' },
  { code: 'RO', dial: '+40', label: 'Romania (+40)' },
  { code: 'GR', dial: '+30', label: 'Greece (+30)' },
  { code: 'TR', dial: '+90', label: 'Turkey (+90)' },
  { code: 'PL', dial: '+48', label: 'Poland (+48)' },
  { code: 'CZ', dial: '+420', label: 'Czechia (+420)' },
  { code: 'HU', dial: '+36', label: 'Hungary (+36)' },
  { code: 'RS', dial: '+381', label: 'Serbia (+381)' },
  { code: 'MK', dial: '+389', label: 'North Macedonia (+389)' },
  { code: 'AL', dial: '+355', label: 'Albania (+355)' },
  { code: 'UA', dial: '+380', label: 'Ukraine (+380)' },
  { code: 'IN', dial: '+91', label: 'India (+91)' },
  { code: 'AU', dial: '+61', label: 'Australia (+61)' },
  { code: 'CA', dial: '+1', label: 'Canada (+1)' },
  { code: 'AE', dial: '+971', label: 'UAE (+971)' },
]

export const DEFAULT_COUNTRY_DIAL = '+359'

export function digitsOnly(value: string): string {
  return value.replace(/\D/g, '')
}

export function formatInternationalPhone(dial: string, localDigits: string): string {
  const code = dial.startsWith('+') ? dial : `+${digitsOnly(dial)}`
  return `${code}${digitsOnly(localDigits)}`
}
