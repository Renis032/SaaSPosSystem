import { useCallback, useEffect, useMemo, useState } from 'react'
import { listLowStock } from '@/api/inventory'
import { listOrdersByStore, listTodayOrdersByStore } from '@/api/orders'
import { listProductsByStore } from '@/api/products'
import { listEmployeesByStore } from '@/api/employees'
import { listCustomersByStore } from '@/api/customers'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Inventory, Order } from '@/types/models'

type DayBucket = { label: string; key: string; sales: number; orders: number }

function dayKey(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function last7Days(): DayBucket[] {
  const days: DayBucket[] = []
  const now = new Date()
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now)
    d.setHours(12, 0, 0, 0)
    d.setDate(d.getDate() - i)
    days.push({
      key: dayKey(d),
      label: d.toLocaleDateString(undefined, { weekday: 'short' }),
      sales: 0,
      orders: 0,
    })
  }
  return days
}

export function DashboardPanel() {
  const { storeId } = useAdminContext()
  const [stats, setStats] = useState({
    products: 0,
    employees: 0,
    customers: 0,
    todayOrders: 0,
    todaySales: 0,
    recentSales: 0,
    lowStock: 0,
  })
  const [weekBuckets, setWeekBuckets] = useState<DayBucket[]>(last7Days())
  const [lowStockRows, setLowStockRows] = useState<Inventory[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [products, employees, customers, todayOrders, lowStock, allOrders] = await Promise.all([
        listProductsByStore(storeId),
        listEmployeesByStore(storeId),
        listCustomersByStore(storeId),
        listTodayOrdersByStore(storeId),
        listLowStock(storeId).catch(() => [] as Inventory[]),
        listOrdersByStore(storeId).catch(() => [] as Order[]),
      ])

      const buckets = last7Days()
      const bucketMap = new Map(buckets.map((b) => [b.key, b]))
      const cutoff = new Date()
      cutoff.setDate(cutoff.getDate() - 6)
      cutoff.setHours(0, 0, 0, 0)

      let recentSales = 0
      for (const order of Array.isArray(allOrders) ? allOrders : []) {
        if (!order.createdAt) continue
        const created = new Date(order.createdAt)
        if (created < cutoff) continue
        const amount = order.totalAmount ?? 0
        recentSales += amount
        const key = dayKey(created)
        const bucket = bucketMap.get(key)
        if (bucket) {
          bucket.sales += amount
          bucket.orders += 1
        }
      }

      setWeekBuckets([...buckets])
      setLowStockRows(Array.isArray(lowStock) ? lowStock.slice(0, 8) : [])
      setStats({
        products: products.length,
        employees: employees.length,
        customers: customers.length,
        todayOrders: todayOrders.length,
        todaySales: todayOrders.reduce((sum, order) => sum + (order.totalAmount ?? 0), 0),
        recentSales,
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

  const maxWeekSales = useMemo(
    () => Math.max(1, ...weekBuckets.map((b) => b.sales)),
    [weekBuckets],
  )
  const maxLowQty = useMemo(
    () => Math.max(1, ...lowStockRows.map((r) => r.quantity), ...(lowStockRows.map((r) => r.lowStockThreshold ?? 0))),
    [lowStockRows],
  )
  const todayVsRecentMax = Math.max(stats.todaySales, stats.recentSales / 7, 1)

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
        {loading ? <p className="muted loading-msg">Loading dashboard…</p> : null}

        {!loading ? (
          <>
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

            <div className="dash-charts">
              <section className="dash-chart-panel">
                <h3>Today vs recent</h3>
                <p className="muted">Today vs average daily sales over the last 7 days</p>
                <div className="bar-chart compare-bars">
                  <div className="bar-row">
                    <span className="bar-label">Today</span>
                    <div className="bar-track">
                      <div
                        className="bar-fill bar-fill-primary"
                        style={{ width: `${Math.min(100, (stats.todaySales / todayVsRecentMax) * 100)}%` }}
                      />
                    </div>
                    <span className="bar-value">${stats.todaySales.toFixed(0)}</span>
                  </div>
                  <div className="bar-row">
                    <span className="bar-label">7d avg</span>
                    <div className="bar-track">
                      <div
                        className="bar-fill"
                        style={{
                          width: `${Math.min(100, (stats.recentSales / 7 / todayVsRecentMax) * 100)}%`,
                        }}
                      />
                    </div>
                    <span className="bar-value">${(stats.recentSales / 7).toFixed(0)}</span>
                  </div>
                </div>
              </section>

              <section className="dash-chart-panel">
                <h3>Week sales</h3>
                <p className="muted">Gross sales by day (from store orders)</p>
                {weekBuckets.every((b) => b.sales === 0) ? (
                  <p className="muted">No orders in the last 7 days</p>
                ) : (
                  <div className="spark-bars" role="img" aria-label="Weekly sales bars">
                    {weekBuckets.map((bucket) => (
                      <div key={bucket.key} className="spark-col">
                        <div className="spark-bar-wrap">
                          <div
                            className="spark-bar"
                            style={{ height: `${Math.max(4, (bucket.sales / maxWeekSales) * 100)}%` }}
                            title={`$${bucket.sales.toFixed(2)} · ${bucket.orders} orders`}
                          />
                        </div>
                        <span>{bucket.label}</span>
                      </div>
                    ))}
                  </div>
                )}
              </section>

              <section className="dash-chart-panel">
                <h3>Low stock</h3>
                <p className="muted">Items at or below threshold</p>
                {lowStockRows.length === 0 ? (
                  <p className="muted">No low-stock items</p>
                ) : (
                  <div className="bar-chart">
                    {lowStockRows.map((row) => (
                      <div key={row.id} className="bar-row">
                        <span className="bar-label" title={`Product #${row.productId}`}>
                          #{row.productId}
                        </span>
                        <div className="bar-track">
                          <div
                            className="bar-fill bar-fill-warn"
                            style={{ width: `${Math.min(100, (row.quantity / maxLowQty) * 100)}%` }}
                          />
                        </div>
                        <span className="bar-value">{row.quantity}</span>
                      </div>
                    ))}
                  </div>
                )}
              </section>
            </div>
          </>
        ) : null}
      </div>
    </div>
  )
}
