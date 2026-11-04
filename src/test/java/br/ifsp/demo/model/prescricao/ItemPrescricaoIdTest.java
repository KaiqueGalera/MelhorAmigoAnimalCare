package br.ifsp.demo.model.prescricao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemPrescricaoIdTest {

    private static final UUID RANDOM_UUID = UUID.randomUUID();

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
        ItemPrescricaoId id = ItemPrescricaoId.of(RANDOM_UUID);
        ItemPrescricaoId mesmoValor = ItemPrescricaoId.of(RANDOM_UUID);

        assertThat(id.getValue()).isEqualTo(RANDOM_UUID);
        assertThat(id.equals(id)).isTrue();
        assertThat(id.equals(mesmoValor)).isTrue();
        assertThat(mesmoValor.equals(id)).isTrue();
        assertThat(id.hashCode()).isEqualTo(mesmoValor.hashCode());
        assertThat(id.equals(ItemPrescricaoId.novo())).isFalse();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("objetosQueNaoSaoItemPrescricaoId")
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("ItemPrescricaoId - deve ser diferente de objeto de outro tipo")
    void itemPrescricaoIdDeveSerDiferenteDeObjetoDeOutroTipo(String descricao, Object outro) {
        assertThat(ItemPrescricaoId.of(RANDOM_UUID).equals(outro)).isFalse();
    }

    static Stream<Arguments> objetosQueNaoSaoItemPrescricaoId() {
        return Stream.of(
                Arguments.of("nulo", null),
                Arguments.of("String com o mesmo UUID", RANDOM_UUID.toString()),
                Arguments.of("PrescricaoId com o mesmo UUID", PrescricaoId.of(RANDOM_UUID)));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    @DisplayName("hashcode de ItemPrescricaoId deve ser o hash do uuid encapsulado")
    void hashCodeDeItemPrescricaoIdDeveSerOHashDoUuidEncapsulado() {
        UUID uuid = new UUID(1L, 2L);

        assertThat(ItemPrescricaoId.of(uuid).hashCode()).isEqualTo(uuid.hashCode());
    }
}