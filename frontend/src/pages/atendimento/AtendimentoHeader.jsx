import { useState } from 'react'
import { atendimentoApi } from '../../api/atendimentoApi'
import { ConfirmDialog } from '../../components/ConfirmDialog'
import { Alert, Button, Card, Field, StatusBadge } from '../../components/ui'
import { JANELA_REABERTURA_MS, StatusAtendimento, StatusPrescricao } from '../../domain/constants'
import { formatDateTime } from '../../domain/format'
import { useAsyncAction } from '../../hooks/useAsyncAction'

/**
 * Pré-condições de Atendimento.concluir(), avaliadas no cliente
 * para mostrar ao usuário o que falta ANTES de ele tentar concluir.
 */
function pendenciasConclusao(at) {
  return [
    { ok: Boolean(at.sinaisVitais), label: 'Sinais vitais registrados' },
    { ok: at.diagnosticos.length > 0, label: 'Ao menos um diagnóstico' },
    {
      ok: !at.prescricoes.some((p) => p.status === StatusPrescricao.ABERTA),
      label: 'Nenhuma prescrição aberta',
    },
  ]
}

function podeReabrir(at, agora = Date.now()) {
  if (at.status !== StatusAtendimento.CONCLUIDO || !at.dataHoraConclusao) return false
  return agora - new Date(at.dataHoraConclusao).getTime() <= JANELA_REABERTURA_MS
}

export function AtendimentoHeader({ atendimento: at, editavel, onAtualizado }) {
  const [dialogo, setDialogo] = useState(null) // 'concluir' | 'cancelar' | 'reabrir' | null
  const [justificativa, setJustificativa] = useState('')
  const [justificativaErro, setJustificativaErro] = useState(null)

  const acao = useAsyncAction(async (tipo) => {
    if (tipo === 'concluir') return atendimentoApi.concluir(at.id)
    if (tipo === 'reabrir') return atendimentoApi.reabrir(at.id)
    return atendimentoApi.cancelar(at.id, justificativa.trim())
  })

  const checklist = pendenciasConclusao(at)
  const prontoParaConcluir = checklist.every((c) => c.ok)

  function fechar() {
    setDialogo(null)
    setJustificativa('')
    setJustificativaErro(null)
    acao.clearError()
  }

  async function confirmar() {
    if (dialogo === 'cancelar' && !justificativa.trim()) {
      setJustificativaErro('A justificativa é obrigatória para cancelar.')
      return
    }
    const { ok, data } = await acao.run(dialogo)
    if (ok) {
      onAtualizado(data)
      fechar()
    }
  }

  return (
    <Card className="header-card">
      <div className="header-card__top">
        <div>
          <p className="eyebrow">Atendimento</p>
          <h1 className="mono h-id">{at.id}</h1>
        </div>
        <StatusBadge status={at.status} />
      </div>

      <dl className="meta">
        <div>
          <dt>Animal</dt>
          <dd className="mono">{at.animalId ?? '—'}</dd>
        </div>
        <div>
          <dt>Origem</dt>
          <dd>{at.agendamentoId ? <span className="mono">Agendamento {at.agendamentoId}</span> : 'Pronto atendimento'}</dd>
        </div>
        <div>
          <dt>Aberto em</dt>
          <dd>{formatDateTime(at.dataHoraAtendimento)}</dd>
        </div>
        {at.dataHoraConclusao && (
          <div>
            <dt>{at.status === StatusAtendimento.CANCELADO ? 'Cancelado em' : 'Concluído em'}</dt>
            <dd>{formatDateTime(at.dataHoraConclusao)}</dd>
          </div>
        )}
        {at.justificativa && (
          <div className="meta__wide">
            <dt>Justificativa do cancelamento</dt>
            <dd>{at.justificativa}</dd>
          </div>
        )}
      </dl>

      {editavel && (
        <ul className="checklist" aria-label="Requisitos para concluir">
          {checklist.map((c) => (
            <li key={c.label} className={c.ok ? 'is-ok' : 'is-pending'}>
              <span aria-hidden="true">{c.ok ? '✓' : '○'}</span> {c.label}
            </li>
          ))}
        </ul>
      )}

      <div className="row row--end">
        {editavel && (
          <>
            <Button variant="danger-ghost" onClick={() => setDialogo('cancelar')}>
              Cancelar atendimento
            </Button>
            <Button
              onClick={() => setDialogo('concluir')}
              disabled={!prontoParaConcluir}
              title={prontoParaConcluir ? undefined : 'Complete os itens da lista para concluir'}
            >
              Concluir atendimento
            </Button>
          </>
        )}
        {podeReabrir(at) && (
          <Button variant="secondary" onClick={() => setDialogo('reabrir')}>
            Reabrir (até 24h)
          </Button>
        )}
      </div>

      <ConfirmDialog
        open={dialogo === 'concluir'}
        title="Concluir atendimento?"
        confirmLabel="Concluir"
        loading={acao.loading}
        onConfirm={confirmar}
        onClose={fechar}
      >
        <p>Após concluído, o atendimento só poderá ser reaberto em até 24 horas.</p>
        <Alert>{acao.error}</Alert>
      </ConfirmDialog>

      <ConfirmDialog
        open={dialogo === 'reabrir'}
        title="Reabrir atendimento?"
        confirmLabel="Reabrir"
        loading={acao.loading}
        onConfirm={confirmar}
        onClose={fechar}
      >
        <p>O atendimento volta ao status “Em andamento” e aceita novos registros.</p>
        <Alert>{acao.error}</Alert>
      </ConfirmDialog>

      <ConfirmDialog
        open={dialogo === 'cancelar'}
        title="Cancelar atendimento"
        confirmLabel="Cancelar atendimento"
        variant="danger"
        loading={acao.loading}
        onConfirm={confirmar}
        onClose={fechar}
      >
        <p>Esta ação é definitiva. Informe o motivo para fins de auditoria.</p>
        <Field label="Justificativa" error={justificativaErro}>
          {(a11y) => (
            <textarea
              {...a11y}
              rows={3}
              value={justificativa}
              onChange={(e) => {
                setJustificativa(e.target.value)
                setJustificativaErro(null)
              }}
            />
          )}
        </Field>
        <Alert>{acao.error}</Alert>
      </ConfirmDialog>
    </Card>
  )
}
