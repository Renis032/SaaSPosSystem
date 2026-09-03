type ResponsePanelProps = {
  loading: boolean
  result: unknown
  error: string | null
}

export function ResponsePanel({ loading, result, error }: ResponsePanelProps) {
  return (
    <aside className="response-panel">
      <div className="response-panel-header">
        <h2>Response</h2>
        {loading ? <span className="badge">Loading…</span> : null}
      </div>
      {error ? (
        <pre className="response-error">{error}</pre>
      ) : (
        <pre className="response-ok">
          {result === null || result === undefined
            ? 'Run an action to see the response here.'
            : JSON.stringify(result, null, 2)}
        </pre>
      )}
    </aside>
  )
}
