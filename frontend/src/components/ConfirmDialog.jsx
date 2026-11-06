import { useEffect, useRef } from 'react'
import { Button } from './ui'

/**
 * Diálogo de confirmação com <dialog> nativo: já traz foco preso,
 * fechamento por ESC e semântica de modal, sem biblioteca extra.
 * Usado em ações irreversíveis (cancelar, concluir, finalizar).
 */
export function ConfirmDialog({ open, title, children, confirmLabel = 'Confirmar', variant = 'primary', loading, onConfirm, onClose }) {
  const ref = useRef(null)

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  return (
    <dialog ref={ref} className="dialog" onClose={onClose} onCancel={onClose}>
      <form
        method="dialog"
        onSubmit={(e) => {
          e.preventDefault()
          onConfirm()
        }}
      >
        <h3>{title}</h3>
        <div className="dialog__body">{children}</div>
        <div className="dialog__actions">
          <Button variant="ghost" onClick={onClose} disabled={loading}>
            Voltar
          </Button>
          <Button type="submit" variant={variant} loading={loading}>
            {confirmLabel}
          </Button>
        </div>
      </form>
    </dialog>
  )
}
