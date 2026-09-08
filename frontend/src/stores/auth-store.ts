import { useSyncExternalStore } from 'react'
import type { AuthUser } from '@/types/models'

const TOKEN_KEY = 'renko_pos_token'
const USER_KEY = 'renko_pos_user'

type AuthSnapshot = {
  token: string | null
  user: AuthUser | null
}

let cachedSnapshot: AuthSnapshot | null = null
const listeners = new Set<() => void>()

function parseUser(raw: string | null): AuthUser | null {
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw) as AuthUser
    if (!parsed || typeof parsed !== 'object') return null
    return parsed
  } catch {
    return null
  }
}

function readSnapshot(): AuthSnapshot {
  const token = localStorage.getItem(TOKEN_KEY)
  const user = parseUser(localStorage.getItem(USER_KEY))
  if (
    cachedSnapshot &&
    cachedSnapshot.token === token &&
    JSON.stringify(cachedSnapshot.user) === JSON.stringify(user)
  ) {
    return cachedSnapshot
  }
  cachedSnapshot = { token, user }
  return cachedSnapshot
}

function emitChange() {
  cachedSnapshot = null
  listeners.forEach((listener) => listener())
}

function subscribe(listener: () => void) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export function getAuthToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function getAuthUser(): AuthUser | null {
  return parseUser(localStorage.getItem(USER_KEY))
}

export function setAuthToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
  emitChange()
}

export function setAuthUser(user: AuthUser | null): void {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(USER_KEY)
  }
  emitChange()
}

export function setAuthSession(token: string, user: AuthUser | null): void {
  localStorage.setItem(TOKEN_KEY, token)
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(USER_KEY)
  }
  emitChange()
}

export function clearAuthToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  emitChange()
}

export function clearAuthSession(): void {
  clearAuthToken()
}

export function isAuthenticated(): boolean {
  return Boolean(getAuthToken())
}

export function useAuth(): AuthSnapshot {
  return useSyncExternalStore(subscribe, readSnapshot, readSnapshot)
}
