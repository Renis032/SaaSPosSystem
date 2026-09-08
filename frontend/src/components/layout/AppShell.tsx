import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { canAccessAdmin, canAccessPos, isSimpleUserRole } from '@/lib/roles'
import { clearActiveBranch } from '@/lib/branch-store'
import { clearAuthSession, useAuth } from '@/stores/auth-store'
import { useStoreId } from '@/hooks/useStoreId'

export function AppShell() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const { storeId } = useStoreId()
  const showAdmin = canAccessAdmin(user?.role, storeId)
  const showPos = canAccessPos(user?.role, storeId)
  const showWorkspace = isSimpleUserRole(user?.role)

  function handleLogout() {
    clearAuthSession()
    clearActiveBranch()
    navigate('/', { replace: true })
  }

  return (
    <div className="app-shell">
      <header className="app-topbar">
        <div className="app-brand">
          <span className="app-brand-mark">R</span>
          <div>
            <strong>Renko POS</strong>
            <p>
              {user?.fullName ?? 'Signed in'} · {user?.role ?? '—'}
            </p>
          </div>
        </div>
        <nav className="app-nav">
          {showWorkspace ? (
            <NavLink
              to="/workspace"
              className={({ isActive }) => (isActive ? 'app-nav-link active' : 'app-nav-link')}
            >
              Workspace
            </NavLink>
          ) : null}
          {showPos ? (
            <NavLink to="/pos" className={({ isActive }) => (isActive ? 'app-nav-link active' : 'app-nav-link')}>
              POS
            </NavLink>
          ) : null}
          {showAdmin ? (
            <NavLink to="/admin" className={({ isActive }) => (isActive ? 'app-nav-link active' : 'app-nav-link')}>
              Admin
            </NavLink>
          ) : null}
          <NavLink
            to="/playground"
            className={({ isActive }) => (isActive ? 'app-nav-link active' : 'app-nav-link')}
          >
            Playground
          </NavLink>
        </nav>
        <div className="app-topbar-meta">
          {storeId ? <span className="meta-pill">Store #{storeId}</span> : <span className="meta-pill muted">No store</span>}
          <button type="button" className="btn btn-secondary" onClick={handleLogout}>
            Log out
          </button>
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
