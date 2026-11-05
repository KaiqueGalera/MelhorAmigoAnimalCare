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

class PrescricaoIdTest {

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Deve rejeitar PrescricaoId criado com UUID nulo")
    void deveRejeitarPrescricaoIdComUuidNulo() {
        assertThatThrownBy(() -> PrescricaoId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Id não pode ser nulo.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Deve respeitar o contrato de igualdade entre PrescricaoId")
    void deveRespeitarContratoDeIgualdade() {
        PrescricaoId id = PrescricaoId.of(RANDOM_UUID);
        PrescricaoId mesmoValor = PrescricaoId.of(RANDOM_UUID);

        assertThat(id.getValue()).isEqualTo(RANDOM_UUID);
        assertThat(id.equals(id)).isTrue();
        assertThat(id.equals(mesmoValor)).isTrue();
        assertThat(mesmoValor.equals(id)).isTrue();
        assertThat(id.hashCode()).isEqualTo(mesmoValor.hashCode());
        assertThat(id.equals(PrescricaoId.novo())).isFalse();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("objetosQueNaoSaoPrescricaoId")
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Deve ser diferente de objeto que nao e um PrescricaoId")
    void deveSerDiferenteDeObjetoQueNaoEUmPrescricaoId(String descricao, Object outro) {
        assertThat(PrescricaoId.of(RANDOM_UUID).equals(outro)).isFalse();
    }

    static Stream<Arguments> objetosQueNaoSaoPrescricaoId() {
        return Stream.of(
                Arguments.of("nulo", null),
                Arguments.of("String com o mesmo UUID", RANDOM_UUID.toString()),
                Arguments.of("ItemPrescricaoId com o mesmo UUID", ItemPrescricaoId.of(RANDOM_UUID)));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    @DisplayName("hashcode de PrescricaoId deve ser o hash do uuid encapsulado")
    void hashCodeDePrescricaoIdDeveSerOHashDoUuidEncapsulado() {
        UUID uuid = new UUID(1L, 2L);

        assertThat(PrescricaoId.of(uuid).hashCode()).isEqualTo(uuid.hashCode());
    }
}