package br.ifsp.demo.model.clinico;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AnamneseTest {

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Anamnese deve rejeitar queixa principal nula")
    void anamneseDeveRejeitarQueixaPrincipalNula() {
        assertThatThrownBy(() -> new Anamnese(null, "Vacinas em dia"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A queixa principal da anamnese é obrigatória.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Anamnese deve rejeitar queixa principal vazia")
    void anamneseDeveRejeitarQueixaPrincipalVazia() {
        assertThatThrownBy(() -> new Anamnese("", "Vacinas em dia"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A queixa principal da anamnese não pode ser vazia.");
    }

}
