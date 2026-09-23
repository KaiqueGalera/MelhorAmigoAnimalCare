package br.ifsp.demo;

import br.ifsp.demo.exception.AnimalJaEmAtendimentoException;
import br.ifsp.demo.model.abertura.*;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import br.ifsp.demo.service.abertura.AtendimentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtendimentoServiceTest {
    private InMemoryAtendimentoRepository repository;
    private AtendimentoService service;

    @BeforeEach()
    void prepara(){
        repository = new InMemoryAtendimentoRepository();
        service = new AtendimentoService(repository);
    }

    @Test
    @DisplayName("# C0101 Devo abrir pronto atendimento para animal sem agendamento prévio")
    void devoAbrirProntoAtendimentoComSucesso(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);

        assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
        assertThat(atendimento.getAgendamentoId()).isNull();
    }

    @Test
    @DisplayName("# C0102 Não devo abrir pronto atendimento quando animal já estiver em atendimento")
    void naoDevoAbrirProntoAtendimento(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        service.abrirProntoAtendimento(animalId);

        assertThrows(AnimalJaEmAtendimentoException.class, ()-> service.abrirProntoAtendimento(animalId));
    }

    @Test
    @DisplayName("# C0103 Cancelar atendimento com justificativa com sucesso")
    void devoCancelarProntoAtendimentoComSucesso(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        String justificativa = "Animal estava muito inquieto e o dono optou por ir embora";
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        Atendimento cancelado = service.cancelarAtendimento(atendimento.getId(), justificativa);

        assertThat(cancelado.getStatus()).isEqualTo(StatusAtendimento.CANCELADO);
        assertThat(cancelado.getJustificativa()).isEqualTo("Animal estava muito inquieto e o dono optou por ir embora");
    }

    @Test
    @DisplayName("# C0201 Devo abrir atendimento para animal dado agendamento")
    void devoAbrirAtendimentoComAgendamento(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        AgendamentoId agendamentoId = AgendamentoId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirAtendimentoComAgendamento(animalId, agendamentoId);

        assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
        assertThat(atendimento.getAgendamentoId()).isEqualTo(agendamentoId);
        assertThat(atendimento.getAnimalId()).isEqualTo(animalId);
    }
}
