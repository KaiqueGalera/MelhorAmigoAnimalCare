package br.ifsp.demo;

import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.StatusAtendimento;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import br.ifsp.demo.service.abertura.AtendimentoService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AtendimentoServiceTest {
    private InMemoryAtendimentoRepository repository;
    private AtendimentoService service;

    @Test
    void devoAbrirProntoAtendimentoComSucesso(){
        AnimalId animalId = AnimalId.novo();
        repository = new InMemoryAtendimentoRepository();
        service = new AtendimentoService(repository);
        Atendimento atendimento = service.abrirProntoAtendimento;

        assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
        assertThat(atendimento.getAgendamento()).isNull();
    }
}
