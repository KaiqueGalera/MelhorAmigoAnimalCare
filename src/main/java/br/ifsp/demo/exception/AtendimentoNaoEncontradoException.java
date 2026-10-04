package br.ifsp.demo.exception;

import br.ifsp.demo.model.abertura.AtendimentoId;

public class AtendimentoNaoEncontradoException extends RuntimeException {
    public AtendimentoNaoEncontradoException(AtendimentoId id)
    {
        super("Atendimento não encontrado: " + id);
    }
}
