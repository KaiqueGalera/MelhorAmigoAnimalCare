import { HORARIO_EXAME, LIMITES } from './constants'

/**
 * Validadores puros (sem React): recebem os valores do formulário e
 * devolvem um objeto { campo: 'mensagem' }. Objeto vazio = válido.
 * Funções puras são fáceis de testar e de reaproveitar.
 */

// AnimalId, AgendamentoId e ExameId encapsulam java.util.UUID no back-end.
export const isUuid = (v) =>
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(String(v ?? '').trim())

const blank = (v) => v == null || String(v).trim() === ''
const positive = (v) => !blank(v) && Number(v) > 0
const integer = (v) => Number.isInteger(Number(v))

export const isValid = (errors) => Object.keys(errors).length === 0

export function validarCadastro({ name, lastname, email, password, confirmPassword }) {
  const e = {}
  if (blank(name)) e.name = 'Informe o nome.'
  if (blank(lastname)) e.lastname = 'Informe o sobrenome.'
  if (blank(email)) e.email = 'Informe o e-mail.'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) e.email = 'E-mail inválido.'
  if (blank(password)) e.password = 'Informe a senha.'
  else if (password.length < 6) e.password = 'A senha deve ter ao menos 6 caracteres.'
  if (password !== confirmPassword) e.confirmPassword = 'As senhas não conferem.'
  return e
}

export function validarLogin({ email, password }) {
  const e = {}
  if (blank(email)) e.email = 'Informe o e-mail.'
  if (blank(password)) e.password = 'Informe a senha.'
  return e
}

export function validarSinaisVitais({ temperaturaC, frequenciaCardiaca, pesoKg }) {
  const e = {}
  if (!positive(temperaturaC)) e.temperaturaC = 'Temperatura deve ser maior que zero.'
  if (!positive(frequenciaCardiaca)) e.frequenciaCardiaca = 'Frequência cardíaca deve ser maior que zero.'
  else if (!integer(frequenciaCardiaca)) e.frequenciaCardiaca = 'Use um número inteiro (bpm).'
  if (!positive(pesoKg)) e.pesoKg = 'Peso deve ser maior que zero.'
  return e
}

export function validarAnamnese({ queixaPrincipal, historicoClinico }) {
  const e = {}
  if (blank(queixaPrincipal)) e.queixaPrincipal = 'A queixa principal é obrigatória.'
  else if (queixaPrincipal.length > LIMITES.ANAMNESE_QUEIXA)
    e.queixaPrincipal = `Máximo de ${LIMITES.ANAMNESE_QUEIXA} caracteres.`
  if (blank(historicoClinico)) e.historicoClinico = 'O histórico clínico é obrigatório.'
  else if (historicoClinico.length > LIMITES.ANAMNESE_HISTORICO)
    e.historicoClinico = `Máximo de ${LIMITES.ANAMNESE_HISTORICO} caracteres.`
  return e
}

export function validarDiagnostico({ codigo, descricao, tipo }) {
  const e = {}
  if (blank(codigo)) e.codigo = 'O código é obrigatório.'
  else if (codigo.length > LIMITES.DIAGNOSTICO_CODIGO) e.codigo = `Máximo de ${LIMITES.DIAGNOSTICO_CODIGO} caracteres.`
  if (blank(descricao)) e.descricao = 'A descrição é obrigatória.'
  else if (descricao.length > LIMITES.DIAGNOSTICO_DESCRICAO)
    e.descricao = `Máximo de ${LIMITES.DIAGNOSTICO_DESCRICAO} caracteres.`
  if (blank(tipo)) e.tipo = 'Selecione o tipo.'
  return e
}

/** Valida um item; devolve os erros com o mesmo formato de campo. */
export function validarItemPrescricao(item) {
  const e = {}
  if (blank(item.medicamento)) e.medicamento = 'Obrigatório.'
  if (!positive(item.dosagem) || !integer(item.dosagem)) e.dosagem = 'Inteiro maior que zero.'
  if (blank(item.via)) e.via = 'Obrigatória.'
  if (blank(item.frequencia)) e.frequencia = 'Obrigatória.'
  if (!positive(item.diasDuracao) || !integer(item.diasDuracao)) e.diasDuracao = 'Inteiro maior que zero.'
  return e
}

/** Regras de AtendimentoService.solicitarExames: data futura, entre 09:00 e 18:00. */
export function validarExameSolicitado({ nome, data }, agora = new Date()) {
  const e = {}
  if (blank(nome)) e.nome = 'Informe o nome do exame.'
  if (blank(data)) {
    e.data = 'Informe data e hora.'
  } else if (data.length < 11 || data.startsWith('T')) {
    e.data = 'Selecione a data.'
  } else if (data.length < 16) {
    e.data = 'Selecione o horário.'
  } else {
    const quando = new Date(data)
    const hora = data.slice(11, 16)
    if (Number.isNaN(quando.getTime())) e.data = 'Data inválida.'
    else if (quando < agora) e.data = 'A data deve ser futura.'
    else if (hora < HORARIO_EXAME.inicio || hora > HORARIO_EXAME.fim)
      e.data = `Horário permitido: ${HORARIO_EXAME.inicio} às ${HORARIO_EXAME.fim}.`
  }
  return e
}
