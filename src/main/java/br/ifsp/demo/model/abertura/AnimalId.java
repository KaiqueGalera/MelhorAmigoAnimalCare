package br.ifsp.demo.model.abertura;

import java.util.Objects;
import java.util.UUID;

public class AnimalId {
    private final UUID id;

    public AnimalId(UUID id) {
        this.id = id;
    }

    public static AnimalId of(UUID uuid){
        return new AnimalId(uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AnimalId animalId = (AnimalId) o;
        return Objects.equals(id, animalId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
