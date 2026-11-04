package br.ifsp.demo.model.prescricao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrescricaoIdTest {

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Deve rejeitar PrescricaoId criado com UUID nulo")
    void deveRejeitarPrescricaoIdComUuidNulo() {
        assertThatThrownBy(() -> PrescricaoId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Id não pode ser nulo.");
    }
}