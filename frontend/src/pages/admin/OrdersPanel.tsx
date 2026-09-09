import { FormEvent, useCallback, useEffect, useState } from 'react'
import { getOrderReceipt, listOrdersByStorePaged } from '@/api/orders'
import { downloadReceiptPdf, printReceipt } from '@/lib/receipt-pdf'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Order, Receipt } from '@/types/models'

const PAGE_SIZE = 10

export function OrdersPanel() {
  const { storeId } = useAdminContext()
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [receipt, setReceipt] = useState<Receipt | null>(null)
  const [query, setQuery] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await listOrdersByStorePaged(storeId, {
        page,
        size: PAGE_SIZE,
        q: query || undefined,
      })
      setOrders(result.content)
      setTotalPages(result.totalPages)
      setTotalElements(result.totalElements)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load orders')
    } finally {
      setLoading(false)
    }
  }, [storeId, page, query])

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

  function applySearch(event: FormEvent) {
    event.preventDefault()
    setPage(0)
    setQuery(searchInput.trim())
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

        <form className="list-toolbar" onSubmit={applySearch}>
          <input
            className="pos-search"
            placeholder="Search orders…"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
          <button type="submit" className="btn btn-secondary btn-sm">
            Search
          </button>
        </form>

        {loading ? <p className="muted loading-msg">Loading orders…</p> : null}
        {!loading && orders.length === 0 ? (
          <div className="empty-state">
            <p>No orders yet</p>
            <p className="muted">Complete a sale on POS, or reset the demo dataset to load sample sales.</p>
          </div>
        ) : null}

        {!loading && orders.length > 0 ? (
          <>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Created</th>
                    <th>Payment</th>
                    <th>Branch</th>
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
                      <td>{order.branchId ?? '—'}</td>
                      <td>${(order.totalAmount ?? 0).toFixed(2)}</td>
                      <td>{order.cashierId ?? '—'}</td>
                      <td>
                        <button
                          type="button"
                          className="btn btn-sm btn-secondary"
                          onClick={() => void viewReceipt(order.id)}
                        >
                          Receipt
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="page-controls">
              <span className="muted">
                {totalElements} total · page {page + 1} of {Math.max(1, totalPages)}
              </span>
              <div className="inline-actions">
                <button
                  type="button"
                  className="btn btn-sm btn-secondary"
                  disabled={page <= 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                >
                  Previous
                </button>
                <button
                  type="button"
                  className="btn btn-sm btn-secondary"
                  disabled={page + 1 >= totalPages}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Next
                </button>
              </div>
            </div>
          </>
        ) : null}

        {receipt ? (
          <div className="receipt-box receipt-print">
            <h3>Receipt {receipt.receiptNumber ?? `#${receipt.orderId}`}</h3>
            <p>
              {receipt.storeName}
              {receipt.branchName ? ` · ${receipt.branchName}` : ''} ·{' '}
              {receipt.orderDate ? new Date(receipt.orderDate).toLocaleString() : ''}
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
            <div className="receipt-totals">
              {receipt.subtotal != null ? (
                <div>
                  <span>Subtotal</span>
                  <strong>${receipt.subtotal.toFixed(2)}</strong>
                </div>
              ) : null}
              {receipt.totalDiscount != null && receipt.totalDiscount > 0 ? (
                <div>
                  <span>Discount</span>
                  <strong>−${receipt.totalDiscount.toFixed(2)}</strong>
                </div>
              ) : null}
              {receipt.taxRate != null && receipt.taxRate > 0 ? (
                <div>
                  <span>Tax ({receipt.taxRate}%)</span>
                  <strong>${(receipt.taxAmount ?? 0).toFixed(2)}</strong>
                </div>
              ) : null}
              {receipt.totalAmount != null ? (
                <div>
                  <span>Total</span>
                  <strong>${receipt.totalAmount.toFixed(2)}</strong>
                </div>
              ) : null}
            </div>
            <div className="receipt-actions">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => downloadReceiptPdf(receipt)}
              >
                Download PDF
              </button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => printReceipt(receipt)}>
                Print
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  )
}
