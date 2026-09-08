import { useCallback, useEffect, useState } from 'react'
import { getStoreAuditLog, getStoreReport } from '@/api/reports'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { AuditLog, StoreReport } from '@/types/models'

function money(value?: number) {
  return `$${(value ?? 0).toFixed(2)}`
}

export function ReportsPanel() {
  const { storeId } = useAdminContext()
  const [report, setReport] = useState<StoreReport | null>(null)
  const [audit, setAudit] = useState<AuditLog[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [auditError, setAuditError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    setAuditError(null)
    try {
      const storeReport = await getStoreReport(storeId)
      setReport(storeReport)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load report')
      setReport(null)
    }

    try {
      const logs = await getStoreAuditLog(storeId)
      setAudit(Array.isArray(logs) ? logs : [])
    } catch (err) {
      setAudit([])
      setAuditError(err instanceof Error ? err.message : 'Failed to load audit log')
    } finally {
      setLoading(false)
    }
  }, [storeId])

  useEffect(() => {
    void load()
  }, [load])

  return (
    <div className="admin-panel panel">
      <div className="panel-header row-between">
        <div>
          <h2>Reports</h2>
          <p>Store summary and audit trail for #{storeId}</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => void load()}>
          Refresh
        </button>
      </div>
      <div className="panel-body">
        {error ? <div className="app-alert error">{error}</div> : null}
        {loading ? <p className="muted">Loading…</p> : null}

        <h3>Store report</h3>
        <div className="stat-grid">
          <div className="stat-tile">
            <span>Orders</span>
            <strong>{report?.orderCount ?? '—'}</strong>
          </div>
          <div className="stat-tile">
            <span>Gross sales</span>
            <strong>{report ? money(report.grossSales) : '—'}</strong>
          </div>
          <div className="stat-tile">
            <span>Refunds</span>
            <strong>
              {report ? `${report.refundCount ?? 0} · ${money(report.refundTotal)}` : '—'}
            </strong>
          </div>
          <div className="stat-tile">
            <span>Net sales</span>
            <strong>{report ? money(report.netSales) : '—'}</strong>
          </div>
          <div className="stat-tile">
            <span>Low stock items</span>
            <strong>{report?.lowStockCount ?? '—'}</strong>
          </div>
          <div className="stat-tile">
            <span>Open shifts</span>
            <strong>{report?.openShiftCount ?? '—'}</strong>
          </div>
        </div>

        <h3>Audit log</h3>
        {auditError ? <div className="app-alert error">{auditError}</div> : null}
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>When</th>
                <th>Actor</th>
                <th>Action</th>
                <th>Entity</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              {audit.length === 0 && !loading ? (
                <tr>
                  <td colSpan={5} className="muted">
                    No audit entries.
                  </td>
                </tr>
              ) : null}
              {audit.map((entry) => (
                <tr key={entry.id}>
                  <td>{entry.createdAt ? new Date(entry.createdAt).toLocaleString() : '—'}</td>
                  <td>{entry.actorEmail ?? (entry.actorUserId != null ? `#${entry.actorUserId}` : '—')}</td>
                  <td>{entry.action}</td>
                  <td>
                    {entry.entityType}
                    {entry.entityId ? ` #${entry.entityId}` : ''}
                  </td>
                  <td className="audit-details">{entry.details ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
