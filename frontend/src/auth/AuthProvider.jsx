import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { authApi } from '../api/authApi'
import { configureHttpClient } from '../api/httpClient'
import { AuthContext } from './context'
import { decodeJwt, tokenStorage } from './tokenStorage'

/**
 * Estado global de autenticação.
 * Context API basta aqui: é um único valor pouco alterado (login/logout),
 * então não justifica Redux/Zustand como dependência extra.
 */
export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => tokenStorage.read())
  const [sessionExpired, setSessionExpired] = useState(false)

  // Ref para o cliente HTTP sempre ler o token atual sem re-registrar a função.
  const tokenRef = useRef(token)
  useEffect(() => {
    tokenRef.current = token
  }, [token])

  const logout = useCallback(() => {
    tokenStorage.clear()
    tokenRef.current = null
    setToken(null)
  }, [])

  useEffect(() => {
    configureHttpClient({
      tokenProvider: () => tokenRef.current,
      onUnauthorized: () => {
        setSessionExpired(true)
        logout()
      },
    })
  }, [logout])

  // Logout automático quando o "exp" do JWT é atingido.
  useEffect(() => {
    const exp = token ? decodeJwt(token)?.exp : null
    if (!exp) return undefined
    const msRestantes = exp * 1000 - Date.now()
    const timer = setTimeout(() => {
      setSessionExpired(true)
      logout()
    }, Math.max(msRestantes, 0))
    return () => clearTimeout(timer)
  }, [token, logout])

  const login = useCallback(async (credentials) => {
    const newToken = await authApi.authenticate(credentials)
    tokenStorage.write(newToken)
    tokenRef.current = newToken
    setSessionExpired(false)
    setToken(newToken)
  }, [])

  const value = useMemo(() => {
    const claims = token ? decodeJwt(token) : null
    return {
      isAuthenticated: Boolean(token),
      user: claims ? { email: claims.sub ?? claims.email ?? '' } : null,
      sessionExpired,
      login,
      logout,
      register: authApi.register,
    }
  }, [token, sessionExpired, login, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
