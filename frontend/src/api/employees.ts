import { apiClient } from '@/lib/api-client'
import type { Employee } from '@/types/models'

export type EmployeeCreateBody = {
  fullName: string
  email: string
  password: string
  phoneNumber?: string
  role: string
}

export function listEmployeesByStore(storeId: number) {
  return apiClient<Employee[]>(`/api/employees/store/${storeId}`)
}

export function createEmployee(storeId: number, body: EmployeeCreateBody) {
  return apiClient<Employee>(`/api/employees/store/${storeId}`, {
    method: 'POST',
    body,
  })
}
