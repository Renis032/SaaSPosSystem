import { useSyncExternalStore } from 'react'

const BRANCH_KEY = 'renko_active_branch'

export type ActiveBranch = {
  id: number
  name: string
  address?: string
  phone?: string
}

type Snapshot = ActiveBranch | null

let cached: Snapshot | undefined
const listeners = new Set<() => void>()

function read(): Snapshot {
  const raw = localStorage.getItem(BRANCH_KEY)
  if (!raw) {
    cached = null
    return null
  }
  try {
    const parsed = JSON.parse(raw) as ActiveBranch
    if (!parsed?.id) {
      cached = null
      return null
    }
    cached = parsed
    return parsed
  } catch {
    cached = null
    return null
  }
}

function emit() {
  cached = undefined
  listeners.forEach((l) => l())
}

function subscribe(listener: () => void) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export function getActiveBranch(): Snapshot {
  return read()
}

export function setActiveBranch(branch: ActiveBranch | null) {
  if (branch) {
    localStorage.setItem(BRANCH_KEY, JSON.stringify(branch))
  } else {
    localStorage.removeItem(BRANCH_KEY)
  }
  emit()
}

export function clearActiveBranch() {
  setActiveBranch(null)
}

export function useActiveBranch(): Snapshot {
  return useSyncExternalStore(
    subscribe,
    () => (cached !== undefined ? cached : read()),
    () => null,
  )
}
