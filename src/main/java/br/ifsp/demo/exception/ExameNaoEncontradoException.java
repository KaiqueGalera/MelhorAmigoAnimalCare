package br.ifsp.demo.exception;

public class ExameNaoEncontradoException extends RuntimeException {
    public ExameNaoEncontradoException(String message) {
        super(message);
    }
}
