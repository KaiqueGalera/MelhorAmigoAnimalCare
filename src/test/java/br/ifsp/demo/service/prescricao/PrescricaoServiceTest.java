package br.ifsp.demo.service.prescricao;

import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.prescricao.ItemPrescricao;
import br.ifsp.demo.model.prescricao.Prescricao;
import br.ifsp.demo.model.prescricao.StatusPrescricao;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import br.ifsp.demo.service.abertura.AtendimentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrescricaoServiceTest {
    private final InMemoryAtendimentoRepository repository = new InMemoryAtendimentoRepository();
    private final AtendimentoService atendimentoService = new AtendimentoService(repository);
    private final PrescricaoService prescricaoService = new PrescricaoService(repository);

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1001 - deve emitir prescricao com status ABERTA via service")
    void deveEmitirPrescricaoComStatusAbertaViaService() {
        Atendimento atendimento = atendimentoService.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

        List<ItemPrescricao> itens = List.of(
                new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5)
        );

        Prescricao prescricao = prescricaoService.emitirPrescricao(atendimento.getId(), itens);

        assertThat(prescricao.getStatus()).isEqualTo(StatusPrescricao.ABERTA);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1701 - deve consultar todas as prescricoes de um atendimento via service")
    void deveConsultarTodasAsPrescricoesDeUmAtendimentoViaService() {
        Atendimento atendimento = atendimentoService.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
        ItemPrescricao item = new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5);
        prescricaoService.emitirPrescricao(atendimento.getId(), List.of(item));

        List<Prescricao> prescricoes = prescricaoService.consultarPrescricoes(atendimento.getId());

        assertThat(prescricoes).hasSize(1);
        assertThat(prescricoes.getFirst().getItens()).containsExactly(item);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1101 - deve editar item de prescricao via service")
    void deveEditarItemDePrescricaoViaService() {
        Atendimento atendimento = atendimentoService.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
        ItemPrescricao item = new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5);
        Prescricao prescricao = prescricaoService.emitirPrescricao(atendimento.getId(), List.of(item));

        ItemPrescricao itemEditado = new ItemPrescricao(item.getId(), "Amoxicilina", 1000, "Oral", "12 em 12 horas", 7);
        prescricaoService.editarItem(atendimento.getId(), prescricao.getId(), item.getId(), itemEditado);

        Prescricao prescricaoAtualizada = prescricaoService.consultarPrescricoes(atendimento.getId()).get(0);
        assertThat(prescricaoAtualizada.getItens()).containsExactly(itemEditado);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1102 - deve remover item de prescricao via service")
    void deveRemoverItemDePrescricaoViaService() {
        Atendimento atendimento = atendimentoService.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
        ItemPrescricao item1 = new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5);
        ItemPrescricao item2 = new ItemPrescricao("Amoxicilina", 250, "Oral", "8/8h", 10);
        Prescricao prescricao = prescricaoService.emitirPrescricao(atendimento.getId(), List.of(item1, item2));

        prescricaoService.removerItem(atendimento.getId(), prescricao.getId(), item1.getId());

        Prescricao prescricaoAtualizada = prescricaoService.consultarPrescricoes(atendimento.getId()).get(0);
        assertThat(prescricaoAtualizada.getItens()).containsExactly(item2);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1201 - deve finalizar prescricao via service")
    void deveFinalizarPrescricaoViaService() {
        Atendimento atendimento = atendimentoService.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
        ItemPrescricao item = new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5);
        Prescricao prescricao = prescricaoService.emitirPrescricao(atendimento.getId(), List.of(item));

        prescricaoService.finalizar(atendimento.getId(), prescricao.getId());

        Prescricao prescricaoAtualizada = prescricaoService.consultarPrescricoes(atendimento.getId()).get(0);
        assertThat(prescricaoAtualizada.getStatus()).isEqualTo(StatusPrescricao.FINALIZADA);
    }
}