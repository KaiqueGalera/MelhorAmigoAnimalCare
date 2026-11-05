import { useState } from 'react'
import { prescricaoApi } from '../../api/prescricaoApi'
import { ConfirmDialog } from '../../components/ConfirmDialog'
import { Alert, Button, Card, EmptyState, Field, StatusBadge } from '../../components/ui'
import { StatusPrescricao, VIAS_ADMINISTRACAO } from '../../domain/constants'
import { formatDateTime, shortId } from '../../domain/format'
import { isValid, validarItemPrescricao } from '../../domain/validators'
import { useAsyncAction } from '../../hooks/useAsyncAction'

// Chave local para linhas novas: usar o índice do array como `key` faria o React
// misturar os valores digitados ao remover uma linha do meio.
const novaLinha = () => ({
  _key: crypto.randomUUID(),
  medicamento: '',
  dosagem: '',
  via: VIAS_ADMINISTRACAO[0],
  frequencia: '',
  diasDuracao: '',
})

const toForm = (item) => ({ ...item, dosagem: String(item.dosagem), diasDuracao: String(item.diasDuracao) })

/** Campos de um ItemPrescricao — reaproveitado na emissão e na edição. */
function ItemFields({ item, errors = {}, onChange }) {
  const set = (campo) => (e) => onChange({ ...item, [campo]: e.target.value })
  return (
    <div className="grid grid--item">
      <Field label="Medicamento" error={errors.medicamento}>
        {(a) => <input {...a} value={item.medicamento} onChange={set('medicamento')} />}
      </Field>
      <Field label="Dosagem" error={errors.dosagem}>
        {(a) => <input {...a} type="number" min="1" step="1" value={item.dosagem} onChange={set('dosagem')} />}
      </Field>
      <Field label="Via" error={errors.via}>
        {(a) => (
          <select {...a} value={item.via} onChange={set('via')}>
            {VIAS_ADMINISTRACAO.map((v) => (
              <option key={v}>{v}</option>
            ))}
          </select>
        )}
      </Field>
      <Field label="Frequência" error={errors.frequencia}>
        {(a) => <input {...a} placeholder="ex.: 12/12h" value={item.frequencia} onChange={set('frequencia')} />}
      </Field>
      <Field label="Dias" error={errors.diasDuracao}>
        {(a) => <input {...a} type="number" min="1" step="1" value={item.diasDuracao} onChange={set('diasDuracao')} />}
      </Field>
    </div>
  )
}

function EmitirPrescricao({ atendimento, onRecarregar }) {
  const [linhas, setLinhas] = useState([novaLinha()])
  const [erros, setErros] = useState({})
  const emitir = useAsyncAction((itens) => prescricaoApi.emitir(atendimento.id, itens))

  const atualizar = (key, item) => {
    setLinhas((ls) => ls.map((l) => (l._key === key ? item : l)))
    setErros((prev) => {
      const rest = { ...prev }
      delete rest[key]
      return rest
    })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const novosErros = {}
    linhas.forEach((l) => {
      const err = validarItemPrescricao(l)
      if (!isValid(err)) novosErros[l._key] = err
    })
    setErros(novosErros)
    if (!isValid(novosErros)) return

    const { ok } = await emitir.run(linhas)
    if (ok) {
      setLinhas([novaLinha()])
      onRecarregar()
    }
  }

  return (
    <Card title="Emitir nova prescrição">
      <form className="form" onSubmit={handleSubmit} noValidate>
        {linhas.map((linha, i) => (
          <fieldset key={linha._key} className="item-row">
            <legend>Item {i + 1}</legend>
            <ItemFields item={linha} errors={erros[linha._key]} onChange={(it) => atualizar(linha._key, it)} />
            {linhas.length > 1 && (
              <Button
                variant="ghost"
                className="btn btn--ghost btn--sm"
                onClick={() => setLinhas((ls) => ls.filter((l) => l._key !== linha._key))}
              >
                Remover item
              </Button>
            )}
          </fieldset>
        ))}
        <Alert onClose={emitir.clearError}>{emitir.error}</Alert>
        <div className="row row--between">
          <Button variant="secondary" onClick={() => setLinhas((ls) => [...ls, novaLinha()])}>
            + Adicionar item
          </Button>
          <Button type="submit" loading={emitir.loading}>
            Emitir prescrição
          </Button>
        </div>
      </form>
    </Card>
  )
}

function PrescricaoCard({ atendimentoId, prescricao, onRecarregar }) {
  const aberta = prescricao.status === StatusPrescricao.ABERTA
  const [editando, setEditando] = useState(null) // item em edição (forma de formulário)
  const [erroItem, setErroItem] = useState({})
  const [dialogo, setDialogo] = useState(null) // 'finalizar' | 'cancelar'

  const op = useAsyncAction(async ({ tipo, payload }) => {
    if (tipo === 'editar') return prescricaoApi.editarItem(atendimentoId, prescricao.id, payload)
    if (tipo === 'remover') return prescricaoApi.removerItem(atendimentoId, prescricao.id, payload)
    if (tipo === 'finalizar') return prescricaoApi.finalizar(atendimentoId, prescricao.id)
    return prescricaoApi.cancelar(atendimentoId, prescricao.id)
  })

  async function executar(tipo, payload) {
    const { ok } = await op.run({ tipo, payload })
    if (ok) {
      setEditando(null)
      setDialogo(null)
      onRecarregar()
    }
  }

  function salvarEdicao() {
    const err = validarItemPrescricao(editando)
    setErroItem(err)
    if (isValid(err)) executar('editar', editando)
  }

  return (
    <Card
      title={`Prescrição ${shortId(prescricao.id)}`}
      actions={<StatusBadge status={prescricao.status} />}
    >
      <p className="muted small">Emitida em {formatDateTime(prescricao.dataHoraEmissao)}</p>

      {prescricao.itens.length === 0 ? (
        <EmptyState>Sem itens.</EmptyState>
      ) : (
        <ul className="rx-list">
          {prescricao.itens.map((item) =>
            editando?.id === item.id ? (
              <li key={item.id} className="rx-item rx-item--editing">
                <ItemFields item={editando} errors={erroItem} onChange={setEditando} />
                <div className="row row--end">
                  <Button variant="ghost" onClick={() => setEditando(null)}>
                    Descartar
                  </Button>
                  <Button loading={op.loading} onClick={salvarEdicao}>
                    Salvar item
                  </Button>
                </div>
              </li>
            ) : (
              <li key={item.id} className="rx-item">
                <div>
                  <strong>{item.medicamento}</strong> — {item.dosagem} · {item.via}
                  <div className="muted small">
                    {item.frequencia} por {item.diasDuracao} dia(s)
                  </div>
                </div>
                {aberta && (
                  <div className="row">
                    <Button
                      variant="ghost"
                      className="btn btn--ghost btn--sm"
                      onClick={() => {
                        setErroItem({})
                        setEditando(toForm(item))
                      }}
                    >
                      Editar
                    </Button>
                    <Button
                      variant="danger-ghost"
                      className="btn btn--danger-ghost btn--sm"
                      disabled={op.loading}
                      onClick={() => executar('remover', item.id)}
                    >
                      Remover
                    </Button>
                  </div>
                )}
              </li>
            ),
          )}
        </ul>
      )}

      {!dialogo && <Alert onClose={op.clearError}>{op.error}</Alert>}

      {aberta && (
        <div className="row row--end">
          <Button variant="danger-ghost" onClick={() => setDialogo('cancelar')}>
            Cancelar prescrição
          </Button>
          <Button onClick={() => setDialogo('finalizar')} disabled={prescricao.itens.length === 0}>
            Finalizar
          </Button>
        </div>
      )}

      <ConfirmDialog
        open={Boolean(dialogo)}
        title={dialogo === 'finalizar' ? 'Finalizar prescrição?' : 'Cancelar prescrição?'}
        confirmLabel={dialogo === 'finalizar' ? 'Finalizar' : 'Cancelar prescrição'}
        variant={dialogo === 'finalizar' ? 'primary' : 'danger'}
        loading={op.loading}
        onConfirm={() => executar(dialogo)}
        onClose={() => {
          setDialogo(null)
          op.clearError()
        }}
      >
        <p>Depois disso os itens não poderão mais ser alterados.</p>
        <Alert>{op.error}</Alert>
      </ConfirmDialog>
    </Card>
  )
}

export function PrescricoesTab({ atendimento, editavel, onRecarregar }) {
  // Mais recentes primeiro: é a que o veterinário normalmente está editando.
  const prescricoes = [...atendimento.prescricoes].reverse()

  return (
    <div className="stack">
      {editavel && <EmitirPrescricao atendimento={atendimento} onRecarregar={onRecarregar} />}
      {prescricoes.length === 0 ? (
        <Card>
          <EmptyState>Nenhuma prescrição emitida neste atendimento.</EmptyState>
        </Card>
      ) : (
        prescricoes.map((p) => (
          <PrescricaoCard key={p.id} atendimentoId={atendimento.id} prescricao={p} onRecarregar={onRecarregar} />
        ))
      )}
    </div>
  )
}
