package br.ifsp.demo.model.prescricao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("ItemPrescricaoId - deve respeitar o contrato de igualdade")
    void itemPrescricaoIdDeveRespeitarContratoDeIgualdade() {
        UUID uuid = UUID.randomUUID();
        ItemPrescricaoId id = ItemPrescricaoId.of(uuid);
        ItemPrescricaoId mesmoValor = ItemPrescricaoId.of(uuid);

        assertThat(id.getValue()).isEqualTo(uuid);
        assertThat(id.equals(id)).isTrue();
        assertThat(id.equals(mesmoValor)).isTrue();
        assertThat(mesmoValor.equals(id)).isTrue();
        assertThat(id.hashCode()).isEqualTo(mesmoValor.hashCode());
        assertThat(id.equals(ItemPrescricaoId.novo())).isFalse();
    }
}