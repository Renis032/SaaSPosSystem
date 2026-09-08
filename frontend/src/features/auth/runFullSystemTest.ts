import { apiClient, ApiError } from '@/lib/api-client'
import { clearAuthSession, setAuthSession } from '@/stores/auth-store'
import type { AuthUser } from '@/types/models'

type AuthPayload = {
  jwt?: string
  message?: string
  user?: AuthUser
}

function toAuthUser(user?: AuthPayload['user']): AuthUser | null {
  if (!user?.id) return null
  return {
    id: Number(user.id),
    email: String(user.email ?? ''),
    fullName: String(user.fullName ?? ''),
    role: String(user.role ?? ''),
    storeId: user.storeId ?? null,
  }
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
    managerEmail: string
    password: string
  }
  ids: Record<string, number | string | null | number[] | string[]>
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

type IdEntity = { id: number }
type OrderEntity = { id: number; totalAmount?: number }

/**
 * Seeds a full demo POS dataset with a few of each core entity:
 * store, branches, categories, products, inventory, customers, employees,
 * shift, orders, receipts, refunds, billing, reports/audit.
 */
export async function runFullSystemTest(): Promise<SystemSetupResult> {
  const stamp = Date.now()
  const password = 'Test1234!'
  const ownerEmail = `owner_${stamp}@renko.test`
  const cashierEmail = `cashier_${stamp}@renko.test`
  const managerEmail = `manager_${stamp}@renko.test`
  const branchManagerEmail = `bmanager_${stamp}@renko.test`
  const steps: StepResult[] = []
  const ids: Record<string, number | string | null | number[] | string[]> = {
    ownerUserId: null,
    cashierUserId: null,
    storeManagerUserId: null,
    branchManagerUserId: null,
    storeId: null,
    branchIds: [],
    categoryIds: [],
    productIds: [],
    inventoryIds: [],
    customerIds: [],
    shiftId: null,
    orderIds: [],
    receiptNumbers: [],
    refundIds: [],
  }

  try {
    clearAuthSession()

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
    setAuthSession(signup.jwt, toAuthUser(signup.user))
    ids.ownerUserId = signup.user?.id ?? null

    const store = await step('2. Create store', steps, () =>
      apiClient<IdEntity>('/api/stores', {
        method: 'POST',
        body: {
          brandName: `Renko Test Store ${stamp}`,
          description: 'Auto-created by TEST button with sample catalog and sales',
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

    const branchDefs = [
      {
        name: 'Main Branch',
        address: '1 Test Street',
        phone: '0888000002',
        email: `main_${stamp}@renko.test`,
      },
      {
        name: 'Downtown Branch',
        address: '22 Market Ave',
        phone: '0888000012',
        email: `downtown_${stamp}@renko.test`,
      },
      {
        name: 'Airport Kiosk',
        address: 'Terminal B Gate 4',
        phone: '0888000022',
        email: `airport_${stamp}@renko.test`,
      },
    ]

    const branchIds: number[] = []
    for (let i = 0; i < branchDefs.length; i++) {
      const def = branchDefs[i]
      const branch = await step(`3.${i + 1} Create branch: ${def.name}`, steps, () =>
        apiClient<IdEntity>('/api/branches', {
          method: 'POST',
          body: {
            ...def,
            storeId: store.id,
            openTime: '09:00:00',
            closeTime: '21:00:00',
            workdays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'],
          },
        }),
      )
      branchIds.push(branch.id)
    }
    ids.branchIds = branchIds

    const categoryNames = ['Beverages', 'Snacks', 'Merchandise']
    const categoryIds: number[] = []
    for (let i = 0; i < categoryNames.length; i++) {
      const name = categoryNames[i]
      const category = await step(`4.${i + 1} Create category: ${name}`, steps, () =>
        apiClient<IdEntity>('/api/categories', {
          method: 'POST',
          body: { name, storeId: store.id },
        }),
      )
      categoryIds.push(category.id)
    }
    ids.categoryIds = categoryIds

    const productDefs = [
      {
        name: 'House Coffee',
        sku: `COF-${stamp}`,
        brand: 'Renko',
        maxRetailPrice: 12.5,
        sellingPrice: 9.99,
        categoryIndex: 0,
        quantity: 80,
        lowStockThreshold: 10,
      },
      {
        name: 'Iced Tea',
        sku: `TEA-${stamp}`,
        brand: 'Renko',
        maxRetailPrice: 8.5,
        sellingPrice: 6.5,
        categoryIndex: 0,
        quantity: 60,
        lowStockThreshold: 8,
      },
      {
        name: 'Trail Mix',
        sku: `SNK-${stamp}`,
        brand: 'TrailCo',
        maxRetailPrice: 7,
        sellingPrice: 5.25,
        categoryIndex: 1,
        quantity: 40,
        lowStockThreshold: 5,
      },
      {
        name: 'Branded Mug',
        sku: `MUG-${stamp}`,
        brand: 'Renko',
        maxRetailPrice: 22,
        sellingPrice: 18,
        categoryIndex: 2,
        quantity: 12,
        lowStockThreshold: 15, // intentionally low-stock for alerts
      },
    ]

    const productIds: number[] = []
    const inventoryIds: number[] = []
    for (let i = 0; i < productDefs.length; i++) {
      const def = productDefs[i]
      const product = await step(`5.${i + 1} Create product: ${def.name}`, steps, () =>
        apiClient<IdEntity>('/api/products', {
          method: 'POST',
          body: {
            name: def.name,
            sku: def.sku,
            description: `Demo product from TEST flow (${def.name})`,
            brand: def.brand,
            imageUrl: '',
            maxRetailPrice: def.maxRetailPrice,
            sellingPrice: def.sellingPrice,
            storeId: store.id,
            categoryId: categoryIds[def.categoryIndex],
          },
        }),
      )
      productIds.push(product.id)

      const inventory = await step(`6.${i + 1} Create inventory: ${def.name}`, steps, () =>
        apiClient<IdEntity>('/api/inventories', {
          method: 'POST',
          body: {
            storeId: store.id,
            productId: product.id,
            quantity: def.quantity,
            lowStockThreshold: def.lowStockThreshold,
          },
        }),
      )
      inventoryIds.push(inventory.id)
    }
    ids.productIds = productIds
    ids.inventoryIds = inventoryIds

    await step('6.5 Adjust stock on Trail Mix (+5)', steps, () =>
      apiClient(`/api/inventories/${inventoryIds[2]}/adjust?delta=5&reason=TEST%20restock`, {
        method: 'POST',
      }),
    )

    const customerDefs = [
      { fullName: 'Walk-in Guest', email: `guest_${stamp}@renko.test`, phone: '0888111222' },
      { fullName: 'Ada Lovelace', email: `ada_${stamp}@renko.test`, phone: '0888222333' },
      { fullName: 'Alan Turing', email: `alan_${stamp}@renko.test`, phone: '0888333444' },
    ]
    const customerIds: number[] = []
    for (let i = 0; i < customerDefs.length; i++) {
      const def = customerDefs[i]
      const customer = await step(`7.${i + 1} Create customer: ${def.fullName}`, steps, () =>
        apiClient<IdEntity>('/api/customers', {
          method: 'POST',
          body: def,
        }),
      )
      customerIds.push(customer.id)
    }
    ids.customerIds = customerIds

    const cashier = await step('8.1 Create CASHIER employee', steps, () =>
      apiClient<IdEntity>(`/api/employees/store/${store.id}`, {
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

    const storeManager = await step('8.2 Create STORE_MANAGER employee', steps, () =>
      apiClient<IdEntity>(`/api/employees/store/${store.id}`, {
        method: 'POST',
        body: {
          fullName: 'Test Store Manager',
          email: managerEmail,
          password,
          phoneNumber: '0888000004',
          role: 'STORE_MANAGER',
        },
      }),
    )
    ids.storeManagerUserId = storeManager.id

    const branchManager = await step('8.3 Create BRANCH_MANAGER employee', steps, () =>
      apiClient<IdEntity>(`/api/employees/store/${store.id}`, {
        method: 'POST',
        body: {
          fullName: 'Test Branch Manager',
          email: branchManagerEmail,
          password,
          phoneNumber: '0888000005',
          role: 'BRANCH_MANAGER',
        },
      }),
    )
    ids.branchManagerUserId = branchManager.id

    await step('9. Activate subscription (trial/local)', steps, () =>
      apiClient('/api/billing/subscription/activate', {
        method: 'POST',
        body: { storeId: store.id, plan: 'STARTER' },
      }),
    )

    await step('9.1 Get subscription', steps, () =>
      apiClient(`/api/billing/subscription/${store.id}`),
    )

    const cashierLogin = await step('10. Login as CASHIER', steps, () =>
      apiClient<AuthPayload>('/auth/login', {
        method: 'POST',
        auth: false,
        token: null,
        body: { email: cashierEmail, password },
      }),
    )
    if (!cashierLogin.jwt) throw new Error('Cashier login did not return a JWT')
    setAuthSession(cashierLogin.jwt, toAuthUser(cashierLogin.user))

    const shift = await step('11. Start shift', steps, () =>
      apiClient<IdEntity>('/api/shift-report/start', { method: 'POST' }),
    )
    ids.shiftId = shift.id

    const orderDefs = [
      {
        label: 'coffee x2 for guest (CASH)',
        customerId: customerIds[0],
        paymentType: 'CASH',
        items: [{ productId: productIds[0], quantity: 2 }],
      },
      {
        label: 'tea + trail mix for Ada (CASH)',
        customerId: customerIds[1],
        paymentType: 'CASH',
        items: [
          { productId: productIds[1], quantity: 1 },
          { productId: productIds[2], quantity: 2 },
        ],
      },
      {
        label: 'mug for Alan (UPI)',
        customerId: customerIds[2],
        paymentType: 'UPI',
        items: [{ productId: productIds[3], quantity: 1 }],
      },
      {
        label: 'coffee + tea walk-up (CASH)',
        customerId: customerIds[0],
        paymentType: 'CASH',
        items: [
          { productId: productIds[0], quantity: 1 },
          { productId: productIds[1], quantity: 1 },
        ],
      },
    ]

    const orderIds: number[] = []
    for (let i = 0; i < orderDefs.length; i++) {
      const def = orderDefs[i]
      const order = await step(`12.${i + 1} Create order: ${def.label}`, steps, () =>
        apiClient<OrderEntity>('/api/orders', {
          method: 'POST',
          body: {
            storeId: store.id,
            customerId: def.customerId,
            paymentType: def.paymentType,
            items: def.items,
          },
        }),
      )
      orderIds.push(order.id)
    }
    ids.orderIds = orderIds

    const receiptNumbers: string[] = []
    for (let i = 0; i < Math.min(3, orderIds.length); i++) {
      const receipt = await step(`13.${i + 1} Get receipt for order #${orderIds[i]}`, steps, () =>
        apiClient<{ receiptNumber?: string }>(`/api/orders/${orderIds[i]}/receipt`),
      )
      if (receipt.receiptNumber) receiptNumbers.push(receipt.receiptNumber)
    }
    ids.receiptNumbers = receiptNumbers

    const refundDefs = [
      {
        orderIndex: 0,
        reason: 'Customer changed mind (partial)',
        amount: productDefs[0].sellingPrice,
      },
      {
        orderIndex: 2,
        reason: 'Damaged mug returned',
        amount: productDefs[3].sellingPrice,
      },
    ]

    const refundIds: number[] = []
    for (let i = 0; i < refundDefs.length; i++) {
      const def = refundDefs[i]
      const refund = await step(
        `14.${i + 1} Create refund for order #${orderIds[def.orderIndex]}`,
        steps,
        () =>
          apiClient<IdEntity>('/api/refunds', {
            method: 'POST',
            body: {
              orderId: orderIds[def.orderIndex],
              reason: def.reason,
              amount: def.amount,
              paymentType: orderDefs[def.orderIndex].paymentType,
              shiftReportId: shift.id,
            },
          }),
      )
      refundIds.push(refund.id)
    }
    ids.refundIds = refundIds

    await step('15. End shift', steps, () =>
      apiClient('/api/shift-report/end', { method: 'PATCH' }),
    )

    const ownerLogin = await step('16. Login back as OWNER', steps, () =>
      apiClient<AuthPayload>('/auth/login', {
        method: 'POST',
        auth: false,
        token: null,
        body: { email: ownerEmail, password },
      }),
    )
    if (ownerLogin.jwt) setAuthSession(ownerLogin.jwt, toAuthUser(ownerLogin.user))

    await step('17. Fetch store report', steps, () =>
      apiClient(`/api/reports/store/${store.id}`),
    )

    await step('17.1 Fetch audit log', steps, () =>
      apiClient(`/api/reports/store/${store.id}/audit`),
    )

    await step('17.2 List low-stock inventory', steps, () =>
      apiClient(`/api/inventories/store/${store.id}/low-stock`),
    )

    return {
      success: true,
      credentials: { ownerEmail, cashierEmail, managerEmail, password },
      ids,
      steps,
    }
  } catch {
    return {
      success: false,
      credentials: { ownerEmail, cashierEmail, managerEmail, password },
      ids,
      steps,
    }
  }
}
