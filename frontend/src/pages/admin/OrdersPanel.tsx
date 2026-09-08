import { useCallback, useEffect, useState } from 'react'
import { getOrderReceipt, listOrdersByStore } from '@/api/orders'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Order, Receipt } from '@/types/models'

export function OrdersPanel() {
  const { storeId } = useAdminContext()
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [receipt, setReceipt] = useState<Receipt | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setOrders(await listOrdersByStore(storeId))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load orders')
    } finally {
      setLoading(false)
    }
  }, [storeId])

  useEffect(() => {
    void load()
  }, [load])

  async function viewReceipt(orderId: number) {
    try {
      setReceipt(await getOrderReceipt(orderId))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load receipt')
    }
  }

  return (
    <div className="admin-panel panel">
      <div className="panel-header row-between">
        <div>
          <h2>Orders</h2>
          <p>Recent orders for store #{storeId}</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => void load()}>
          Refresh
        </button>
      </div>
      <div className="panel-body">
        {error ? <div className="app-alert error">{error}</div> : null}
        {loading ? <p className="muted">Loading…</p> : null}
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Created</th>
                <th>Payment</th>
                <th>Total</th>
                <th>Cashier</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>{order.id}</td>
                  <td>{order.createdAt ? new Date(order.createdAt).toLocaleString() : '—'}</td>
                  <td>{order.paymentType ?? '—'}</td>
                  <td>${(order.totalAmount ?? 0).toFixed(2)}</td>
                  <td>{order.cashierId ?? '—'}</td>
                  <td>
                    <button type="button" className="btn btn-sm btn-secondary" onClick={() => void viewReceipt(order.id)}>
                      Receipt
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {receipt ? (
          <div className="receipt-box">
            <h3>Receipt {receipt.receiptNumber ?? `#${receipt.orderId}`}</h3>
            <p>
              {receipt.storeName} · {receipt.orderDate ? new Date(receipt.orderDate).toLocaleString() : ''}
            </p>
            <ul>
              {(receipt.items ?? []).map((item, index) => (
                <li key={`${item.name}-${index}`}>
                  <span>
                    {item.quantity} × {item.name}
                  </span>
                  <span>${(item.lineTotal ?? item.finalPrice ?? 0).toFixed(2)}</span>
                </li>
              ))}
            </ul>
          </div>
        ) : null}
      </div>
    </div>
  )
}
