import { Link } from 'react-router-dom'
import { useAuth } from '@/stores/auth-store'

export function WorkspacePage() {
  const { user } = useAuth()

  return (
    <div className="page-state workspace-page">
      <h2>Limited workspace</h2>
      <p>
        Signed in as <strong>{user?.fullName ?? user?.email}</strong> ({user?.role ?? 'USER'}).
      </p>
      <p className="muted">
        Simple users can browse the API Playground but cannot open POS or the admin console. Switch
        accounts from the landing page or sign out to try Owner, Cashier, or Manager.
      </p>
      <div className="workspace-actions">
        <Link to="/playground" className="btn">
          Open Playground
        </Link>
        <Link to="/" className="btn btn-secondary">
          Back to home
        </Link>
      </div>
    </div>
  )
}
