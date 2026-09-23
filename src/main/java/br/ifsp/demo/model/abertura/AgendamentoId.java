package br.ifsp.demo.model.abertura;

import java.util.Objects;
import java.util.UUID;

public class AgendamentoId {
    private final UUID id;

    public AgendamentoId(UUID id) {
        this.id = id;
    }

    public static AgendamentoId of(UUID uuid){
        return new AgendamentoId(uuid);
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AgendamentoId that = (AgendamentoId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
