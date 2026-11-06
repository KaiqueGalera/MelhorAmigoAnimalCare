package br.ifsp.demo.model.abertura;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

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

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Deve respeitar o contrato de igualdade entre AtendimentoId")
    void deveRespeitarContratoDeIgualdadeEntreAtendimentoId() {
        AtendimentoId atendId = AtendimentoId.of(RANDOM_UUID);
        AtendimentoId mesmoValor = AtendimentoId.of(RANDOM_UUID);

        assertThat(atendId.equals(mesmoValor)).isTrue();
        assertThat(mesmoValor.equals(atendId)).isTrue();
        assertThat(atendId.hashCode()).isEqualTo(mesmoValor.hashCode());
        assertThat(atendId.equals(AtendimentoId.novo())).isFalse();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("objetosQueNaoSaoAtendimentoId")
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Deve ser diferente de objeto que nao eh um AtendimentoId")
    void deveSerDiferenteDeObjetoQueNaoEhUmAtendimentoId(String descricao, Object outro) {
        assertThat(AtendimentoId.of(RANDOM_UUID).equals(outro)).isFalse();
    }

    static Stream<Arguments> objetosQueNaoSaoAtendimentoId() {
        return Stream.of(
                Arguments.of("nulo", null),
                Arguments.of("String com o mesmo UUID", RANDOM_UUID.toString()),
                Arguments.of("AnimalId com o mesmo UUID", AnimalId.of(RANDOM_UUID)));
    }
}
