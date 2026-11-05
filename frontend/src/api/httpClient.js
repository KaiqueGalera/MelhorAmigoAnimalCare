/**
 * Cliente HTTP único da aplicação.
 *
 * Centraliza: URL base, cabeçalho Authorization, serialização JSON,
 * tradução de erros do Spring em mensagens legíveis e tratamento de 401.
 * As telas nunca chamam fetch diretamente — isso evita repetição e
 * garante que toda requisição autenticada siga a mesma regra.
 */

const BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(message, status, details) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

// Injeção de dependência simples: o AuthContext registra como obter o token
// e o que fazer quando a sessão expira. Assim o cliente HTTP não depende do React.
let getToken = () => null
let handleUnauthorized = () => {}

export function configureHttpClient({ tokenProvider, onUnauthorized }) {
  if (tokenProvider) getToken = tokenProvider
  if (onUnauthorized) handleUnauthorized = onUnauthorized
}

const STATUS_MESSAGES = {
  400: 'Dados inválidos. Verifique os campos informados.',
  401: 'Sessão expirada ou credenciais inválidas.',
  403: 'Você não tem permissão para esta operação.',
  404: 'Registro não encontrado.',
  409: 'Conflito: o registro já existe ou está em uso.',
  422: 'A operação viola uma regra de negócio.',
  500: 'Erro interno no servidor. Tente novamente em instantes.',
}

/** Extrai a mensagem mais útil dos formatos de erro comuns do Spring. */
function extractMessage(payload, status) {
  if (payload && typeof payload === 'object') {
    // ProblemDetail (RFC 7807) → detail; erro padrão do Spring → message
    const candidate = payload.detail || payload.message || payload.error || payload.title
    if (candidate && typeof candidate === 'string') return candidate
  }
  if (typeof payload === 'string' && payload.trim() && payload.length < 300) return payload
  return STATUS_MESSAGES[status] ?? `Erro inesperado (HTTP ${status}).`
}

async function parseBody(response) {
  if (response.status === 204) return null
  const text = await response.text()
  if (!text) return null
  const contentType = response.headers.get('content-type') ?? ''
  if (contentType.includes('json')) {
    try {
      return JSON.parse(text)
    } catch {
      return text
    }
  }
  return text
}

export async function request(path, { method = 'GET', body, auth = true, signal } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  if (auth) {
    const token = getToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }

  let response
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new ApiError('Não foi possível conectar ao servidor. Verifique se o back-end está em execução.', 0)
  }

  const payload = await parseBody(response)

  if (!response.ok) {
    // 401 em rota autenticada = token inválido/expirado → encerra a sessão.
    // Na tela de login o 401 significa apenas "senha errada", então não desloga.
    if (response.status === 401 && auth) handleUnauthorized()
    throw new ApiError(extractMessage(payload, response.status), response.status, payload)
  }

  return payload
}

export const http = {
  get: (path, options) => request(path, { ...options, method: 'GET' }),
  post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
  put: (path, body, options) => request(path, { ...options, method: 'PUT', body }),
  patch: (path, body, options) => request(path, { ...options, method: 'PATCH', body }),
  delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
}
