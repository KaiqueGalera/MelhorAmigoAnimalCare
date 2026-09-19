package br.ifsp.demo;

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

        assertThat(atendimento.getStatus().isEqualTo(StatusAtendimentos.EM_ANDAMENTO));
        assertThat(atendimento.getAgendamento()).isNull();
    }
}
