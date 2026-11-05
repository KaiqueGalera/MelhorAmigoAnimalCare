import { lazy, Suspense } from 'react'
import { BrowserRouter, Link, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthProvider'
import { GuestRoute, ProtectedRoute } from './auth/ProtectedRoute'
import { AppLayout } from './components/Layout'
import { Loading } from './components/ui'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'

// Code splitting: as telas autenticadas só são baixadas depois do login,
// deixando a tela de login mais leve no primeiro acesso.
const DashboardPage = lazy(() => import('./pages/DashboardPage'))
const AtendimentoPage = lazy(() => import('./pages/AtendimentoPage'))

function NotFound() {
  return (
    <div className="stack center">
      <h1>Página não encontrada</h1>
      <Link to="/">Voltar ao início</Link>
    </div>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Suspense fallback={<Loading />}>
          <Routes>
            <Route element={<GuestRoute />}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/registrar" element={<RegisterPage />} />
            </Route>

            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route index element={<DashboardPage />} />
                <Route path="atendimentos/:id" element={<AtendimentoPage />} />
                <Route path="*" element={<NotFound />} />
              </Route>
            </Route>
          </Routes>
        </Suspense>
      </BrowserRouter>
    </AuthProvider>
  )
}
