import { http } from './httpClient'
import { endpoints } from './endpoints'
import { toAtendimento, toExameSolicitado } from './mappers'

const ep = endpoints.atendimentos

/** Operações de AtendimentoService expostas pela API REST. */
export const atendimentoApi = {
  buscar: async (id, { signal } = {}) => toAtendimento(await http.get(ep.porId(id), { signal })),

  abrirProntoAtendimento: async (animalId) =>
    toAtendimento(await http.post(ep.abrirProntoAtendimento, { animalId })),

  abrirComAgendamento: async (animalId, agendamentoId) =>
    toAtendimento(await http.post(ep.abrirComAgendamento, { animalId, agendamentoId })),

  cancelar: async (id, justificativa) =>
    toAtendimento(await http.post(ep.cancelar(id), { justificativa })),

  concluir: async (id) => toAtendimento(await http.post(ep.concluir(id))),

  // O instante de reabertura é definido pelo servidor (relógio confiável),
  // nunca pelo navegador — o relógio do cliente pode estar errado ou ser manipulado.
  reabrir: async (id) => toAtendimento(await http.post(ep.reabrir(id))),

  registrarSinaisVitais: async (id, sinais) =>
    toAtendimento(await http.put(ep.sinaisVitais(id), sinais)),

  registrarAnamnese: async (id, anamnese) =>
    toAtendimento(await http.put(ep.anamnese(id), anamnese)),

  registrarDiagnostico: async (id, diagnostico) =>
    toAtendimento(await http.post(ep.diagnosticos(id), diagnostico)),

  /** exames: [{ exameId, nome, data: 'YYYY-MM-DDTHH:mm:ss' }] */
  solicitarExames: async (id, exames) => toAtendimento(await http.post(ep.exames(id), { exames })),

  registrarResultadoExame: async (id, exameId, resultado) =>
    toExameSolicitado(await http.patch(ep.resultadoExame(id, exameId), { resultado })),
}
