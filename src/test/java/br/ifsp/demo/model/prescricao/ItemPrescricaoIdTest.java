package br.ifsp.demo.model.prescricao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemPrescricaoIdTest {

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("ItemPrescricaoId - deve rejeitar criacao com UUID nulo")
    void itemPrescricaoIdDeveRejeitarUuidNulo() {
        assertThatThrownBy(() -> ItemPrescricaoId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Id não pode ser nulo.");
    }
}