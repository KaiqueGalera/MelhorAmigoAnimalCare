import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './useAuth'

/**
 * Guarda de rota: sem token, redireciona ao login guardando a URL de origem
 * para voltar a ela depois de autenticar.
 * Importante: isto é só UX — quem protege os dados de fato é o back-end.
 */
export function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  return <Outlet />
}

/** Inverso: usuário logado não precisa ver login/cadastro. */
export function GuestRoute() {
  const { isAuthenticated } = useAuth()
  return isAuthenticated ? <Navigate to="/" replace /> : <Outlet />
}
