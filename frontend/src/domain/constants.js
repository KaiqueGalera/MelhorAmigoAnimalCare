/**
 * Espelho das constantes e enums do domínio Java.
 * Manter os limites iguais aos dos records evita que o usuário preencha um
 * formulário inteiro para só depois receber um 400 do servidor.
 * O back-end continua sendo a fonte da verdade: estas regras são só UX.
 */

export const StatusAtendimento = Object.freeze({
  AGENDADO: 'AGENDADO',
  EM_ANDAMENTO: 'EM_ANDAMENTO',
  CONCLUIDO: 'CONCLUIDO',
  CANCELADO: 'CANCELADO',
})

export const StatusPrescricao = Object.freeze({
  ABERTA: 'ABERTA',
  FINALIZADA: 'FINALIZADA',
  CANCELADA: 'CANCELADA',
})

export const ExameStatus = Object.freeze({
  PENDENTE: 'PENDENTE',
  CONCLUIDO: 'CONCLUIDO',
})

// Espelho de TipoDiagnostico.java
export const TIPOS_DIAGNOSTICO = [
  { value: 'PRESUNTIVO', label: 'Presuntivo' },
  { value: 'EMPIRICO', label: 'Empírico' },
  { value: 'DEFINITIVO', label: 'Definitivo' },
]

export const LIMITES = Object.freeze({
  DIAGNOSTICO_CODIGO: 10, // Diagnostico.LIMITE_CODIGO
  DIAGNOSTICO_DESCRICAO: 200, // Diagnostico.LIMITE_DESCRICAO
  ANAMNESE_QUEIXA: 100, // Anamnese.LIMITE_QUEIXA_PRINCIPAL
  ANAMNESE_HISTORICO: 500, // Anamnese.LIMITE_HISTORICO_CLINICO
})

// AtendimentoService.INICIO_HORARIO_EXAME / FIM_HORARIO_EXAME
export const HORARIO_EXAME = Object.freeze({ inicio: '09:00', fim: '18:00' })

// Atendimento.JANELA_REABERTURA
export const JANELA_REABERTURA_MS = 24 * 60 * 60 * 1000

export const STATUS_LABEL = {
  AGENDADO: 'Agendado',
  EM_ANDAMENTO: 'Em andamento',
  CONCLUIDO: 'Concluído',
  CANCELADO: 'Cancelado',
  ABERTA: 'Aberta',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
  PENDENTE: 'Pendente',
}

export const VIAS_ADMINISTRACAO = ['Oral', 'Subcutânea', 'Intramuscular', 'Intravenosa', 'Tópica', 'Oftálmica', 'Otológica']
