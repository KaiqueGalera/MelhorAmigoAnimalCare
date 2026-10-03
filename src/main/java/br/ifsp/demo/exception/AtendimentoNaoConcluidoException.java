package br.ifsp.demo.exception;

public class AtendimentoNaoConcluidoException extends RuntimeException {
    public AtendimentoNaoConcluidoException(String message) {
        super(message);
    }
}
