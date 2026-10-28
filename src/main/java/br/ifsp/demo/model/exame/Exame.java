package br.ifsp.demo.model.exame;

import java.util.Objects;

public class Exame {
    private final ExameId id;
    private final String nome;

    public Exame(ExameId id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public ExameId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Exame exame = (Exame) o;
        return Objects.equals(id, exame.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
