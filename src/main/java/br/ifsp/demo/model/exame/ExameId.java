package br.ifsp.demo.model.exame;

import java.util.Objects;
import java.util.UUID;

public class ExameId {
    private final UUID value;

    private ExameId(UUID value) {
        this.value = value;
    }

    public static ExameId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Id não pode ser nulo.");
        }
        return new ExameId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExameId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
