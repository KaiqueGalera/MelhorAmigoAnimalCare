import { http } from './httpClient'
import { endpoints } from './endpoints'
import { toPrescricao } from './mappers'

const ep = endpoints.prescricoes

/** Converte o formulário (strings) para o payload numérico esperado por ItemPrescricao. */
function toItemPayload(item) {
  return {
    ...(item.id ? { id: item.id } : {}),
    medicamento: item.medicamento.trim(),
    dosagem: Number(item.dosagem),
    via: item.via.trim(),
    frequencia: item.frequencia.trim(),
    diasDuracao: Number(item.diasDuracao),
  }
}

/** Operações de PrescricaoService expostas pela API REST. */
export const prescricaoApi = {
  listar: async (atendimentoId, { signal } = {}) => {
    const lista = await http.get(ep.doAtendimento(atendimentoId), { signal })
    return (Array.isArray(lista) ? lista : []).map(toPrescricao)
  },

  emitir: async (atendimentoId, itens) =>
    toPrescricao(await http.post(ep.doAtendimento(atendimentoId), { itens: itens.map(toItemPayload) })),

  editarItem: (atendimentoId, prescricaoId, item) =>
    http.put(ep.item(atendimentoId, prescricaoId, item.id), toItemPayload(item)),

  removerItem: (atendimentoId, prescricaoId, itemId) =>
    http.delete(ep.item(atendimentoId, prescricaoId, itemId)),

  finalizar: (atendimentoId, prescricaoId) => http.patch(ep.finalizar(atendimentoId, prescricaoId)),

  cancelar: (atendimentoId, prescricaoId) => http.patch(ep.cancelar(atendimentoId, prescricaoId)),
}
