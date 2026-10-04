package br.ifsp.demo.exception;

import br.ifsp.demo.model.abertura.AtendimentoId;

public class InstanteReaberturaInvalidoException extends RuntimeException {
    public InstanteReaberturaInvalidoException(AtendimentoId id) {
        super("Instante de reabertura anterior à conclusão do atendimento: " + id);
    }
}
