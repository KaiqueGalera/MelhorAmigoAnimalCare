package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.JustificativaObrigatoriaException;
import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

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
    // Classes: inválida (nula / vazia / só espaços em branco)
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
}