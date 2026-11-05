import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

export function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app">
      <header className="topbar">
        <Link to="/" className="brand">
          <span className="brand__mark" aria-hidden="true">✚</span>
          VetClínica
        </Link>
        <nav className="topbar__nav" aria-label="Principal">
          <NavLink to="/" end>
            Atendimentos
          </NavLink>
        </nav>
        <div className="topbar__user">
          {user?.email && <span className="topbar__email">{user.email}</span>}
          <button type="button" className="btn btn--ghost btn--sm" onClick={handleLogout}>
            Sair
          </button>
        </div>
      </header>
      <main className="container">
        <Outlet />
      </main>
    </div>
  )
}

export function AuthLayout({ title, subtitle, children }) {
  return (
    <div className="auth">
      <div className="auth__panel">
        <div className="brand brand--lg">
          <span className="brand__mark" aria-hidden="true">✚</span>
          VetClínica
        </div>
        <h1>{title}</h1>
        {subtitle && <p className="muted">{subtitle}</p>}
        {children}
      </div>
    </div>
  )
}
