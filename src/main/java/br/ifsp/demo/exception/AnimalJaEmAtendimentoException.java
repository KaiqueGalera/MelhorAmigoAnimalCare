package br.ifsp.demo.exception;

import br.ifsp.demo.model.abertura.AnimalId;

public class AnimalJaEmAtendimentoException extends RuntimeException {
    public AnimalJaEmAtendimentoException(AnimalId animalId) {

        super("Animal - "+ animalId + " já possui atendimento em andamento!");
    }
}
