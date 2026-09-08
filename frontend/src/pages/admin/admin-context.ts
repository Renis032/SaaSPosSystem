import { useOutletContext } from 'react-router-dom'

export type AdminOutletContext = {
  storeId: number
}

export function useAdminContext() {
  return useOutletContext<AdminOutletContext>()
}
