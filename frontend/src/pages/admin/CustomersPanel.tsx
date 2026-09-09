import { FormEvent, useCallback, useEffect, useState } from 'react'
import { createCustomer, listCustomersByStorePaged } from '@/api/customers'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Customer } from '@/types/models'

const PAGE_SIZE = 10

export function CustomersPanel() {
  const { storeId } = useAdminContext()
  const [customers, setCustomers] = useState<Customer[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({ fullName: '', email: '', phone: '' })
  const [query, setQuery] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const result = await listCustomersByStorePaged(storeId, {
        page,
        size: PAGE_SIZE,
        q: query || undefined,
      })
      setCustomers(result.content)
      setTotalPages(result.totalPages)
      setTotalElements(result.totalElements)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load customers')
    } finally {
      setLoading(false)
    }
  }, [storeId, page, query])

  useEffect(() => {
    void load()
  }, [load])

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    try {
      await createCustomer({ ...form, storeEntity: { id: storeId } })
      setForm({ fullName: '', email: '', phone: '' })
      setPage(0)
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Create failed')
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
          <h2>Customers</h2>
          <p>Customers for store #{storeId}</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => void load()}>
          Refresh
        </button>
      </div>
      <div className="panel-body">
        {error ? <div className="app-alert error">{error}</div> : null}
        <form className="stack-form" onSubmit={handleCreate}>
          <h3>Add customer</h3>
          <div className="form-grid">
            <label className="field">
              <span className="field-label">Full name</span>
              <input required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
            </label>
            <label className="field">
              <span className="field-label">Email</span>
              <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </label>
            <label className="field">
              <span className="field-label">Phone</span>
              <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            </label>
          </div>
          <button type="submit" className="btn">
            Create customer
          </button>
        </form>

        <form className="list-toolbar" onSubmit={applySearch}>
          <input
            className="pos-search"
            placeholder="Search customers…"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
          <button type="submit" className="btn btn-secondary btn-sm">
            Search
          </button>
        </form>

        {loading ? <p className="muted loading-msg">Loading customers…</p> : null}
        {!loading && customers.length === 0 ? (
          <div className="empty-state">
            <p>No customers yet</p>
            <p className="muted">Add a customer above, or select one during POS checkout.</p>
          </div>
        ) : null}

        {!loading && customers.length > 0 ? (
          <>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                  </tr>
                </thead>
                <tbody>
                  {customers.map((customer) => (
                    <tr key={customer.id}>
                      <td>{customer.id}</td>
                      <td>{customer.fullName}</td>
                      <td>{customer.email ?? '—'}</td>
                      <td>{customer.phone ?? '—'}</td>
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
