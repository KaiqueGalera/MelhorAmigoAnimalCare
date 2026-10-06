package br.ifsp.demo.model.prescricao;

import java.util.Objects;
import java.util.UUID;

public class PrescricaoId {
    private final UUID value;

    private PrescricaoId(UUID value) {
        this.value = value;
    }

    public static PrescricaoId novo() {
        return new PrescricaoId(UUID.randomUUID());
    }

    public static PrescricaoId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Id não pode ser nulo.");
        }
        return new PrescricaoId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrescricaoId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}