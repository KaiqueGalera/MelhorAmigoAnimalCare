import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { atendimentoApi } from '../api/atendimentoApi'
import { Alert, Button, Card, EmptyState, Field, StatusBadge } from '../components/ui'
import { formatDateTime, shortId } from '../domain/format'
import { useAsyncAction } from '../hooks/useAsyncAction'
import { rememberAtendimento, useRecentAtendimentos } from '../hooks/useRecentAtendimentos'

const MODO = { PRONTO: 'pronto', AGENDADO: 'agendado' }

/** Valor do select que significa "cadastrar um animal novo" (ID gerado no envio). */
const NOVO_ANIMAL = ''

function abrir({ modo, animalId, agendamentoId }) {
  return modo === MODO.PRONTO
    ? atendimentoApi.abrirProntoAtendimento(animalId)
    : atendimentoApi.abrirComAgendamento(animalId, agendamentoId)
}

export default function DashboardPage() {
  const navigate = useNavigate()
  const { items: recentes, clear } = useRecentAtendimentos()

  const [modo, setModo] = useState(MODO.PRONTO)
  const [animalSelecionado, setAnimalSelecionado] = useState(NOVO_ANIMAL)
  const abertura = useAsyncAction(abrir)

  // Animais já atendidos neste navegador (sem repetição), para reaproveitar o mesmo ID:
  // é isso que mantém a regra "1 atendimento em andamento por animal" e o histórico.
  const animaisRecentes = useMemo(
    () => [...new Set(recentes.map((r) => r.animalId).filter(Boolean))],
    [recentes],
  )

  const [buscaId, setBuscaId] = useState('')

  async function handleAbrir(event) {
    event.preventDefault()
    // IDs gerados no próprio cliente: não há cadastro de animais/agendamentos no back-end,
    // que só exige UUIDs. O agendamento é sempre novo (um agendamento gera só 1 atendimento).
    const animalId = animalSelecionado || crypto.randomUUID()
    const agendamentoId = crypto.randomUUID()

    const { ok, data } = await abertura.run({ modo, animalId, agendamentoId })
    if (ok) {
      rememberAtendimento(data)
      navigate(`/atendimentos/${data.id}`)
    }
  }

  function handleBuscar(event) {
    event.preventDefault()
    const id = buscaId.trim()
    if (id) navigate(`/atendimentos/${encodeURIComponent(id)}`)
  }

  return (
    <div className="stack">
      <div className="page-head">
        <h1>Atendimentos</h1>
        <p className="muted">Abra um novo atendimento ou continue um já iniciado.</p>
      </div>

      <div className="grid grid--2 grid--start">
        <Card title="Abrir atendimento">
          <div className="segmented" role="radiogroup" aria-label="Tipo de abertura">
            {[
              [MODO.PRONTO, 'Pronto atendimento'],
              [MODO.AGENDADO, 'Com agendamento'],
            ].map(([value, label]) => (
              <button
                key={value}
                type="button"
                role="radio"
                aria-checked={modo === value}
                className={modo === value ? 'is-active' : ''}
                onClick={() => setModo(value)}
              >
                {label}
              </button>
            ))}
          </div>

          <Alert onClose={abertura.clearError}>{abertura.error}</Alert>

          <form className="form" onSubmit={handleAbrir} noValidate>
            <Field
              label="Animal"
              hint={
                animalSelecionado === NOVO_ANIMAL
                  ? 'Um identificador novo será gerado automaticamente.'
                  : 'Um animal não pode ter dois atendimentos em andamento ao mesmo tempo.'
              }
            >
              {(a11y) => (
                <select {...a11y} value={animalSelecionado} onChange={(e) => setAnimalSelecionado(e.target.value)}>
                  <option value={NOVO_ANIMAL}>Novo animal</option>
                  {animaisRecentes.map((id) => (
                    <option key={id} value={id}>
                      Animal {shortId(id)}
                    </option>
                  ))}
                </select>
              )}
            </Field>
            {modo === MODO.AGENDADO && (
              <p className="field__hint">O identificador do agendamento é gerado automaticamente.</p>
            )}
            <Button type="submit" loading={abertura.loading}>
              Abrir atendimento
            </Button>
          </form>
        </Card>

        <Card title="Buscar atendimento">
          <form className="form form--inline" onSubmit={handleBuscar}>
            <Field label="ID do atendimento" className="grow">
              {(a11y) => (
                <input {...a11y} value={buscaId} onChange={(e) => setBuscaId(e.target.value)} placeholder="UUID" />
              )}
            </Field>
            <Button type="submit" variant="secondary" disabled={!buscaId.trim()}>
              Abrir
            </Button>
          </form>
        </Card>
      </div>

      <Card
        title="Recentes neste navegador"
        actions={
          recentes.length > 0 && (
            <Button variant="ghost" className="btn btn--ghost btn--sm" onClick={clear}>
              Limpar
            </Button>
          )
        }
      >
        {recentes.length === 0 ? (
          <EmptyState>Nenhum atendimento acessado ainda.</EmptyState>
        ) : (
          <ul className="list">
            {recentes.map((item) => (
              <li key={item.id}>
                <Link to={`/atendimentos/${item.id}`} className="list__row">
                  <span className="mono">{shortId(item.id)}</span>
                  <span>Animal {shortId(item.animalId)}</span>
                  <StatusBadge status={item.status} />
                  <span className="muted small">{formatDateTime(item.vistoEm)}</span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  )
}
