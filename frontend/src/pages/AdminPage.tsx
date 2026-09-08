import { FormEvent, useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { createStore } from '@/api/stores'
import { useStoreId } from '@/hooks/useStoreId'
import { isAdminRole } from '@/lib/roles'
import type { AdminOutletContext } from '@/pages/admin/admin-context'
import { useAuth } from '@/stores/auth-store'

const SECTIONS = [
  { to: '/admin', end: true, label: 'Dashboard' },
  { to: '/admin/products', label: 'Products' },
  { to: '/admin/inventory', label: 'Inventory' },
  { to: '/admin/employees', label: 'Employees' },
  { to: '/admin/customers', label: 'Customers' },
  { to: '/admin/orders', label: 'Orders' },
  { to: '/admin/reports', label: 'Reports' },
  { to: '/admin/billing', label: 'Billing' },
  { to: '/admin/shifts', label: 'Shifts' },
] as const

export function AdminPage() {
  const { user } = useAuth()
  const { storeId, loading, error, refresh } = useStoreId()
  const [creatingStore, setCreatingStore] = useState(false)
  const [storeForm, setStoreForm] = useState({
    brandName: '',
    storeType: 'RETAIL',
    description: '',
    contactEmail: '',
    contactPhone: '',
    contactAddress: '',
  })
  const [storeMessage, setStoreMessage] = useState<string | null>(null)

  async function handleCreateStore(event: FormEvent) {
    event.preventDefault()
    setCreatingStore(true)
    setStoreMessage(null)
    try {
      const store = await createStore({
        brandName: storeForm.brandName,
        storeType: storeForm.storeType,
        description: storeForm.description,
        contact: {
          email: storeForm.contactEmail,
          phone: storeForm.contactPhone,
          address: storeForm.contactAddress,
        },
      })
      setStoreMessage(`Store created (#${store.id}).`)
      await refresh()
    } catch (err) {
      setStoreMessage(err instanceof Error ? err.message : 'Failed to create store')
    } finally {
      setCreatingStore(false)
    }
  }

  if (loading) {
    return <div className="page-state">Loading admin console…</div>
  }

  if (!storeId) {
    return (
      <div className="admin-setup panel">
        <div className="panel-header">
          <h1>Set up your store</h1>
          <p>{error ?? 'Create a store to unlock the admin console and POS.'}</p>
        </div>
        <div className="panel-body">
          {isAdminRole(user?.role) ? (
            <form className="stack-form" onSubmit={handleCreateStore}>
              {storeMessage ? <div className="app-alert">{storeMessage}</div> : null}
              <div className="form-grid">
                <label className="field">
                  <span className="field-label">Brand name</span>
                  <input
                    required
                    value={storeForm.brandName}
                    onChange={(e) => setStoreForm({ ...storeForm, brandName: e.target.value })}
                  />
                </label>
                <label className="field">
                  <span className="field-label">Store type</span>
                  <input
                    value={storeForm.storeType}
                    onChange={(e) => setStoreForm({ ...storeForm, storeType: e.target.value })}
                  />
                </label>
                <label className="field">
                  <span className="field-label">Description</span>
                  <input
                    value={storeForm.description}
                    onChange={(e) => setStoreForm({ ...storeForm, description: e.target.value })}
                  />
                </label>
                <label className="field">
                  <span className="field-label">Contact email</span>
                  <input
                    type="email"
                    value={storeForm.contactEmail}
                    onChange={(e) => setStoreForm({ ...storeForm, contactEmail: e.target.value })}
                  />
                </label>
                <label className="field">
                  <span className="field-label">Contact phone</span>
                  <input
                    value={storeForm.contactPhone}
                    onChange={(e) => setStoreForm({ ...storeForm, contactPhone: e.target.value })}
                  />
                </label>
                <label className="field">
                  <span className="field-label">Address</span>
                  <input
                    value={storeForm.contactAddress}
                    onChange={(e) => setStoreForm({ ...storeForm, contactAddress: e.target.value })}
                  />
                </label>
              </div>
              <button type="submit" className="btn" disabled={creatingStore}>
                {creatingStore ? 'Creating…' : 'Create store'}
              </button>
            </form>
          ) : (
            <p className="muted">Ask an owner or manager to assign you to a store.</p>
          )}
          <button type="button" className="btn btn-secondary" onClick={() => void refresh()}>
            Retry lookup
          </button>
        </div>
      </div>
    )
  }

  const outletContext: AdminOutletContext = { storeId }

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar panel">
        <div className="panel-header">
          <h1>Admin</h1>
          <p>Store #{storeId}</p>
        </div>
        <nav className="admin-nav">
          {SECTIONS.map((section) => (
            <NavLink
              key={section.to}
              to={section.to}
              end={'end' in section ? section.end : false}
              className={({ isActive }) => (isActive ? 'admin-nav-link active' : 'admin-nav-link')}
            >
              {section.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <section className="admin-content">
        <Outlet context={outletContext} />
      </section>
    </div>
  )
}
