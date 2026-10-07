package br.ifsp.demo.model.exame;

public class Exame {
    private final ExameId id;
    private final String nome;

    public Exame(ExameId id, String nome) {
        this.id = id;
        this.nome = nome;
    }
}
