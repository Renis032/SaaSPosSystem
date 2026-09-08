type ResponsePanelProps = {
  loading: boolean
  result: unknown
  error: string | null
}

function isPlainObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function formatPrimitive(value: unknown): string {
  if (value === null) return 'null'
  if (value === undefined) return 'undefined'
  if (typeof value === 'string') return value
  if (typeof value === 'boolean' || typeof value === 'number') return String(value)
  return String(value)
}

function itemLabel(item: unknown, index: number): string {
  if (isPlainObject(item)) {
    if (typeof item.step === 'string' && item.step.trim()) return item.step
    if (typeof item.name === 'string' && item.name.trim()) return item.name
    if (item.id !== undefined && item.id !== null) return `id ${String(item.id)}`
  }
  return `[${index}]`
}

function JsonNode({
  label,
  value,
  depth = 0,
  defaultOpen = false,
}: {
  label?: string
  value: unknown
  depth?: number
  defaultOpen?: boolean
}) {
  if (Array.isArray(value)) {
    const summary = label
      ? `${label} (${value.length})`
      : `Array (${value.length})`
    return (
      <details className="json-tree-node" open={defaultOpen || depth < 1}>
        <summary className="json-tree-summary">
          <span className="json-tree-key">{summary}</span>
        </summary>
        <div className="json-tree-children">
          {value.length === 0 ? (
            <div className="json-tree-empty">empty</div>
          ) : (
            value.map((item, index) => (
              <JsonNode
                key={index}
                label={itemLabel(item, index)}
                value={item}
                depth={depth + 1}
                defaultOpen={false}
              />
            ))
          )}
        </div>
      </details>
    )
  }

  if (isPlainObject(value)) {
    const entries = Object.entries(value)
    const summary = label ? `${label}` : 'Object'
    const countLabel = entries.length === 1 ? '1 field' : `${entries.length} fields`
    const okStatus = typeof value.ok === 'boolean' ? value.ok : null
    return (
      <details className="json-tree-node" open={defaultOpen || depth < 1}>
        <summary className="json-tree-summary">
          <span className="json-tree-key">{summary}</span>
          {okStatus !== null ? (
            <span className={okStatus ? 'json-tree-status ok' : 'json-tree-status fail'}>
              {okStatus ? 'ok' : 'failed'}
            </span>
          ) : (
            <span className="json-tree-meta">{countLabel}</span>
          )}
        </summary>
        <div className="json-tree-children">
          {entries.length === 0 ? (
            <div className="json-tree-empty">empty</div>
          ) : (
            entries.map(([key, child]) => (
              <JsonNode
                key={key}
                label={key}
                value={child}
                depth={depth + 1}
                defaultOpen={false}
              />
            ))
          )}
        </div>
      </details>
    )
  }

  return (
    <div className="json-tree-leaf">
      {label ? <span className="json-tree-key">{label}</span> : null}
      <span
        className={`json-tree-value json-tree-value--${value === null ? 'null' : typeof value}`}
      >
        {formatPrimitive(value)}
      </span>
    </div>
  )
}

function StructuredResult({ result }: { result: unknown }) {
  if (isPlainObject(result)) {
    const entries = Object.entries(result)
    return (
      <div className="json-tree">
        {entries.map(([key, value]) => (
          <JsonNode key={key} label={key} value={value} defaultOpen={key === 'steps'} />
        ))}
      </div>
    )
  }

  if (Array.isArray(result)) {
    return (
      <div className="json-tree">
        <JsonNode value={result} defaultOpen />
      </div>
    )
  }

  return <pre className="response-ok">{formatPrimitive(result)}</pre>
}

export function ResponsePanel({ loading, result, error }: ResponsePanelProps) {
  const hasResult = result !== null && result !== undefined
  const rawMessage = hasResult
    ? JSON.stringify(result, null, 2)
    : error
      ? error
      : null

  return (
    <aside className="response-panel">
      <div className="response-panel-header">
        <h2>Response</h2>
        {loading ? <span className="badge">Loading…</span> : null}
      </div>

      <div className="response-panel-body">
        {error ? <div className="response-error-banner">{error}</div> : null}

        {!hasResult && !error ? (
          <p className="response-placeholder">Run an action to see the response here.</p>
        ) : null}

        {hasResult ? <StructuredResult result={result} /> : null}

        {rawMessage ? (
          <details className="response-raw">
            <summary>Raw message</summary>
            <pre className={error && !hasResult ? 'response-error' : 'response-ok'}>
              {rawMessage}
            </pre>
          </details>
        ) : null}
      </div>
    </aside>
  )
}
