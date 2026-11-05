import { useCallback, useState } from 'react'

/**
 * Padroniza "loading + erro" de qualquer chamada assíncrona.
 * Evita repetir try/catch/finally e três useState em cada formulário.
 *
 *   const { run, loading, error } = useAsyncAction(api.salvar)
 *   const { ok, data } = await run(dados)
 *
 * Retornar { ok } explicitamente é mais seguro do que ler `error` logo
 * após o await: o estado do React só é atualizado no próximo render.
 */
export function useAsyncAction(action) {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const run = useCallback(
    async (...args) => {
      setLoading(true)
      setError(null)
      try {
        const data = await action(...args)
        return { ok: true, data }
      } catch (err) {
        setError(err?.message ?? 'Erro inesperado.')
        return { ok: false, data: undefined }
      } finally {
        setLoading(false)
      }
    },
    [action],
  )

  const clearError = useCallback(() => setError(null), [])

  return { run, loading, error, clearError }
}
