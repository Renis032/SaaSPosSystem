import { useState } from 'react'
import { ResponsePanel } from '@/components/ui/ResponsePanel'
import { AuthSection } from '@/features/auth/AuthSection'
import { runFullSystemTest } from '@/features/auth/runFullSystemTest'
import { BillingSection } from '@/features/billing/BillingSection'
import { CustomerSection } from '@/features/customers/CustomerSection'
import { EmployeeSection } from '@/features/employees/EmployeeSection'
import { UserSection } from '@/features/employees/UserSection'
import { InventorySection } from '@/features/inventory/InventorySection'
import { OrderSection } from '@/features/orders/OrderSection'
import { CategorySection } from '@/features/products/CategorySection'
import { ProductSection } from '@/features/products/ProductSection'
import { RefundSection } from '@/features/refunds/RefundSection'
import { ShiftReportSection } from '@/features/shift-reports/ShiftReportSection'
import { BranchSection } from '@/features/stores/BranchSection'
import { StoreSection } from '@/features/stores/StoreSection'
import { useApiAction } from '@/hooks/useApiAction'
import { ApiError } from '@/lib/api-client'
import { getAuthToken } from '@/stores/auth-store'

const SECTIONS = [
  'auth',
  'stores',
  'branches',
  'categories',
  'products',
  'inventory',
  'customers',
  'employees',
  'orders',
  'refunds',
  'shifts',
  'users',
  'billing',
] as const

type SectionId = (typeof SECTIONS)[number]

export function ApiPlaygroundPage() {
  const { loading, result, error, run } = useApiAction()
  const [token, setToken] = useState<string | null>(() => getAuthToken())
  const [active, setActive] = useState<SectionId>('auth')
  const [testRunning, setTestRunning] = useState(false)

  const safeRun = async <T,>(action: () => Promise<T>) => {
    try {
      return await run(action)
    } catch {
      return undefined as T
    }
  }

  const handleFullTest = async () => {
    setTestRunning(true)
    try {
      await run(async () => {
        const outcome = await runFullSystemTest()
        setToken(getAuthToken())
        if (!outcome.success) {
          const failed = outcome.steps.find((s) => !s.ok)
          throw new ApiError(
            `TEST failed at ${failed?.step ?? 'unknown step'}: ${failed?.error ?? 'unknown error'}`,
            400,
            outcome,
          )
        }
        return outcome
      })
    } catch {
      setToken(getAuthToken())
    } finally {
      setTestRunning(false)
    }
  }

  return (
    <div className="playground">
      <header className="playground-header">
        <div>
          <h1>Renko API Playground</h1>
          <p>Fill fields and click buttons to hit your Spring Boot controllers.</p>
        </div>
        <div className="header-actions">
          <button
            type="button"
            className="btn btn-test"
            disabled={loading || testRunning}
            onClick={handleFullTest}
            title="Creates a full valid POS system: owner, store, catalog, inventory, cashier, shift, order, receipt, refund"
          >
            {testRunning ? 'TEST running…' : 'TEST'}
          </button>
          <div className={`token-pill ${token ? 'token-pill-on' : ''}`}>
            {token ? `JWT saved (${token.slice(0, 18)}…)` : 'No JWT — signup or login first'}
          </div>
        </div>
      </header>

      <nav className="playground-nav">
        {SECTIONS.map((section) => (
          <button
            key={section}
            type="button"
            className={`nav-chip ${active === section ? 'nav-chip-active' : ''}`}
            onClick={() => setActive(section)}
          >
            {section}
          </button>
        ))}
      </nav>

      <div className="playground-layout">
        <div className="playground-main">
          {active === 'auth' && <AuthSection onRun={safeRun} onTokenChange={setToken} />}
          {active === 'stores' && <StoreSection onRun={safeRun} />}
          {active === 'branches' && <BranchSection onRun={safeRun} />}
          {active === 'categories' && <CategorySection onRun={safeRun} />}
          {active === 'products' && <ProductSection onRun={safeRun} />}
          {active === 'inventory' && <InventorySection onRun={safeRun} />}
          {active === 'customers' && <CustomerSection onRun={safeRun} />}
          {active === 'employees' && <EmployeeSection onRun={safeRun} />}
          {active === 'orders' && <OrderSection onRun={safeRun} />}
          {active === 'refunds' && <RefundSection onRun={safeRun} />}
          {active === 'shifts' && <ShiftReportSection onRun={safeRun} />}
          {active === 'users' && <UserSection onRun={safeRun} />}
          {active === 'billing' && <BillingSection onRun={safeRun} />}
        </div>
        <ResponsePanel loading={loading || testRunning} result={result} error={error} />
      </div>
    </div>
  )
}
