import { useState } from 'react'
import { atendimentoApi } from '../../api/atendimentoApi'
import { Alert, Button, Card, EmptyState, Field } from '../../components/ui'
import { LIMITES, TIPOS_DIAGNOSTICO } from '../../domain/constants'
import { validarAnamnese, validarDiagnostico, validarSinaisVitais } from '../../domain/validators'
import { useAsyncAction } from '../../hooks/useAsyncAction'
import { useForm } from '../../hooks/useForm'

/** Converte valores numéricos do DTO para string, que é o que inputs controlados esperam. */
const asText = (v) => (v == null ? '' : String(v))

function SinaisVitaisForm({ atendimento, editavel, onAtualizado }) {
  const atual = atendimento.sinaisVitais
  const form = useForm(
    {
      temperaturaC: asText(atual?.temperaturaC),
      frequenciaCardiaca: asText(atual?.frequenciaCardiaca),
      pesoKg: asText(atual?.pesoKg),
    },
    validarSinaisVitais,
  )
  const salvar = useAsyncAction((dados) => atendimentoApi.registrarSinaisVitais(atendimento.id, dados))
  const [salvo, setSalvo] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSalvo(false)
    if (!form.validateAll()) return
    const { values: v } = form
    const { ok, data } = await salvar.run({
      temperaturaC: Number(v.temperaturaC),
      frequenciaCardiaca: Number(v.frequenciaCardiaca),
      pesoKg: Number(v.pesoKg),
    })
    if (ok) {
      onAtualizado(data)
      setSalvo(true)
    }
  }

  const num = (name, step) => (a11y) => (
    <input
      {...a11y}
      name={name}
      type="number"
      inputMode="decimal"
      min="0"
      step={step}
      value={form.values[name]}
      onChange={form.handleChange}
      disabled={!editavel}
    />
  )

  return (
    <Card title="Sinais vitais">
      <form className="form" onSubmit={handleSubmit} noValidate>
        <div className="grid grid--3">
          <Field label="Temperatura (°C)" error={form.errors.temperaturaC}>{num('temperaturaC', '0.1')}</Field>
          <Field label="Freq. cardíaca (bpm)" error={form.errors.frequenciaCardiaca}>
            {num('frequenciaCardiaca', '1')}
          </Field>
          <Field label="Peso (kg)" error={form.errors.pesoKg}>{num('pesoKg', '0.01')}</Field>
        </div>
        <Alert onClose={salvar.clearError}>{salvar.error}</Alert>
        {salvo && <Alert kind="success">Sinais vitais salvos.</Alert>}
        {editavel && (
          <div className="row row--end">
            <Button type="submit" loading={salvar.loading}>
              {atual ? 'Atualizar' : 'Registrar'}
            </Button>
          </div>
        )}
      </form>
    </Card>
  )
}

function AnamneseForm({ atendimento, editavel, onAtualizado }) {
  const atual = atendimento.anamnese
  const form = useForm(
    { queixaPrincipal: atual?.queixaPrincipal ?? '', historicoClinico: atual?.historicoClinico ?? '' },
    validarAnamnese,
  )
  const salvar = useAsyncAction((dados) => atendimentoApi.registrarAnamnese(atendimento.id, dados))
  const [salvo, setSalvo] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setSalvo(false)
    if (!form.validateAll()) return
    const { ok, data } = await salvar.run({
      queixaPrincipal: form.values.queixaPrincipal.trim(),
      historicoClinico: form.values.historicoClinico.trim(),
    })
    if (ok) {
      onAtualizado(data)
      setSalvo(true)
    }
  }

  return (
    <Card title="Anamnese">
      <form className="form" onSubmit={handleSubmit} noValidate>
        <Field
          label="Queixa principal"
          error={form.errors.queixaPrincipal}
          maxLength={LIMITES.ANAMNESE_QUEIXA}
          value={form.values.queixaPrincipal}
        >
          {(a11y) => (
            <input
              {...a11y}
              name="queixaPrincipal"
              value={form.values.queixaPrincipal}
              onChange={form.handleChange}
              disabled={!editavel}
            />
          )}
        </Field>
        <Field
          label="Histórico clínico"
          error={form.errors.historicoClinico}
          maxLength={LIMITES.ANAMNESE_HISTORICO}
          value={form.values.historicoClinico}
        >
          {(a11y) => (
            <textarea
              {...a11y}
              rows={5}
              name="historicoClinico"
              value={form.values.historicoClinico}
              onChange={form.handleChange}
              disabled={!editavel}
            />
          )}
        </Field>
        <Alert onClose={salvar.clearError}>{salvar.error}</Alert>
        {salvo && <Alert kind="success">Anamnese salva.</Alert>}
        {editavel && (
          <div className="row row--end">
            <Button type="submit" loading={salvar.loading}>
              {atual ? 'Atualizar' : 'Registrar'}
            </Button>
          </div>
        )}
      </form>
    </Card>
  )
}

const DIAGNOSTICO_VAZIO = { codigo: '', descricao: '', tipo: TIPOS_DIAGNOSTICO[0].value }

function DiagnosticosSection({ atendimento, editavel, onAtualizado }) {
  const form = useForm(DIAGNOSTICO_VAZIO, validarDiagnostico)
  const salvar = useAsyncAction((dados) => atendimentoApi.registrarDiagnostico(atendimento.id, dados))
  const tipoLabel = (v) => TIPOS_DIAGNOSTICO.find((t) => t.value === v)?.label ?? v

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.validateAll()) return
    const { ok, data } = await salvar.run({
      codigo: form.values.codigo.trim(),
      descricao: form.values.descricao.trim(),
      tipo: form.values.tipo,
    })
    if (ok) {
      onAtualizado(data)
      form.reset()
    }
  }

  return (
    <Card title="Diagnósticos">
      {atendimento.diagnosticos.length === 0 ? (
        <EmptyState>Nenhum diagnóstico registrado. É necessário ao menos um para concluir.</EmptyState>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Código</th>
              <th>Descrição</th>
              <th>Tipo</th>
            </tr>
          </thead>
          <tbody>
            {atendimento.diagnosticos.map((d, i) => (
              // Diagnostico é um record sem ID próprio; o índice é estável
              // porque a lista só cresce (não há remoção no domínio).
              <tr key={`${d.codigo}-${i}`}>
                <td className="mono">{d.codigo}</td>
                <td>{d.descricao}</td>
                <td>{tipoLabel(d.tipo)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {editavel && (
        <form className="form form--divided" onSubmit={handleSubmit} noValidate>
          <div className="grid grid--diag">
            <Field label="Código" error={form.errors.codigo} maxLength={LIMITES.DIAGNOSTICO_CODIGO} value={form.values.codigo}>
              {(a11y) => <input {...a11y} name="codigo" value={form.values.codigo} onChange={form.handleChange} />}
            </Field>
            <Field
              label="Descrição"
              error={form.errors.descricao}
              maxLength={LIMITES.DIAGNOSTICO_DESCRICAO}
              value={form.values.descricao}
            >
              {(a11y) => <input {...a11y} name="descricao" value={form.values.descricao} onChange={form.handleChange} />}
            </Field>
            <Field label="Tipo" error={form.errors.tipo}>
              {(a11y) => (
                <select {...a11y} name="tipo" value={form.values.tipo} onChange={form.handleChange}>
                  {TIPOS_DIAGNOSTICO.map((t) => (
                    <option key={t.value} value={t.value}>
                      {t.label}
                    </option>
                  ))}
                </select>
              )}
            </Field>
          </div>
          <Alert onClose={salvar.clearError}>{salvar.error}</Alert>
          <div className="row row--end">
            <Button type="submit" loading={salvar.loading}>
              Adicionar diagnóstico
            </Button>
          </div>
        </form>
      )}
    </Card>
  )
}

export function ClinicoTab(props) {
  return (
    <div className="stack">
      <div className="grid grid--2 grid--start">
        <SinaisVitaisForm {...props} />
        <AnamneseForm {...props} />
      </div>
      <DiagnosticosSection {...props} />
    </div>
  )
}
