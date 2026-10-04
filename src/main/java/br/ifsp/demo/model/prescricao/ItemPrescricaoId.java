package br.ifsp.demo.model.prescricao;

import java.util.Objects;
import java.util.UUID;

public class ItemPrescricaoId {
    private final UUID value;

    private ItemPrescricaoId(UUID value) {
        this.value = value;
    }

    public static ItemPrescricaoId novo() {
        return new ItemPrescricaoId(UUID.randomUUID());
    }

    public static ItemPrescricaoId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Id não pode ser nulo.");
        }
        return new ItemPrescricaoId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPrescricaoId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}