const ADMIN_ROLES = new Set(['OWNER', 'STORE_MANAGER', 'BRANCH_MANAGER', 'ADMIN'])

export function isAdminRole(role?: string | null): boolean {
  return Boolean(role && ADMIN_ROLES.has(role))
}

export function isCashierRole(role?: string | null): boolean {
  return role === 'CASHIER'
}

export function isSimpleUserRole(role?: string | null): boolean {
  return role === 'USER'
}

export function homePathForRole(role?: string | null): string {
  if (isCashierRole(role)) return '/pos'
  if (isAdminRole(role)) return '/admin'
  if (isSimpleUserRole(role)) return '/workspace'
  return '/workspace'
}

export function canAccessAdmin(role?: string | null, _storeId?: number | null): boolean {
  return isAdminRole(role)
}

export function canAccessPos(role?: string | null, _storeId?: number | null): boolean {
  return isCashierRole(role) || isAdminRole(role)
}
