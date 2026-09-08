import { useCallback, useEffect, useState } from 'react'
import { getAdminStore, getEmployeeStore } from '@/api/stores'
import { ApiError } from '@/lib/api-client'
import { isAdminRole } from '@/lib/roles'
import { setAuthUser, useAuth } from '@/stores/auth-store'

export function useStoreId() {
  const { user } = useAuth()
  const [storeId, setStoreId] = useState<number | null>(user?.storeId ?? null)
  const [loading, setLoading] = useState(!user?.storeId)
  const [error, setError] = useState<string | null>(null)

  const resolve = useCallback(async () => {
    if (user?.storeId) {
      setStoreId(user.storeId)
      setLoading(false)
      setError(null)
      return user.storeId
    }

    if (!user) {
      setStoreId(null)
      setLoading(false)
      return null
    }

    setLoading(true)
    setError(null)
    try {
      const store = isAdminRole(user.role)
        ? await getAdminStore().catch(async (err) => {
            if (err instanceof ApiError) {
              return getEmployeeStore()
            }
            throw err
          })
        : await getEmployeeStore().catch(async () => getAdminStore())

      if (store?.id) {
        setStoreId(store.id)
        setAuthUser({ ...user, storeId: store.id })
        return store.id
      }
      setStoreId(null)
      setError('No store linked to this account. Create a store in Admin first.')
      return null
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Could not resolve store'
      setError(message)
      setStoreId(null)
      return null
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => {
    void resolve()
  }, [resolve])

  return { storeId, loading, error, refresh: resolve }
}
