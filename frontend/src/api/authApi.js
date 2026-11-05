import { http } from './httpClient'
import { endpoints } from './endpoints'

/**
 * Formato padrão do template Spring do IFSP:
 *   RegisterUserRequest { name, lastname, email, password }
 *   AuthRequest         { username, password }  (username = e-mail)
 *   AuthResponse        { token }
 * Se o seu DTO usar outros nomes, ajuste apenas aqui.
 */
export const authApi = {
  register: ({ name, lastname, email, password }) =>
    http.post(endpoints.auth.register, { name, lastname, email, password }, { auth: false }),

  authenticate: async ({ email, password }) => {
    const response = await http.post(
      endpoints.auth.authenticate,
      { username: email, password },
      { auth: false },
    )
    const token = response?.token ?? response?.accessToken ?? response?.jwt
    if (!token) throw new Error('O servidor não retornou um token de acesso.')
    return token
  },
}
