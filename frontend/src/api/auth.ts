import { apiClient } from '@/lib/api-client'
import type { AuthUser } from '@/types/models'

export type LoginRequest = {
  email: string
  password: string
}

export type SignupRequest = {
  fullName: string
  email: string
  password: string
  phoneNumber?: string
  role: 'OWNER'
}

export type AuthResponse = {
  jwt?: string
  message?: string
  user?: AuthUser
}

export function login(payload: LoginRequest) {
  return apiClient<AuthResponse>('/auth/login', {
    method: 'POST',
    body: payload,
    auth: false,
  })
}

export function signup(payload: SignupRequest) {
  return apiClient<AuthResponse>('/auth/signup', {
    method: 'POST',
    body: payload,
    auth: false,
  })
}
