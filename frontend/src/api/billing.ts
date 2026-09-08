import { ApiError, apiClient } from '@/lib/api-client'
import type { Subscription } from '@/types/models'

export type ActivateSubscriptionBody = {
  storeId: number
  plan: 'STARTER' | 'PRO'
}

export async function getSubscription(storeId: number): Promise<Subscription> {
  try {
    return await apiClient<Subscription>(`/api/billing/subscription/${storeId}`)
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return {
        storeId,
        message: 'Subscription endpoint not available yet (404). Billing activation may not be wired on this server.',
      }
    }
    throw error
  }
}

export async function activateSubscription(body: ActivateSubscriptionBody): Promise<Subscription> {
  try {
    return await apiClient<Subscription>('/api/billing/subscription/activate', {
      method: 'POST',
      body,
    })
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return {
        storeId: body.storeId,
        plan: body.plan,
        message: 'Subscription activate endpoint not available yet (404).',
      }
    }
    throw error
  }
}

export type CheckoutSubscriptionBody = {
  storeId: number
  plan: 'STARTER' | 'PRO'
  successUrl?: string
  cancelUrl?: string
}

export type CheckoutSubscriptionResult = {
  mode?: string
  url?: string
  sessionId?: string
  message?: string
  storeId?: string
  plan?: string
  error?: string
}

export function checkoutSubscription(body: CheckoutSubscriptionBody) {
  return apiClient<CheckoutSubscriptionResult>('/api/billing/subscription/checkout', {
    method: 'POST',
    body,
  })
}
