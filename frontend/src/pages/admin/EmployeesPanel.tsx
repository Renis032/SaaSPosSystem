import { FormEvent, useCallback, useEffect, useState } from 'react'
import { createEmployee, listEmployeesByStore } from '@/api/employees'
import { USER_ROLES } from '@/lib/enums'
import { useAdminContext } from '@/pages/admin/admin-context'
import type { Employee } from '@/types/models'

const CREATE_ROLES = USER_ROLES.filter((role) => role !== 'OWNER' && role !== 'ADMIN')

export function EmployeesPanel() {
  const { storeId } = useAdminContext()
  const [employees, setEmployees] = useState<Employee[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phoneNumber: '',
    role: 'CASHIER',
  })

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setEmployees(await listEmployeesByStore(storeId))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load employees')
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
      await createEmployee(storeId, form)
      setForm({ fullName: '', email: '', password: '', phoneNumber: '', role: 'CASHIER' })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Create failed')
    }
  }

  return (
    <div className="admin-panel panel">
      <div className="panel-header row-between">
        <div>
          <h2>Employees</h2>
          <p>Staff for store #{storeId}</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={() => void load()}>
          Refresh
        </button>
      </div>
      <div className="panel-body">
        {error ? <div className="app-alert error">{error}</div> : null}
        <form className="stack-form" onSubmit={handleCreate}>
          <h3>Add employee</h3>
          <div className="form-grid">
            <label className="field">
              <span className="field-label">Full name</span>
              <input required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
            </label>
            <label className="field">
              <span className="field-label">Email</span>
              <input
                required
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            </label>
            <label className="field">
              <span className="field-label">Password</span>
              <input
                required
                type="password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
              />
            </label>
            <label className="field">
              <span className="field-label">Phone</span>
              <input value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} />
            </label>
            <label className="field">
              <span className="field-label">Role</span>
              <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                {CREATE_ROLES.map((role) => (
                  <option key={role} value={role}>
                    {role}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <button type="submit" className="btn">
            Create employee
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
                <th>Role</th>
                <th>Phone</th>
              </tr>
            </thead>
            <tbody>
              {employees.map((employee) => (
                <tr key={employee.id}>
                  <td>{employee.id}</td>
                  <td>{employee.fullName}</td>
                  <td>{employee.email}</td>
                  <td>{employee.role}</td>
                  <td>{employee.phoneNumber ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
