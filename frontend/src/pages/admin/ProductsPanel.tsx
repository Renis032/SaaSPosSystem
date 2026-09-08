import { FormEvent, useCallback, useEffect, useState } from 'react'
import { createProduct, deleteProduct, listProductsByStore } from '@/api/products'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Product } from '@/types/models'

export function ProductsPanel() {
  const { storeId } = useAdminContext()
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({
    name: '',
    sku: '',
    brand: '',
    sellingPrice: '',
    maxRetailPrice: '',
    description: '',
  })

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setProducts(await listProductsByStore(storeId))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load products')
    } finally {
      setLoading(false)
    }
  }, [storeId])

  useEffect(() => {
    void load()
  }, [load])

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    setError(null)
    try {
      await createProduct({
        name: form.name,
        sku: form.sku,
        brand: form.brand || undefined,
        description: form.description || undefined,
        sellingPrice: Number(form.sellingPrice),
        maxRetailPrice: Number(form.maxRetailPrice || form.sellingPrice),
        storeId,
      })
      setForm({ name: '', sku: '', brand: '', sellingPrice: '', maxRetailPrice: '', description: '' })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Create failed')
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('Delete this product?')) return
    try {
      await deleteProduct(id)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed')
    }
  }

  return (
    <div className="admin-panel panel">
      <div className="panel-header row-between">
        <div>
          <h2>Products</h2>
          <p>Catalog for store #{storeId}</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => void load()}>
          Refresh
        </button>
      </div>
      <div className="panel-body">
        {error ? <div className="app-alert error">{error}</div> : null}
        <form className="stack-form" onSubmit={handleCreate}>
          <h3>Add product</h3>
          <div className="form-grid">
            <label className="field">
              <span className="field-label">Name</span>
              <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </label>
            <label className="field">
              <span className="field-label">SKU</span>
              <input required value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} />
            </label>
            <label className="field">
              <span className="field-label">Brand</span>
              <input value={form.brand} onChange={(e) => setForm({ ...form, brand: e.target.value })} />
            </label>
            <label className="field">
              <span className="field-label">Selling price</span>
              <input
                required
                type="number"
                step="0.01"
                min="0"
                value={form.sellingPrice}
                onChange={(e) => setForm({ ...form, sellingPrice: e.target.value })}
              />
            </label>
            <label className="field">
              <span className="field-label">MRP</span>
              <input
                type="number"
                step="0.01"
                min="0"
                value={form.maxRetailPrice}
                onChange={(e) => setForm({ ...form, maxRetailPrice: e.target.value })}
              />
            </label>
            <label className="field">
              <span className="field-label">Description</span>
              <input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </label>
          </div>
          <button type="submit" className="btn">
            Create product
          </button>
        </form>

        {loading ? <p className="muted">Loading…</p> : null}
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>SKU</th>
                <th>Price</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td>{product.id}</td>
                  <td>{product.name}</td>
                  <td>{product.sku}</td>
                  <td>${(product.sellingPrice ?? 0).toFixed(2)}</td>
                  <td>
                    <button type="button" className="btn btn-danger btn-sm" onClick={() => void handleDelete(product.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
