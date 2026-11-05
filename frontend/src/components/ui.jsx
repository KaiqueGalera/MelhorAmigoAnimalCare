import { useId } from 'react'
import { STATUS_LABEL } from '../domain/constants'

/**
 * Componentes de interface pequenos e sem estado.
 * Field associa label e mensagem de erro ao input via id/aria-*,
 * o que deixa os formulários acessíveis a leitores de tela.
 */

export function Field({ label, error, hint, maxLength, value, children, className = '' }) {
  const id = useId()
  const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined
  const count = maxLength != null && typeof value === 'string' ? value.length : null

  return (
    <div className={`field ${error ? 'field--error' : ''} ${className}`}>
      <div className="field__top">
        <label htmlFor={id}>{label}</label>
        {count != null && (
          <span className={`field__count ${count > maxLength ? 'is-over' : ''}`}>
            {count}/{maxLength}
          </span>
        )}
      </div>
      {children({ id, 'aria-invalid': Boolean(error), 'aria-describedby': describedBy })}
      {error ? (
        <p id={`${id}-error`} className="field__error">
          {error}
        </p>
      ) : hint ? (
        <p id={`${id}-hint`} className="field__hint">
          {hint}
        </p>
      ) : null}
    </div>
  )
}

export function Alert({ kind = 'error', children, onClose }) {
  if (!children) return null
  return (
    <div className={`alert alert--${kind}`} role={kind === 'error' ? 'alert' : 'status'}>
      <span>{children}</span>
      {onClose && (
        <button type="button" className="alert__close" onClick={onClose} aria-label="Fechar aviso">
          ×
        </button>
      )}
    </div>
  )
}

export function Button({ variant = 'primary', loading = false, children, disabled, ...props }) {
  return (
    <button
      type="button"
      className={`btn btn--${variant}`}
      disabled={disabled || loading}
      aria-busy={loading}
      {...props}
    >
      {loading && <span className="spinner" aria-hidden="true" />}
      {children}
    </button>
  )
}

export function StatusBadge({ status }) {
  if (!status) return null
  return <span className={`badge badge--${status.toLowerCase()}`}>{STATUS_LABEL[status] ?? status}</span>
}

export function Card({ title, actions, children, className = '' }) {
  return (
    <section className={`card ${className}`}>
      {(title || actions) && (
        <header className="card__header">
          {title && <h2>{title}</h2>}
          {actions && <div className="card__actions">{actions}</div>}
        </header>
      )}
      {children}
    </section>
  )
}

export function EmptyState({ children }) {
  return <p className="empty">{children}</p>
}

export function Loading({ label = 'Carregando…' }) {
  return (
    <div className="loading" role="status">
      <span className="spinner spinner--lg" aria-hidden="true" />
      {label}
    </div>
  )
}
