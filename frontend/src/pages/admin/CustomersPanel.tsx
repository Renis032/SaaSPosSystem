import { FormEvent, useCallback, useEffect, useState } from 'react'
import { createCustomer, listCustomersByStore } from '@/api/customers'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Customer } from '@/types/models'

export function CustomersPanel() {
  const { storeId } = useAdminContext()
  const [customers, setCustomers] = useState<Customer[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({ fullName: '', email: '', phone: '' })

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setCustomers(await listCustomersByStore(storeId))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load customers')
    } finally {
      setLoading(false)
    }
  }, [storeId])

  useEffect(() => {
    void load()
  }, [load])

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    try {
      await createCustomer({ ...form, storeEntity: { id: storeId } })
      setForm({ fullName: '', email: '', phone: '' })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Create failed')
    }
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

        {loading ? <p className="muted">Loading…</p> : null}
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
      </div>
    </div>
  )
}
