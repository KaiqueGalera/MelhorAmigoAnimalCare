package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.JustificativaObrigatoriaException;
import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.StatusAtendimento;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.model.clinico.TipoDiagnostico;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AtendimentoServiceFuncionalTest {
    private InMemoryAtendimentoRepository repository;
    private AtendimentoService service;

    @BeforeEach()
    void prepara(){
        repository = new InMemoryAtendimentoRepository();
        service = new AtendimentoService(repository);
    }

    // =================================================================
    // PARTIÇÃO DE EQUIVALÊNCIA — justificativa de cancelamento
    // Classes: inválida (nula / vazia / só espaços em branco) x válidas
    // =================================================================

    @ParameterizedTest(name = "justificativa inválida: \"{0}\"")
    @DisplayName("#PE01 Não devo cancelar atendimento com justificativa inválida (nula, vazia ou em branco)")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void naoDevoCancelarComJustificativaInvalida(String justificativaInvalida) {
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);

        assertThrows(JustificativaObrigatoriaException.class,
                () -> service.cancelarAtendimento(atendimento.getId(), justificativaInvalida));
    }

    @ParameterizedTest(name = "justificativa válida: \"{0}\"")
    @Tag("UnitTest")
    @Tag("Funcional")
    @DisplayName("#PE02 Devo cancelar atendimento com justificativa válida")
    @ValueSource(strings = {
            "a",
            "Animal estava muito inquieto e o dono optou por ir embora",
            "Tutor não compareceu"
    })
    void devoCancelarComJustificativaValida(String justificativaValida) {
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);

        Atendimento cancelado = service.cancelarAtendimento(atendimento.getId(), justificativaValida);

        assertThat(cancelado.getStatus()).isEqualTo(StatusAtendimento.CANCELADO);
        assertThat(cancelado.getJustificativa()).isEqualTo(justificativaValida);
    }

    // =================================================================
    // TABELA DE DECISÃO — conclusão do atendimento
    // sinaisVitais presente? x diagnostico presente?
    // =================================================================

    @ParameterizedTest(name = "sinaisVitais={0}, diagnostico={1} -> deveConcluir={2}")
    @Tag("Funcional")
    @Tag("TabelaDecisao")
    @DisplayName("#TD01 Tabela de decisão para conclusão do atendimento")
    @CsvSource({
            "false, false, false",
            "false, true,  false",
            "true,  false, false",
            "true,  true,  true"
    })
    void tabelaDecisaoConcluir(boolean comSinaisVitais, boolean comDiagnostico, boolean deveConcluir) {
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);

        if (comSinaisVitais) {
            atendimento.registrarSinaisVitais(new SinaisVitais(38.5, 100, 77));
        }
        if (comDiagnostico) {
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
        }

        if (deveConcluir) {
            assertDoesNotThrow(() -> service.concluirAtendimento(atendimento.getId()));
        } else {
            assertThrows(RuntimeException.class, () -> service.concluirAtendimento(atendimento.getId()));
        }
    }
}