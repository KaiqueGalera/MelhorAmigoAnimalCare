package br.ifsp.demo.model.abertura;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AgendamentoIdTest {

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("getId e getValue de Agendamento devem retornar o UUID encapsulado")
    void getIdEGetValueDeAgendamentoDevemRetornarUuidEncapsulado() {
        AgendamentoId agen = new AgendamentoId(RANDOM_UUID);

        assertThat(agen.getId()).isEqualTo(RANDOM_UUID);
        assertThat(agen.getValue()).isEqualTo(RANDOM_UUID);

    }
}
