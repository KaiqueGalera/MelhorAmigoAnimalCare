/**
 * Mapa único de rotas do back-end.
 *
 * Espelha AtendimentoController, PrescricaoController e UserController do back-end.
 * Se uma rota mudar no back-end, ajuste SOMENTE este arquivo — nenhuma tela muda.
 */

const API = '/api/v1'
const enc = encodeURIComponent

export const endpoints = {
  auth: {
    register: `${API}/register`,
    authenticate: `${API}/authenticate`,
  },

  atendimentos: {
    // abrirAtendimentoComAgendamento(animalId, agendamentoId)
    abrirComAgendamento: `${API}/atendimentos/agendados`,
    // abrirProntoAtendimento(animalId)
    abrirProntoAtendimento: `${API}/atendimentos/pronto-atendimento`,
    porId: (id) => `${API}/atendimentos/${enc(id)}`,
    cancelar: (id) => `${API}/atendimentos/${enc(id)}/cancelamento`,
    concluir: (id) => `${API}/atendimentos/${enc(id)}/conclusao`,
    reabrir: (id) => `${API}/atendimentos/${enc(id)}/reabertura`,

    sinaisVitais: (id) => `${API}/atendimentos/${enc(id)}/sinais-vitais`,
    anamnese: (id) => `${API}/atendimentos/${enc(id)}/anamnese`,
    diagnosticos: (id) => `${API}/atendimentos/${enc(id)}/diagnosticos`,

    exames: (id) => `${API}/atendimentos/${enc(id)}/exames`,
    resultadoExame: (id, exameId) =>
      `${API}/atendimentos/${enc(id)}/exames/${enc(exameId)}/resultado`,
  },

  prescricoes: {
    doAtendimento: (atendimentoId) => `${API}/atendimentos/${enc(atendimentoId)}/prescricoes`,
    item: (atendimentoId, prescricaoId, itemId) =>
      `${API}/atendimentos/${enc(atendimentoId)}/prescricoes/${enc(prescricaoId)}/itens/${enc(itemId)}`,
    finalizar: (atendimentoId, prescricaoId) =>
      `${API}/atendimentos/${enc(atendimentoId)}/prescricoes/${enc(prescricaoId)}/finalizar`,
    cancelar: (atendimentoId, prescricaoId) =>
      `${API}/atendimentos/${enc(atendimentoId)}/prescricoes/${enc(prescricaoId)}/cancelar`,
  },
}
