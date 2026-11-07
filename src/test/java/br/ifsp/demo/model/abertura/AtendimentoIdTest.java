package br.ifsp.demo.model.abertura;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AtendimentoIdTest {

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("getValue de AtendimentoId deve retornar o UUID encapsulado")
    void getValueDeAtendimentoIdDeveRetornarUuidEncapsulado() {
        AtendimentoId atendId = new AtendimentoId(RANDOM_UUID);

        assertThat(atendId.getValue()).isEqualTo(RANDOM_UUID);

    }
}
