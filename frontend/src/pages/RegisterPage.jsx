import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { AuthLayout } from '../components/Layout'
import { Alert, Button, Field } from '../components/ui'
import { useAsyncAction } from '../hooks/useAsyncAction'
import { useForm } from '../hooks/useForm'
import { validarCadastro } from '../domain/validators'

const INITIAL = { name: '', lastname: '', email: '', password: '', confirmPassword: '' }

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const { values, errors, handleChange, validateAll } = useForm(INITIAL, validarCadastro)
  const { run, loading, error } = useAsyncAction(register)

  async function handleSubmit(event) {
    event.preventDefault()
    if (!validateAll()) return
    // confirmPassword é só do front: não vai para a API.
    const { confirmPassword: _ignored, ...payload } = values
    const { ok } = await run({ ...payload, email: payload.email.trim() })
    // Não logamos automaticamente: o endpoint de cadastro devolve só o id,
    // e pedir o login confirma que o usuário sabe a senha que acabou de criar.
    if (ok) navigate('/login', { replace: true, state: { registeredEmail: payload.email.trim() } })
  }

  const input = (name, props = {}) => (a11y) => (
    <input {...a11y} name={name} value={values[name]} onChange={handleChange} {...props} />
  )

  return (
    <AuthLayout title="Criar conta" subtitle="Cadastre-se para registrar atendimentos.">
      <Alert>{error}</Alert>
      <form className="form" onSubmit={handleSubmit} noValidate>
        <div className="grid grid--2">
          <Field label="Nome" error={errors.name}>
            {input('name', { autoComplete: 'given-name', autoFocus: true })}
          </Field>
          <Field label="Sobrenome" error={errors.lastname}>
            {input('lastname', { autoComplete: 'family-name' })}
          </Field>
        </div>
        <Field label="E-mail" error={errors.email}>
          {input('email', { type: 'email', autoComplete: 'email' })}
        </Field>
        <Field label="Senha" error={errors.password} hint="Mínimo de 6 caracteres.">
          {input('password', { type: 'password', autoComplete: 'new-password' })}
        </Field>
        <Field label="Confirmar senha" error={errors.confirmPassword}>
          {input('confirmPassword', { type: 'password', autoComplete: 'new-password' })}
        </Field>
        <Button type="submit" loading={loading} className="btn btn--primary btn--block">
          Criar conta
        </Button>
      </form>
      <p className="auth__switch">
        Já possui conta? <Link to="/login">Entrar</Link>
      </p>
    </AuthLayout>
  )
}
