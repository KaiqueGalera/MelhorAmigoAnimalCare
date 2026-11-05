import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { atendimentoApi } from '../api/atendimentoApi'
import { Alert, Button, Loading } from '../components/ui'
import { StatusAtendimento } from '../domain/constants'
import { rememberAtendimento } from '../hooks/useRecentAtendimentos'
import { AtendimentoHeader } from './atendimento/AtendimentoHeader'
import { ClinicoTab } from './atendimento/ClinicoTab'
import { PrescricoesTab } from './atendimento/PrescricoesTab'
import { ExamesTab } from './atendimento/ExamesTab'

const TABS = [
  { id: 'clinico', label: 'Registro clínico' },
  { id: 'prescricoes', label: 'Prescrições' },
  { id: 'exames', label: 'Exames' },
]

/**
 * Atendimento é a raiz de agregado do domínio: sinais vitais, diagnósticos,
 * prescrições e exames pertencem a ele. Por isso a página mantém UM objeto
 * `atendimento` como fonte da verdade e o recarrega após cada alteração,
 * em vez de cada aba guardar sua própria cópia (que poderia ficar inconsistente,
 * ex.: a checklist de conclusão não enxergar uma prescrição recém-finalizada).
 */
export default function AtendimentoPage() {
  const { id } = useParams()
  const [atendimento, setAtendimento] = useState(null)
  const [loadError, setLoadError] = useState(null)
  const [tab, setTab] = useState('clinico')

  const aplicar = useCallback((atualizado) => {
    setAtendimento(atualizado)
    rememberAtendimento(atualizado)
  }, [])

  const recarregar = useCallback(
    async (signal) => {
      try {
        const dados = await atendimentoApi.buscar(id, { signal })
        aplicar(dados)
        setLoadError(null)
      } catch (err) {
        if (err.name !== 'AbortError') setLoadError(err.message)
      }
    },
    [id, aplicar],
  )

  useEffect(() => {
    // AbortController cancela a requisição se o usuário trocar de atendimento
    // antes da resposta chegar — evita mostrar dados do atendimento errado.
    const controller = new AbortController()
    atendimentoApi
      .buscar(id, { signal: controller.signal })
      .then((dados) => {
        aplicar(dados)
        setLoadError(null)
      })
      .catch((err) => {
        if (err.name !== 'AbortError') setLoadError(err.message)
      })
    return () => controller.abort()
  }, [id, aplicar])

  if (loadError && !atendimento) {
    return (
      <div className="stack">
        <Alert>{loadError}</Alert>
        <div className="row">
          <Button variant="secondary" onClick={() => recarregar()}>
            Tentar novamente
          </Button>
          <Link to="/" className="btn btn--ghost">
            Voltar
          </Link>
        </div>
      </div>
    )
  }

  if (!atendimento) return <Loading label="Carregando atendimento…" />

  const editavel = atendimento.status === StatusAtendimento.EM_ANDAMENTO
  const props = { atendimento, editavel, onAtualizado: aplicar, onRecarregar: () => recarregar() }

  return (
    <div className="stack">
      <Link to="/" className="back-link">
        ← Atendimentos
      </Link>

      <AtendimentoHeader {...props} />

      {!editavel && (
        <Alert kind="info">
          Este atendimento está {atendimento.status === StatusAtendimento.CANCELADO ? 'cancelado' : 'concluído'} e
          não aceita novos registros clínicos.
        </Alert>
      )}

      <div className="tabs" role="tablist" aria-label="Seções do atendimento">
        {TABS.map((t) => (
          <button
            key={t.id}
            role="tab"
            type="button"
            id={`tab-${t.id}`}
            aria-selected={tab === t.id}
            aria-controls={`panel-${t.id}`}
            className={tab === t.id ? 'is-active' : ''}
            onClick={() => setTab(t.id)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* key = id: ao navegar para outro atendimento, os formulários são recriados
          do zero em vez de manterem valores digitados do atendimento anterior. */}
      <div role="tabpanel" id={`panel-${tab}`} aria-labelledby={`tab-${tab}`} key={atendimento.id}>
        {tab === 'clinico' && <ClinicoTab {...props} />}
        {tab === 'prescricoes' && <PrescricoesTab {...props} />}
        {tab === 'exames' && <ExamesTab {...props} />}
      </div>
    </div>
  )
}
