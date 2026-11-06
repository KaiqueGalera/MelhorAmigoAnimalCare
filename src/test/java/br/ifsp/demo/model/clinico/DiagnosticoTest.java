package br.ifsp.demo.model.clinico;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DiagnosticoTest {

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Diagnóstico deve rejeitar código nulo")
    void diagnosticoDeveRejeitarCodigoNulo() {
        assertThatThrownBy(() -> new Diagnostico(null, "Fungo na orelha", TipoDiagnostico.PRESUNTIVO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O código do diagnóstico é obrigatório.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Diagnóstico deve rejeitar código vazio")
    void diagnosticoDeveRejeitarCodigoNVazio() {
        assertThatThrownBy(() -> new Diagnostico("", "Fungo na orelha", TipoDiagnostico.PRESUNTIVO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O código do diagnóstico é obrigatório.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Diagnóstico deve rejeitar descrição nula")
    void diagnosticoDeveRejeitarDescricaoNula() {
        assertThatThrownBy(() -> new Diagnostico("A1", null, TipoDiagnostico.PRESUNTIVO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A descrição do diagnóstico é obrigatória.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("Diagnóstico deve rejeitar descrição vazia")
    void diagnosticoDeveRejeitarDescricaoVazia() {
        assertThatThrownBy(() -> new Diagnostico("A1", "", TipoDiagnostico.PRESUNTIVO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A descrição do diagnóstico é obrigatória.");
    }
}
