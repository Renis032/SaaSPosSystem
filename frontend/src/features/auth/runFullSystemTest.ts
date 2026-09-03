import { apiClient, ApiError } from '@/lib/api-client'
import { clearAuthToken, setAuthToken } from '@/stores/auth-store'

type AuthPayload = {
  jwt?: string
  message?: string
  user?: { id?: number; email?: string; fullName?: string; role?: string; storeId?: number }
}

type StepResult = {
  step: string
  ok: boolean
  data?: unknown
  error?: string
}

export type SystemSetupResult = {
  success: boolean
  credentials: {
    ownerEmail: string
    cashierEmail: string
    password: string
  }
  ids: Record<string, number | string | null>
  steps: StepResult[]
}

async function step<T>(
  name: string,
  steps: StepResult[],
  action: () => Promise<T>,
): Promise<T> {
  try {
    const data = await action()
    steps.push({ step: name, ok: true, data })
    return data
  } catch (err) {
    const message =
      err instanceof ApiError
        ? err.message
        : err instanceof Error
          ? err.message
          : 'Unknown error'
    steps.push({
      step: name,
      ok: false,
      error: message,
      data: err instanceof ApiError ? err.body : undefined,
    })
    throw err
  }
}

/**
 * End-to-end happy path that builds a usable POS system:
 * OWNER → store (+contact) → branch → category → product → inventory
 * → customer → cashier → shift → cash order → receipt → refund → end shift
 */
export async function runFullSystemTest(): Promise<SystemSetupResult> {
  const stamp = Date.now()
  const password = 'Test1234!'
  const ownerEmail = `owner_${stamp}@renko.test`
  const cashierEmail = `cashier_${stamp}@renko.test`
  const steps: StepResult[] = []
  const ids: Record<string, number | string | null> = {
    ownerUserId: null,
    cashierUserId: null,
    storeId: null,
    branchId: null,
    categoryId: null,
    productId: null,
    inventoryId: null,
    customerId: null,
    shiftId: null,
    orderId: null,
    receiptNumber: null,
    refundId: null,
  }

  try {
    clearAuthToken()

    const signup = await step('1. Signup OWNER', steps, () =>
      apiClient<AuthPayload>('/auth/signup', {
        method: 'POST',
        auth: false,
        token: null,
        body: {
          fullName: 'Test Owner',
          email: ownerEmail,
          password,
          phoneNumber: '0888000001',
          role: 'OWNER',
        },
      }),
    )
    if (!signup.jwt) throw new Error('Signup did not return a JWT')
    setAuthToken(signup.jwt)
    ids.ownerUserId = signup.user?.id ?? null

    const store = await step('2. Create store', steps, () =>
      apiClient<{ id: number }>('/api/stores', {
        method: 'POST',
        body: {
          brandName: `Renko Test Store ${stamp}`,
          description: 'Auto-created by TEST button',
          storeType: 'RETAIL',
          contact: {
            email: ownerEmail,
            phone: '0888000001',
            address: '1 Test Street',
          },
        },
      }),
    )
    ids.storeId = store.id

    const branch = await step('3. Create branch', steps, () =>
      apiClient<{ id: number }>('/api/branches', {
        method: 'POST',
        body: {
          name: 'Main Branch',
          address: '1 Test Street',
          phone: '0888000002',
          email: `branch_${stamp}@renko.test`,
          storeId: store.id,
          openTime: '09:00:00',
          closeTime: '21:00:00',
          workdays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'],
        },
      }),
    )
    ids.branchId = branch.id

    const category = await step('4. Create category', steps, () =>
      apiClient<{ id: number }>('/api/categories', {
        method: 'POST',
        body: { name: 'Beverages', storeId: store.id },
      }),
    )
    ids.categoryId = category.id

    const product = await step('5. Create product', steps, () =>
      apiClient<{ id: number }>('/api/products', {
        method: 'POST',
        body: {
          name: 'Test Coffee',
          sku: `SKU-${stamp}`,
          description: 'Demo product from TEST flow',
          brand: 'Renko',
          imageUrl: '',
          maxRetailPrice: 12.5,
          sellingPrice: 9.99,
          storeId: store.id,
          categoryId: category.id,
        },
      }),
    )
    ids.productId = product.id

    const inventory = await step('6. Create inventory', steps, () =>
      apiClient<{ id: number }>('/api/inventories', {
        method: 'POST',
        body: {
          storeId: store.id,
          productId: product.id,
          quantity: 100,
          lowStockThreshold: 10,
        },
      }),
    )
    ids.inventoryId = inventory.id

    const customer = await step('7. Create customer', steps, () =>
      apiClient<{ id: number }>('/api/customers', {
        method: 'POST',
        body: {
          fullName: 'Walk-in Guest',
          email: `guest_${stamp}@renko.test`,
          phone: '0888111222',
        },
      }),
    )
    ids.customerId = customer.id

    const cashier = await step('8. Create CASHIER employee', steps, () =>
      apiClient<{ id: number }>(`/api/employees/store/${store.id}`, {
        method: 'POST',
        body: {
          fullName: 'Test Cashier',
          email: cashierEmail,
          password,
          phoneNumber: '0888000003',
          role: 'CASHIER',
        },
      }),
    )
    ids.cashierUserId = cashier.id

    const cashierLogin = await step('9. Login as CASHIER', steps, () =>
      apiClient<AuthPayload>('/auth/login', {
        method: 'POST',
        auth: false,
        token: null,
        body: { email: cashierEmail, password },
      }),
    )
    if (!cashierLogin.jwt) throw new Error('Cashier login did not return a JWT')
    setAuthToken(cashierLogin.jwt)

    const shift = await step('10. Start shift', steps, () =>
      apiClient<{ id: number }>('/api/shift-report/start', { method: 'POST' }),
    )
    ids.shiftId = shift.id

    const order = await step('11. Create CASH order', steps, () =>
      apiClient<{ id: number }>('/api/orders', {
        method: 'POST',
        body: {
          storeId: store.id,
          customerId: customer.id,
          paymentType: 'CASH',
          items: [{ productId: product.id, quantity: 2 }],
        },
      }),
    )
    ids.orderId = order.id

    const receipt = await step('12. Get receipt', steps, () =>
      apiClient<{ receiptNumber?: string }>(`/api/orders/${order.id}/receipt`),
    )
    ids.receiptNumber = receipt.receiptNumber ?? null

    const refund = await step('13. Create refund', steps, () =>
      apiClient<{ id: number }>('/api/refunds', {
        method: 'POST',
        body: {
          orderId: order.id,
          reason: 'TEST flow demo refund',
          amount: 9.99,
          paymentType: 'CASH',
          shiftReportId: shift.id,
        },
      }),
    )
    ids.refundId = refund.id

    await step('14. End shift', steps, () =>
      apiClient('/api/shift-report/end', { method: 'PATCH' }),
    )

    const ownerLogin = await step('15. Login back as OWNER', steps, () =>
      apiClient<AuthPayload>('/auth/login', {
        method: 'POST',
        auth: false,
        token: null,
        body: { email: ownerEmail, password },
      }),
    )
    if (ownerLogin.jwt) setAuthToken(ownerLogin.jwt)

    return {
      success: true,
      credentials: { ownerEmail, cashierEmail, password },
      ids,
      steps,
    }
  } catch {
    return {
      success: false,
      credentials: { ownerEmail, cashierEmail, password },
      ids,
      steps,
    }
  }
}
