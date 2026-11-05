import { useCallback, useState } from 'react'

/**
 * O back-end não expõe listagem de atendimentos (só busca por ID).
 * Para não obrigar o usuário a copiar UUIDs, guardamos localmente
 * os últimos atendimentos abertos/consultados neste navegador.
 * Guardamos apenas ID, animal e status — nada de dado clínico (LGPD:
 * minimização de dados no dispositivo).
 */

const KEY = 'vetclinica.recentes'
const MAX = 10

function readList() {
  try {
    const parsed = JSON.parse(localStorage.getItem(KEY) ?? '[]')
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function writeList(list) {
  try {
    localStorage.setItem(KEY, JSON.stringify(list))
  } catch {
    /* ignore */
  }
}

export function rememberAtendimento({ id, animalId, status }) {
  if (!id) return
  const list = readList().filter((item) => item.id !== id)
  list.unshift({ id, animalId, status, vistoEm: new Date().toISOString() })
  writeList(list.slice(0, MAX))
}

export function useRecentAtendimentos() {
  const [items, setItems] = useState(readList)

  const clear = useCallback(() => {
    writeList([])
    setItems([])
  }, [])

  return { items, clear }
}
