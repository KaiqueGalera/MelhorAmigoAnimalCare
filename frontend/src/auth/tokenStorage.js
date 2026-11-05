/**
 * Persistência do JWT.
 *
 * Escolha: sessionStorage. O token sobrevive a um F5, mas some ao fechar
 * a aba — adequado para um computador compartilhado de recepção/consultório.
 * localStorage manteria o login por dias, ampliando a janela de uso indevido.
 *
 * Observação: o ideal em produção é o back-end emitir o token em cookie
 * HttpOnly + SameSite, inacessível ao JavaScript (protege contra XSS).
 * Como a API atual devolve o token no corpo da resposta, armazenamos no cliente.
 */

const KEY = 'vetclinica.token'

/** Lê o payload do JWT sem validar assinatura (isso é papel do servidor). */
export function decodeJwt(token) {
  try {
    const payload = token.split('.')[1]
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decodeURIComponent(escape(json)))
  } catch {
    return null
  }
}

export function isExpired(token) {
  const exp = decodeJwt(token)?.exp
  return typeof exp === 'number' && exp * 1000 <= Date.now()
}

export const tokenStorage = {
  read() {
    try {
      const token = sessionStorage.getItem(KEY)
      if (!token || isExpired(token)) {
        sessionStorage.removeItem(KEY)
        return null
      }
      return token
    } catch {
      return null
    }
  },
  write(token) {
    try {
      sessionStorage.setItem(KEY, token)
    } catch {
      /* storage indisponível (modo privado): sessão fica só em memória */
    }
  },
  clear() {
    try {
      sessionStorage.removeItem(KEY)
    } catch {
      /* ignore */
    }
  },
}
