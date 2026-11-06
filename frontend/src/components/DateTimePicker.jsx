import { useEffect, useId, useMemo, useRef, useState } from 'react'

/**
 * Seletor de data e hora em pt-BR.
 *
 * Por que não <input type="datetime-local">? O calendário nativo segue o idioma
 * da INTERFACE do navegador (não o lang da página), então aparece em inglês,
 * com formato MM/DD e AM/PM em navegadores configurados em inglês.
 *
 * O valor segue o mesmo contrato do datetime-local ("YYYY-MM-DDTHH:mm"), para
 * que validadores e o envio à API não precisem mudar.
 */

const DIAS_SEMANA = ['dom', 'seg', 'ter', 'qua', 'qui', 'sex', 'sáb']
const fmtMesAno = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' })
const fmtDiaCompleto = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'full' })

const pad = (n) => String(n).padStart(2, '0')

/** "YYYY-MM-DD" com os componentes LOCAIS (toISOString usaria UTC e poderia mudar o dia). */
function toIsoDate(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function fromIsoDate(s) {
  const [y, m, d] = s.split('-').map(Number)
  return new Date(y, m - 1, d)
}

function toMinutos(hhmm) {
  const [h, m] = hhmm.split(':').map(Number)
  return h * 60 + m
}

/** Horários de `inicio` a `fim` (inclusive), a cada `passo` minutos. */
function gerarHorarios(inicio, fim, passo) {
  const lista = []
  for (let t = toMinutos(inicio); t <= toMinutos(fim); t += passo) {
    lista.push(`${pad(Math.floor(t / 60))}:${pad(t % 60)}`)
  }
  return lista
}

function Calendario({ selecionada, minima, onSelect, onClose }) {
  const hojeIso = toIsoDate(new Date())
  const minimaIso = minima ? toIsoDate(minima) : null
  const inicial = selecionada ? fromIsoDate(selecionada) : minima ?? new Date()
  const [mes, setMes] = useState(new Date(inicial.getFullYear(), inicial.getMonth(), 1))

  const dias = useMemo(() => {
    const vaziosAntes = mes.getDay() // 0 = domingo, igual à primeira coluna
    const total = new Date(mes.getFullYear(), mes.getMonth() + 1, 0).getDate()
    const celulas = Array.from({ length: vaziosAntes }, () => null)
    for (let d = 1; d <= total; d++) celulas.push(new Date(mes.getFullYear(), mes.getMonth(), d))
    return celulas
  }, [mes])

  const mudarMes = (delta) => setMes((m) => new Date(m.getFullYear(), m.getMonth() + delta, 1))
  // Não deixa navegar para meses inteiramente no passado.
  const podeVoltar = !minima || mes > new Date(minima.getFullYear(), minima.getMonth(), 1)

  return (
    <div className="dtp__popover" role="dialog" aria-label="Escolher data">
      <div className="dtp__head">
        <button type="button" className="dtp__nav" onClick={() => mudarMes(-1)} disabled={!podeVoltar} aria-label="Mês anterior">
          ‹
        </button>
        <span className="dtp__title">{fmtMesAno.format(mes)}</span>
        <button type="button" className="dtp__nav" onClick={() => mudarMes(1)} aria-label="Próximo mês">
          ›
        </button>
      </div>
      <div className="dtp__grid">
        {DIAS_SEMANA.map((d) => (
          <span key={d} className="dtp__weekday">
            {d}
          </span>
        ))}
        {dias.map((dia, i) => {
          if (!dia) return <span key={`v${i}`} />
          const iso = toIsoDate(dia)
          const desabilitado = minimaIso != null && iso < minimaIso
          const classes = ['dtp__day', iso === selecionada && 'is-selected', iso === hojeIso && 'is-today']
          return (
            <button
              key={iso}
              type="button"
              className={classes.filter(Boolean).join(' ')}
              disabled={desabilitado}
              aria-pressed={iso === selecionada}
              aria-label={fmtDiaCompleto.format(dia)}
              onClick={() => {
                onSelect(iso)
                onClose()
              }}
            >
              {dia.getDate()}
            </button>
          )
        })}
      </div>
    </div>
  )
}

export function DateTimePicker({
  value,
  onChange,
  minDate,
  horaInicio = '00:00',
  horaFim = '23:59',
  passoMinutos = 15,
  id,
  ...ariaProps
}) {
  const [aberto, setAberto] = useState(false)
  const raiz = useRef(null)
  const horaId = useId()

  const data = value ? value.slice(0, 10) : ''
  const hora = value ? value.slice(11, 16) : ''

  // Mantém o formato "YYYY-MM-DDTHH:mm"; parcial enquanto falta data ou hora,
  // para o validador apontar o que falta.
  const emitir = (d, h) => onChange(d || h ? `${d}T${h}` : '')

  // Fecha ao clicar fora ou apertar Esc.
  useEffect(() => {
    if (!aberto) return
    const fora = (e) => raiz.current && !raiz.current.contains(e.target) && setAberto(false)
    const esc = (e) => e.key === 'Escape' && setAberto(false)
    document.addEventListener('mousedown', fora)
    document.addEventListener('keydown', esc)
    return () => {
      document.removeEventListener('mousedown', fora)
      document.removeEventListener('keydown', esc)
    }
  }, [aberto])

  const horarios = useMemo(() => {
    const todos = gerarHorarios(horaInicio, horaFim, passoMinutos)
    // No dia de hoje, esconde horários que já passaram.
    const agora = new Date()
    if (data !== toIsoDate(agora)) return todos
    const minutosAgora = agora.getHours() * 60 + agora.getMinutes()
    return todos.filter((h) => toMinutos(h) > minutosAgora)
  }, [data, horaInicio, horaFim, passoMinutos])

  const rotuloData = data ? fromIsoDate(data).toLocaleDateString('pt-BR') : 'dd/mm/aaaa'

  return (
    <div className="dtp" ref={raiz}>
      <button
        id={id}
        type="button"
        className={`dtp__trigger ${data ? '' : 'is-empty'}`}
        aria-haspopup="dialog"
        aria-expanded={aberto}
        onClick={() => setAberto((a) => !a)}
        {...ariaProps}
      >
        <span>{rotuloData}</span>
        <span aria-hidden="true">📅</span>
      </button>
      <select
        id={horaId}
        className="dtp__time"
        aria-label="Horário"
        value={horarios.includes(hora) ? hora : ''}
        onChange={(e) => emitir(data, e.target.value)}
        {...ariaProps}
      >
        <option value="">--:--</option>
        {horarios.map((h) => (
          <option key={h} value={h}>
            {h}
          </option>
        ))}
      </select>
      {aberto && (
        <Calendario
          selecionada={data}
          minima={minDate}
          onSelect={(d) => emitir(d, hora)}
          onClose={() => setAberto(false)}
        />
      )}
    </div>
  )
}
