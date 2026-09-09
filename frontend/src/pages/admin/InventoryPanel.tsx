import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react'
import { addStock, adjustStock, createInventory, listInventoriesByStore, listLowStock } from '@/api/inventory'
import { listProductsByStore } from '@/api/products'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Inventory, Product } from '@/types/models'

export function InventoryPanel() {
  const { storeId } = useAdminContext()
  const [rows, setRows] = useState<Inventory[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [lowStockIds, setLowStockIds] = useState<Set<number>>(new Set())
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({ productId: '', quantity: '10', lowStockThreshold: '5' })
  const [addQty, setAddQty] = useState<Record<number, string>>({})
  const [adjustDelta, setAdjustDelta] = useState<Record<number, string>>({})
  const [adjustReason, setAdjustReason] = useState<Record<number, string>>({})
  const [busyId, setBusyId] = useState<number | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [inventories, productList, lowStock] = await Promise.all([
        listInventoriesByStore(storeId),
        listProductsByStore(storeId),
        listLowStock(storeId).catch(() => [] as Inventory[]),
      ])
      setRows(inventories)
      setProducts(productList)
      setLowStockIds(new Set(lowStock.map((row) => row.id)))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load inventory')
    } finally {
      setLoading(false)
    }
  }, [storeId])

  useEffect(() => {
    void load()
  }, [load])

  const productName = (productId: number) => products.find((p) => p.id === productId)?.name ?? `Product #${productId}`

  const isLowStock = useMemo(() => {
    return (row: Inventory) => {
      if (lowStockIds.has(row.id)) return true
      if (row.lowStockThreshold != null && row.quantity <= row.lowStockThreshold) return true
      return false
    }
  }, [lowStockIds])

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    try {
      await createInventory({
        storeId,
        productId: Number(form.productId),
        quantity: Number(form.quantity),
        lowStockThreshold: Number(form.lowStockThreshold),
      })
      setForm({ productId: '', quantity: '10', lowStockThreshold: '5' })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Create failed')
    }
  }

  async function handleAddStock(id: number) {
    const qty = Number(addQty[id] || 0)
    if (!qty) return
    setBusyId(id)
    try {
      await addStock(id, qty)
      setAddQty((prev) => ({ ...prev, [id]: '' }))
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Add stock failed')
    } finally {
      setBusyId(null)
    }
  }

  async function handleAdjust(id: number) {
    const delta = Number(adjustDelta[id] || 0)
    if (!delta) {
      setError('Enter a non-zero delta to adjust stock.')
      return
    }
    setBusyId(id)
    setError(null)
    try {
      await adjustStock(id, delta, adjustReason[id])
      setAdjustDelta((prev) => ({ ...prev, [id]: '' }))
      setAdjustReason((prev) => ({ ...prev, [id]: '' }))
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Adjust stock failed')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="admin-panel panel">
      <div className="panel-header row-between">
        <div>
          <h2>Inventory</h2>
          <p>Stock levels for store #{storeId}</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => void load()}>
          Refresh
        </button>
      </div>
      <div className="panel-body">
        {error ? <div className="app-alert error">{error}</div> : null}
        <form className="stack-form" onSubmit={handleCreate}>
          <h3>Create inventory row</h3>
          <div className="form-grid">
            <label className="field">
              <span className="field-label">Product</span>
              <select required value={form.productId} onChange={(e) => setForm({ ...form, productId: e.target.value })}>
                <option value="">Select product</option>
                {products.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.name} (#{product.id})
                  </option>
                ))}
              </select>
            </label>
            <label className="field">
              <span className="field-label">Quantity</span>
              <input
                required
                type="number"
                min="0"
                value={form.quantity}
                onChange={(e) => setForm({ ...form, quantity: e.target.value })}
              />
            </label>
            <label className="field">
              <span className="field-label">Low stock threshold</span>
              <input
                required
                type="number"
                min="0"
                value={form.lowStockThreshold}
                onChange={(e) => setForm({ ...form, lowStockThreshold: e.target.value })}
              />
            </label>
          </div>
          <button type="submit" className="btn">
            Create
          </button>
        </form>

        {loading ? <p className="muted loading-msg">Loading inventory…</p> : null}
        {!loading && rows.length === 0 ? (
          <div className="empty-state">
            <p className="muted">No inventory yet</p>
            <button type="button" className="btn btn-secondary btn-sm" onClick={() => void load()}>
              Refresh
            </button>
          </div>
        ) : null}
        {!loading && rows.length > 0 ? (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Product</th>
                  <th>Qty</th>
                  <th>Threshold</th>
                  <th>Add stock</th>
                  <th>Adjust (±)</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => {
                  const low = isLowStock(row)
                  return (
                    <tr key={row.id} className={low ? 'row-warn' : undefined}>
                      <td>{row.id}</td>
                      <td>
                        {productName(row.productId)}
                        {low ? <span className="stock-badge">Low stock</span> : null}
                      </td>
                      <td>{row.quantity}</td>
                      <td>{row.lowStockThreshold ?? '—'}</td>
                      <td className="inline-actions">
                        <input
                          type="number"
                          min="1"
                          placeholder="Qty"
                          value={addQty[row.id] ?? ''}
                          onChange={(e) => setAddQty((prev) => ({ ...prev, [row.id]: e.target.value }))}
                        />
                        <button
                          type="button"
                          className="btn btn-sm"
                          disabled={busyId === row.id}
                          onClick={() => void handleAddStock(row.id)}
                        >
                          Add
                        </button>
                      </td>
                      <td className="inline-actions adjust-actions">
                        <input
                          type="number"
                          placeholder="±Δ"
                          value={adjustDelta[row.id] ?? ''}
                          onChange={(e) => setAdjustDelta((prev) => ({ ...prev, [row.id]: e.target.value }))}
                        />
                        <input
                          type="text"
                          className="reason-input"
                          placeholder="Reason"
                          value={adjustReason[row.id] ?? ''}
                          onChange={(e) => setAdjustReason((prev) => ({ ...prev, [row.id]: e.target.value }))}
                        />
                        <button
                          type="button"
                          className="btn btn-sm btn-secondary"
                          disabled={busyId === row.id}
                          onClick={() => void handleAdjust(row.id)}
                        >
                          Adjust
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        ) : null}
      </div>
    </div>
  )
}
