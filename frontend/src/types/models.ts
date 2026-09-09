export type AuthUser = {
  id: number
  email: string
  fullName: string
  role: string
  storeId?: number | null
  phoneNumber?: string | null
}

export type Product = {
  id: number
  name: string
  sku?: string
  description?: string
  brand?: string
  imageUrl?: string
  maxRetailPrice?: number
  sellingPrice: number
  discountPercentage?: number
  categoryId?: number | null
  storeId?: number
}

export type Inventory = {
  id: number
  quantity: number
  storeId: number
  productId: number
  lowStockThreshold?: number
  lastUpdated?: string
}

export type Customer = {
  id: number
  fullName: string
  email?: string
  phone?: string
  storeEntity?: { id?: number } | null
  createdAt?: string
}

export type Employee = {
  id: number
  fullName: string
  email: string
  phoneNumber?: string
  role: string
  storeId?: number | null
}

export type OrderItem = {
  id?: number
  productId: number
  quantity: number
  price?: number
  originalPrice?: number
  discountApplied?: number
  discountPercent?: number
  orderId?: number
}

export type Order = {
  id: number
  totalAmount?: number
  subtotal?: number
  totalDiscount?: number
  taxRate?: number
  taxAmount?: number
  orderDiscountPercent?: number
  createdAt?: string
  storeId?: number
  branchId?: number | null
  customerId?: number | null
  cashierId?: number | null
  customerName?: string
  customerPhone?: string
  paymentType?: string
  items?: OrderItem[]
}

export type ReceiptItem = {
  name: string
  sku?: string
  quantity: number
  originalPrice?: number
  discountPercentage?: number
  discountAmount?: number
  finalPrice?: number
  lineTotal?: number
}

export type Receipt = {
  orderId: number
  receiptNumber?: string
  orderDate?: string
  storeName?: string
  storeAddress?: string
  storePhone?: string
  cashierName?: string
  customerName?: string
  customerPhone?: string
  items?: ReceiptItem[]
  subtotal?: number
  totalDiscount?: number
  taxRate?: number
  taxAmount?: number
  totalAmount?: number
  branchName?: string
  paymentType?: string
}

export type ShiftReport = {
  id: number
  shiftStart?: string
  shiftEnd?: string | null
  totalSales?: number
  totalRefunds?: number
  netSales?: number
  totalOrders?: number
  cashierId?: number
  storeId?: number
  branchId?: number | null
  recentOrderIds?: number[]
}

export type Refund = {
  id: number
  orderId: number
  reason?: string
  amount?: number
  shiftReportId?: number | null
  cashierId?: number | null
  cashierName?: string
  storeId?: number | null
  paymentType?: string
  createdAt?: string
  updatedAt?: string
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type Store = {
  id: number
  brandName?: string
  description?: string
  storeType?: string
  storeStatus?: string
}

export type Subscription = {
  id?: number
  storeId?: number
  plan?: string
  status?: string
  entitled?: boolean
  trialEndsAt?: string
  currentPeriodEnd?: string
  message?: string
}

export type StoreReport = {
  storeId?: number
  orderCount?: number
  grossSales?: number
  refundTotal?: number
  netSales?: number
  refundCount?: number
  lowStockCount?: number
  openShiftCount?: number
}

export type AuditLog = {
  id: number
  storeId?: number
  actorUserId?: number
  actorEmail?: string
  action: string
  entityType: string
  entityId?: string
  details?: string
  beforeState?: string
  afterState?: string
  createdAt?: string
}
