package br.ifsp.demo.model.prescricao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
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
}