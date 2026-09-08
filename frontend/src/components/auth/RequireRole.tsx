import { Navigate, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { homePathForRole, isCashierRole } from '@/lib/roles'
import { useAuth } from '@/stores/auth-store'

type Props = {
  children: ReactNode
  roles: string[]
  /** Where unauthorized roles are sent when blocked */
  cashierFallback?: string
}

export function RequireRole({ children, roles, cashierFallback }: Props) {
  const { token, user } = useAuth()
  const location = useLocation()

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  const role = user?.role
  if (!role || !roles.includes(role)) {
    const fallback = cashierFallback ?? homePathForRole(role)
    if (isCashierRole(role) || role === 'USER' || location.pathname.startsWith('/admin')) {
      return <Navigate to={fallback} replace />
    }
    return <Navigate to={fallback} replace />
  }

  return <>{children}</>
}
