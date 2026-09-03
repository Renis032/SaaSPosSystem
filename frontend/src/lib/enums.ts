export const USER_ROLES = [
  'USER',
  'ADMIN',
  'CASHIER',
  'BRANCH_MANAGER',
  'STORE_MANAGER',
  'OWNER',
] as const

export const STORE_STATUSES = ['PENDING', 'ACTIVE', 'BLOCKED'] as const

export const PAYMENT_TYPES = ['CASH', 'UPI', 'CARD'] as const

export const ORDER_STATUSES = ['PENDING', 'COMPLETED'] as const

export function toOptions(values: readonly string[]) {
  return values.map((value) => ({ value, label: value }))
}
