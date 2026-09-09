import { useEffect } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { listBranchesByStore } from '@/api/branches'
import { canAccessAdmin, canAccessPos, isSimpleUserRole } from '@/lib/roles'
import { clearActiveBranch, setActiveBranch, useActiveBranch } from '@/lib/branch-store'
import { clearAuthSession, useAuth } from '@/stores/auth-store'
import { useStoreId } from '@/hooks/useStoreId'

export function AppShell() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const { storeId } = useStoreId()
  const activeBranch = useActiveBranch()
  const showAdmin = canAccessAdmin(user?.role, storeId)
  const showPos = canAccessPos(user?.role, storeId)
  const showWorkspace = isSimpleUserRole(user?.role)

  useEffect(() => {
    if (!storeId) {
      setActiveBranch(null)
      return
    }
    let cancelled = false
    void listBranchesByStore(storeId)
      .then((rows) => {
        if (cancelled) return
        const list = Array.isArray(rows) ? rows : []
        if (list.length === 0) {
          setActiveBranch(null)
          return
        }
        const stillValid = activeBranch && list.some((b) => b.id === activeBranch.id)
        if (!stillValid) {
          const first = list[0]
          setActiveBranch({
            id: first.id,
            name: first.name,
            address: first.address,
            phone: first.phone,
          })
        }
      })
      .catch(() => {
        if (!cancelled) setActiveBranch(null)
      })
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [storeId])

  function handleLogout() {
    clearAuthSession()
    clearActiveBranch()
    navigate('/', { replace: true })
  }

  return (
    <div className="app-shell">
      <header className="app-topbar">
        <Link to="/" className="app-brand" title="Back to home">
          <span className="app-brand-mark">R</span>
          <div>
            <strong>Renko POS</strong>
            <p>
              {user?.fullName ?? 'Signed in'} · {user?.role ?? '—'}
            </p>
          </div>
        </Link>
        <nav className="app-nav">
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'app-nav-link active' : 'app-nav-link')}>
            Home
          </NavLink>
          {showPos ? (
            <NavLink
              to="/pos"
              className={({ isActive }) =>
                isActive ? 'app-nav-link app-nav-link-pos active' : 'app-nav-link app-nav-link-pos'
              }
            >
              POS
            </NavLink>
          ) : null}
          {showAdmin ? (
            <NavLink to="/admin" className={({ isActive }) => (isActive ? 'app-nav-link active' : 'app-nav-link')}>
              Admin
            </NavLink>
          ) : null}
          {showWorkspace ? (
            <NavLink
              to="/workspace"
              className={({ isActive }) => (isActive ? 'app-nav-link active' : 'app-nav-link')}
            >
              Workspace
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
