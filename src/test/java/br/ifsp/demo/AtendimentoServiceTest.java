package br.ifsp.demo;

import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.StatusAtendimento;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import br.ifsp.demo.service.abertura.AtendimentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtendimentoServiceTest {
    private InMemoryAtendimentoRepository repository;
    private AtendimentoService service;

    @Test
    @DisplayName("# 2 Devo abrir pronto atendimento para animal sem agendamento prévio")
    void devoAbrirProntoAtendimentoComSucesso(){
        AnimalId animalId = AnimalId.novo();
        repository = new InMemoryAtendimentoRepository();
        service = new AtendimentoService(repository);
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);

        assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
        assertThat(atendimento.getAgendamentoId()).isNull();
    }

    @Test
    @DisplayName("# 3 Não devo abrir pronto atendimento quando animal já estiver em atendimento")
    void naoDevoAbrirProntoAtendimento(){
        AnimalId animalId = AnimalId.novo();
        repository = new InMemoryAtendimentoRepository();
        service = new AtendimentoService(repository);
        service.abrirProntoAtendimento(animalId);


        assertThrows(IllegalStateException.class, ()-> service.abrirProntoAtendimento(animalId));
    }
}
