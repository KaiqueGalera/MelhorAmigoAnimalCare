/**
 * Anticorrupção entre o JSON do back-end e o modelo usado nas telas.
 *
 * Os IDs do domínio são value objects (AtendimentoId, PrescricaoId...).
 * Dependendo de como o Jackson os serializa, chegam como "uuid" ou
 * { value: "uuid" } / { id: "uuid" }. Normalizar aqui evita espalhar
 * esse detalhe por todos os componentes.
 */

export function idOf(raw) {
  if (raw == null) return null
  if (typeof raw === 'string' || typeof raw === 'number') return String(raw)
  return idOf(raw.value ?? raw.id ?? raw.uuid ?? null)
}

const asArray = (value) => (Array.isArray(value) ? value : [])

export function toItemPrescricao(dto) {
  return {
    id: idOf(dto.id),
    medicamento: dto.medicamento ?? '',
    dosagem: dto.dosagem ?? '',
    via: dto.via ?? '',
    frequencia: dto.frequencia ?? '',
    diasDuracao: dto.diasDuracao ?? '',
  }
}

export function toPrescricao(dto) {
  return {
    id: idOf(dto.id),
    status: dto.status,
    dataHoraEmissao: dto.dataHoraEmissao ?? null,
    itens: asArray(dto.itens).map(toItemPrescricao),
  }
}

export function toExameSolicitado(dto) {
  const exame = dto.exame ?? {}
  return {
    exameId: idOf(exame.id ?? dto.exameId),
    nome: exame.nome ?? dto.nome ?? '',
    data: dto.data ?? null,
    resultado: dto.resultado ?? '',
    status: dto.status ?? 'PENDENTE',
  }
}

export function toAtendimento(dto) {
  return {
    id: idOf(dto.id),
    animalId: idOf(dto.animalId),
    agendamentoId: idOf(dto.agendamentoId),
    status: dto.status,
    dataHoraAtendimento: dto.dataHoraAtendimento ?? null,
    dataHoraConclusao: dto.dataHoraConclusao ?? null,
    justificativa: dto.justificativa ?? null,
    sinaisVitais: dto.sinaisVitais ?? null,
    anamnese: dto.anamnese ?? null,
    diagnosticos: asArray(dto.diagnosticos),
    prescricoes: asArray(dto.prescricoes).map(toPrescricao),
    exames: asArray(dto.exames).map(toExameSolicitado),
  }
}
