import { useCallback, useState } from 'react'

/**
 * Estado de formulário controlado + validação sob demanda.
 * Os erros de um campo somem assim que o usuário o edita, para não
 * "gritar" com quem já está corrigindo.
 */
export function useForm(initialValues, validate) {
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState({})

  const handleChange = useCallback((event) => {
    const { name, value } = event.target
    setValues((prev) => ({ ...prev, [name]: value }))
    setErrors((prev) => {
      if (!prev[name]) return prev
      const { [name]: _removed, ...rest } = prev
      return rest
    })
  }, [])

  /** Executa a validação; devolve true se não há erros. */
  const validateAll = useCallback(() => {
    const result = validate ? validate(values) : {}
    setErrors(result)
    return Object.keys(result).length === 0
  }, [validate, values])

  const reset = useCallback((next = initialValues) => {
    setValues(next)
    setErrors({})
  }, [initialValues])

  return { values, errors, handleChange, validateAll, reset, setValues }
}
