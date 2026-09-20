package br.ifsp.demo.model.abertura;

import java.util.Objects;
import java.util.UUID;

public class AtendimentoId {
    private final UUID id;

    public AtendimentoId(UUID id) {
        this.id = id;
    }

    public static AtendimentoId novo(){
        return new AtendimentoId(UUID.randomUUID());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AtendimentoId that = (AtendimentoId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
