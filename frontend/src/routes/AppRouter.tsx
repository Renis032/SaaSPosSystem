import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { RequireAuth } from '@/components/auth/RequireAuth'
import { RequireRole } from '@/components/auth/RequireRole'
import { AppShell } from '@/components/layout/AppShell'
import { homePathForRole } from '@/lib/roles'
import { ApiPlaygroundPage } from '@/pages/ApiPlaygroundPage'
import { AdminPage } from '@/pages/AdminPage'
import { BillingPanel } from '@/pages/admin/BillingPanel'
import { CustomersPanel } from '@/pages/admin/CustomersPanel'
import { DashboardPanel } from '@/pages/admin/DashboardPanel'
import { EmployeesPanel } from '@/pages/admin/EmployeesPanel'
import { InventoryPanel } from '@/pages/admin/InventoryPanel'
import { OrdersPanel } from '@/pages/admin/OrdersPanel'
import { ProductsPanel } from '@/pages/admin/ProductsPanel'
import { RefundsPanel } from '@/pages/admin/RefundsPanel'
import { ReportsPanel } from '@/pages/admin/ReportsPanel'
import { ShiftsPanel } from '@/pages/admin/ShiftsPanel'
import { LandingPage } from '@/pages/LandingPage'
import { LoginPage } from '@/pages/LoginPage'
import { PosPage } from '@/pages/PosPage'
import { SignupPage } from '@/pages/SignupPage'
import { WorkspacePage } from '@/pages/WorkspacePage'
import { useAuth } from '@/stores/auth-store'

const ADMIN_ROLES = ['OWNER', 'STORE_MANAGER', 'BRANCH_MANAGER', 'ADMIN']
const POS_ROLES = ['CASHIER', ...ADMIN_ROLES]
const WORKSPACE_ROLES = ['USER', ...POS_ROLES]

function AppHomeRedirect() {
  const { token, user } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  return <Navigate to={homePathForRole(user?.role)} replace />
}

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />

        <Route
          element={
            <RequireAuth>
              <AppShell />
            </RequireAuth>
          }
        >
          <Route
            path="/workspace"
            element={
              <RequireRole roles={WORKSPACE_ROLES} cashierFallback="/pos">
                <WorkspacePage />
              </RequireRole>
            }
          />
          <Route
            path="/pos"
            element={
              <RequireRole roles={POS_ROLES} cashierFallback="/workspace">
                <PosPage />
              </RequireRole>
            }
          />
          <Route
            path="/admin"
            element={
              <RequireRole roles={ADMIN_ROLES} cashierFallback="/pos">
                <AdminPage />
              </RequireRole>
            }
          >
            <Route index element={<DashboardPanel />} />
            <Route path="products" element={<ProductsPanel />} />
            <Route path="inventory" element={<InventoryPanel />} />
            <Route path="employees" element={<EmployeesPanel />} />
            <Route path="customers" element={<CustomersPanel />} />
            <Route path="orders" element={<OrdersPanel />} />
            <Route path="refunds" element={<RefundsPanel />} />
            <Route path="reports" element={<ReportsPanel />} />
            <Route path="billing" element={<BillingPanel />} />
            <Route path="shifts" element={<ShiftsPanel />} />
          </Route>
          <Route path="/playground" element={<ApiPlaygroundPage />} />
        </Route>

        <Route path="/app" element={<AppHomeRedirect />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
