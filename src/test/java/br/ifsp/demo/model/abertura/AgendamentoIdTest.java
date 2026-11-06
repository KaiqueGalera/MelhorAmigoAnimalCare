package br.ifsp.demo.model.abertura;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AgendamentoIdTest {

    private static final UUID RAMDOM_UUID = UUID.randomUUID();

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("getId e getValue devem retornar o UUID encapsulado")
    void getIdEGetValueDevemRetornarUuidEncapsulado() {
        AgendamentoId agen = new AgendamentoId(RAMDOM_UUID);

        assertThat(agen.getId()).isEqualTo(RAMDOM_UUID);
        assertThat(agen.getValue()).isEqualTo(RAMDOM_UUID);

    }
}
