import { useState } from 'react'
import { atendimentoApi } from '../../api/atendimentoApi'
import { DateTimePicker } from '../../components/DateTimePicker'
import { Alert, Button, Card, EmptyState, Field, StatusBadge } from '../../components/ui'
import { ExameStatus, HORARIO_EXAME } from '../../domain/constants'
import { formatDateTime, toLocalDateTimePayload } from '../../domain/format'
import { isValid, validarExameSolicitado } from '../../domain/validators'
import { useAsyncAction } from '../../hooks/useAsyncAction'

// Não há catálogo de exames no back-end: o código (UUID) é gerado aqui, no envio.
const novaLinha = () => ({ _key: crypto.randomUUID(), nome: '', data: '' })

function SolicitarExames({ atendimento, onAtualizado }) {
  const [linhas, setLinhas] = useState([novaLinha()])
  const [erros, setErros] = useState({})
  const solicitar = useAsyncAction((exames) => atendimentoApi.solicitarExames(atendimento.id, exames))

  const atualizar = (key, campo, valor) => {
    setLinhas((ls) => ls.map((l) => (l._key === key ? { ...l, [campo]: valor } : l)))
    setErros((prev) => ({ ...prev, [key]: { ...prev[key], [campo]: undefined } }))
  }
  const set = (key, campo) => (e) => atualizar(key, campo, e.target.value)

  async function handleSubmit(e) {
    e.preventDefault()
    const agora = new Date()
    const novosErros = {}
    linhas.forEach((l) => {
      const err = validarExameSolicitado(l, agora)
      if (!isValid(err)) novosErros[l._key] = err
    })
    setErros(novosErros)
    if (!isValid(novosErros)) return

    const { ok, data } = await solicitar.run(
      linhas.map((l) => ({ exameId: crypto.randomUUID(), nome: l.nome.trim(), data: toLocalDateTimePayload(l.data) })),
    )
    if (ok) {
      onAtualizado(data)
      setLinhas([novaLinha()])
    }
  }

  return (
    <Card title="Solicitar exames">
      <form className="form" onSubmit={handleSubmit} noValidate>
        <p className="field__hint">
          Exames devem ser agendados para uma data futura, entre {HORARIO_EXAME.inicio} e {HORARIO_EXAME.fim}.
        </p>
        {linhas.map((l, i) => {
          const err = erros[l._key] ?? {}
          return (
            <fieldset key={l._key} className="item-row">
              <legend>Exame {i + 1}</legend>
              <div className="grid grid--exame">
                <Field label="Nome do exame" error={err.nome}>
                  {(a) => <input {...a} placeholder="ex.: Hemograma completo" value={l.nome} onChange={set(l._key, 'nome')} />}
                </Field>
                <Field label="Data e hora" error={err.data}>
                  {(a) => (
                    <DateTimePicker
                      {...a}
                      value={l.data}
                      onChange={(v) => atualizar(l._key, 'data', v)}
                      minDate={new Date()}
                      horaInicio={HORARIO_EXAME.inicio}
                      horaFim={HORARIO_EXAME.fim}
                    />
                  )}
                </Field>
              </div>
              {linhas.length > 1 && (
                <Button
                  variant="ghost"
                  className="btn btn--ghost btn--sm"
                  onClick={() => setLinhas((ls) => ls.filter((x) => x._key !== l._key))}
                >
                  Remover
                </Button>
              )}
            </fieldset>
          )
        })}
        <Alert onClose={solicitar.clearError}>{solicitar.error}</Alert>
        <div className="row row--between">
          <Button variant="secondary" onClick={() => setLinhas((ls) => [...ls, novaLinha()])}>
            + Adicionar exame
          </Button>
          <Button type="submit" loading={solicitar.loading}>
            Solicitar
          </Button>
        </div>
      </form>
    </Card>
  )
}

function ResultadoForm({ atendimentoId, exame, onRecarregar }) {
  const [resultado, setResultado] = useState('')
  const [erro, setErro] = useState(null)
  const salvar = useAsyncAction((texto) => atendimentoApi.registrarResultadoExame(atendimentoId, exame.exameId, texto))

  async function handleSubmit(e) {
    e.preventDefault()
    if (!resultado.trim()) {
      setErro('O resultado deve ser informado.')
      return
    }
    const { ok } = await salvar.run(resultado.trim())
    if (ok) onRecarregar()
  }

  return (
    <form className="form form--compact" onSubmit={handleSubmit} noValidate>
      <Field label="Resultado" error={erro}>
        {(a) => (
          <textarea
            {...a}
            rows={2}
            value={resultado}
            onChange={(e) => {
              setResultado(e.target.value)
              setErro(null)
            }}
          />
        )}
      </Field>
      <Alert onClose={salvar.clearError}>{salvar.error}</Alert>
      <div className="row row--end">
        <Button type="submit" className="btn btn--primary btn--sm" loading={salvar.loading}>
          Registrar resultado
        </Button>
      </div>
    </form>
  )
}

export function ExamesTab({ atendimento, editavel, onAtualizado, onRecarregar }) {
  const exames = [...atendimento.exames].sort((a, b) => String(a.data).localeCompare(String(b.data)))

  return (
    <div className="stack">
      {editavel && <SolicitarExames atendimento={atendimento} onAtualizado={onAtualizado} />}

      <Card title="Exames solicitados">
        {exames.length === 0 ? (
          <EmptyState>Nenhum exame solicitado.</EmptyState>
        ) : (
          <ul className="exam-list">
            {exames.map((ex) => (
              <li key={`${ex.exameId}-${ex.data}`} className="exam-item">
                <div className="exam-item__head">
                  <div>
                    <strong>{ex.nome || 'Exame'}</strong> <span className="mono muted small">#{ex.exameId}</span>
                    <div className="muted small">Agendado para {formatDateTime(ex.data)}</div>
                  </div>
                  <StatusBadge status={ex.status} />
                </div>
                {ex.status === ExameStatus.CONCLUIDO ? (
                  <p className="exam-item__result">{ex.resultado}</p>
                ) : (
                  <ResultadoForm atendimentoId={atendimento.id} exame={ex} onRecarregar={onRecarregar} />
                )}
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  )
}
