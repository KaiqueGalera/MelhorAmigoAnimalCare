package br.ifsp.demo.exception;

import br.ifsp.demo.model.abertura.AgendamentoId;

public class AgendamentoJaComAtendimento extends RuntimeException {
    public AgendamentoJaComAtendimento(AgendamentoId agendamentoId) {
        super("Agendamento " + agendamentoId + " já foi utilizado em outro atendimento");
    }
}
