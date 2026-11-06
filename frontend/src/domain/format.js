const dateTimeFmt = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

/** LocalDateTime do Java chega sem fuso ("2026-11-05T10:30:00") e é lido como horário local. */
export function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? String(value) : dateTimeFmt.format(date)
}

/** O DateTimePicker (mesmo contrato do datetime-local) devolve "YYYY-MM-DDTHH:mm"; LocalDateTime aceita com segundos. */
export function toLocalDateTimePayload(value) {
  return value && value.length === 16 ? `${value}:00` : value
}

export function shortId(id) {
  return id ? `${String(id).slice(0, 8)}…` : '—'
}
