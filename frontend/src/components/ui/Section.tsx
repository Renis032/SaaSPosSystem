import type { ReactNode } from 'react'

type SectionProps = {
  title: string
  description?: string
  children: ReactNode
}

export function Section({ title, description, children }: SectionProps) {
  return (
    <section className="panel">
      <header className="panel-header">
        <h2>{title}</h2>
        {description ? <p>{description}</p> : null}
      </header>
      <div className="panel-body">{children}</div>
    </section>
  )
}

type ActionRowProps = {
  title: string
  children: ReactNode
}

export function ActionRow({ title, children }: ActionRowProps) {
  return (
    <div className="action-row">
      <h3>{title}</h3>
      <div className="action-row-content">{children}</div>
    </div>
  )
}
