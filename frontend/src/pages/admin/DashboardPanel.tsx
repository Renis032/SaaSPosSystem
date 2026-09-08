import { useCallback, useEffect, useState } from 'react'
import { listLowStock } from '@/api/inventory'
import { listTodayOrdersByStore } from '@/api/orders'
import { listProductsByStore } from '@/api/products'
import { listEmployeesByStore } from '@/api/employees'
import { listCustomersByStore } from '@/api/customers'
import { useAdminContext } from '@/pages/admin/admin-context'

export function DashboardPanel() {
  const { storeId } = useAdminContext()
  const [stats, setStats] = useState({
    products: 0,
    employees: 0,
    customers: 0,
    todayOrders: 0,
    todaySales: 0,
    lowStock: 0,
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [products, employees, customers, todayOrders, lowStock] = await Promise.all([
        listProductsByStore(storeId),
        listEmployeesByStore(storeId),
        listCustomersByStore(storeId),
        listTodayOrdersByStore(storeId),
        listLowStock(storeId).catch(() => []),
      ])
      setStats({
        products: products.length,
        employees: employees.length,
        customers: customers.length,
        todayOrders: todayOrders.length,
        todaySales: todayOrders.reduce((sum, order) => sum + (order.totalAmount ?? 0), 0),
        lowStock: lowStock.length,
      })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load dashboard')
    } finally {
      setLoading(false)
    }
  }, [storeId])

  useEffect(() => {
    void load()
  }, [load])

  return (
    <div className="admin-panel panel">
      <div className="panel-header row-between">
        <div>
          <h2>Back office</h2>
          <p>Store #{storeId} overview — products, people, and today’s sales</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => void load()}>
          Refresh
        </button>
      </div>
      <div className="panel-body">
        {error ? <div className="app-alert error">{error}</div> : null}
        {loading ? <p className="muted">Loading…</p> : null}
        <div className="stat-grid">
          <div className="stat-tile">
            <span>Today sales</span>
            <strong>${stats.todaySales.toFixed(2)}</strong>
          </div>
          <div className="stat-tile">
            <span>Today orders</span>
            <strong>{stats.todayOrders}</strong>
          </div>
          <div className="stat-tile">
            <span>Products</span>
            <strong>{stats.products}</strong>
          </div>
          <div className="stat-tile">
            <span>Employees</span>
            <strong>{stats.employees}</strong>
          </div>
          <div className="stat-tile">
            <span>Customers</span>
            <strong>{stats.customers}</strong>
          </div>
          <div className="stat-tile">
            <span>Low stock</span>
            <strong>{stats.lowStock}</strong>
          </div>
        </div>
      </div>
    </div>
  )
}
