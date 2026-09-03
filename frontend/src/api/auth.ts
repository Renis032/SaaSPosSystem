import { apiClient } from '@/lib/api-client'

export type LoginRequest = {
  email: string
  password: string
}

export type SignupRequest = {
  fullName: string
  email: string
  password: string
}

export type AuthResponse = {
  jwt?: string
  message?: string
  user?: {
    id?: number
    email?: string
    fullName?: string
    role?: string
  }
}

export function login(payload: LoginRequest) {
  return apiClient<AuthResponse>('/auth/login', {
    method: 'POST',
    body: payload,
  })
}

export function signup(payload: SignupRequest) {
  return apiClient<AuthResponse>('/auth/signup', {
    method: 'POST',
    body: payload,
  })
}
