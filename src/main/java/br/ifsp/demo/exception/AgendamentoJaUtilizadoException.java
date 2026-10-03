package br.ifsp.demo.exception;

import br.ifsp.demo.model.abertura.AgendamentoId;

public class AgendamentoJaUtilizadoException extends RuntimeException {
    public AgendamentoJaUtilizadoException(AgendamentoId agendamentoId) {
        super("Agendamento " + agendamentoId + " já foi utilizado em outro atendimento");
    }
}
