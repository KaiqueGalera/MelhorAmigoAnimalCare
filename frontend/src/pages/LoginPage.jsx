import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { AuthLayout } from '../components/Layout'
import { Alert, Button, Field } from '../components/ui'
import { useAsyncAction } from '../hooks/useAsyncAction'
import { useForm } from '../hooks/useForm'
import { validarLogin } from '../domain/validators'

const INITIAL = { email: '', password: '' }

export default function LoginPage() {
  const { login, sessionExpired } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const { values, errors, handleChange, validateAll } = useForm(INITIAL, validarLogin)
  const { run, loading, error } = useAsyncAction(login)

  const registeredEmail = location.state?.registeredEmail
  const redirectTo = location.state?.from?.pathname ?? '/'

  async function handleSubmit(event) {
    event.preventDefault()
    if (!validateAll()) return
    const { ok } = await run(values)
    if (ok) navigate(redirectTo, { replace: true })
  }

  return (
    <AuthLayout title="Entrar" subtitle="Acesse o painel de atendimentos da clínica.">
      {sessionExpired && <Alert kind="warning">Sua sessão expirou. Entre novamente.</Alert>}
      {registeredEmail && <Alert kind="success">Conta criada! Entre com {registeredEmail}.</Alert>}
      <Alert>{error}</Alert>

      <form className="form" onSubmit={handleSubmit} noValidate>
        <Field label="E-mail" error={errors.email}>
          {(a11y) => (
            <input
              {...a11y}
              name="email"
              type="email"
              autoComplete="username"
              value={values.email}
              onChange={handleChange}
              autoFocus
            />
          )}
        </Field>
        <Field label="Senha" error={errors.password}>
          {(a11y) => (
            <input
              {...a11y}
              name="password"
              type="password"
              autoComplete="current-password"
              value={values.password}
              onChange={handleChange}
            />
          )}
        </Field>
        <Button type="submit" loading={loading} className="btn btn--primary btn--block">
          Entrar
        </Button>
      </form>

      <p className="auth__switch">
        Ainda não tem conta? <Link to="/registrar">Cadastre-se</Link>
      </p>
    </AuthLayout>
  )
}
