import { FormEvent, useCallback, useEffect, useState } from 'react'
import { createProduct, deleteProduct, listProductsByStorePaged } from '@/api/products'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Product } from '@/types/models'

const PAGE_SIZE = 10

export function ProductsPanel() {
  const { storeId } = useAdminContext()
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [query, setQuery] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
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
      const result = await listProductsByStorePaged(storeId, {
        page,
        size: PAGE_SIZE,
        q: query || undefined,
      })
      setProducts(result.content)
      setTotalPages(result.totalPages)
      setTotalElements(result.totalElements)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load products')
    } finally {
      setLoading(false)
    }
  }, [storeId, page, query])

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
      setPage(0)
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

  function applySearch(event: FormEvent) {
    event.preventDefault()
    setPage(0)
    setQuery(searchInput.trim())
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

        <form className="list-toolbar" onSubmit={applySearch}>
          <input
            className="pos-search"
            placeholder="Search products…"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
          <button type="submit" className="btn btn-secondary btn-sm">
            Search
          </button>
        </form>

        {loading ? <p className="muted loading-msg">Loading products…</p> : null}
        {!loading && products.length === 0 ? (
          <div className="empty-state">
            <p className="muted">No products yet</p>
            <button type="button" className="btn btn-secondary btn-sm" onClick={() => void load()}>
              Refresh
            </button>
          </div>
        ) : null}

        {!loading && products.length > 0 ? (
          <>
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
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          onClick={() => void handleDelete(product.id)}
                        >
                          Delete
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
      </div>
    </div>
  )
}
