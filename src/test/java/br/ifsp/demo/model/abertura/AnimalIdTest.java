package br.ifsp.demo.model.abertura;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AnimalIdTest {

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("getValue de Animal deve retornar o UUID encapsulado")
    void getValueDeAnimalDeveRetornarUuidEncapsulado() {
        AnimalId animalId = new AnimalId(RANDOM_UUID);

        assertThat(animalId.getValue()).isEqualTo(RANDOM_UUID);

    }
}
