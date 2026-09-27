package br.ifsp.demo.model.prescricao;

import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AnimalId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class PrescricaoTest {

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1001 - deve criar nova prescricao com status ABERTA")
    void deveCriarNovaPrescricaoComStatusAberta(){
        UUID animalId = UUID.randomUUID();
        Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(animalId));

        List<ItemPrescricao> itens = List.of(
                new ItemPrescricao("Dipirona", 1, "Oral", "12/12h", 7)
        );

        Prescricao prescricao = atendimento.emitirPrescricao(itens);

        assertEquals(StatusPrescricao.ABERTA, prescricao.getStatus());
        assertTrue(atendimento.getPrescricoes().contains(prescricao));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1002 - deve rejeitar prescricao quando atendimento nao esta em andamento")
    void deveRejeitarPrescricaoQuandoAtendimentoNaoEstaEmAndamento(){
        UUID animalId = UUID.randomUUID();
        Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(animalId));
        atendimento.cancelar("TESTE");

        List<ItemPrescricao> itens = List.of(
                new ItemPrescricao("Dipirona", 1, "Oral", "12/12h", 7)
        );

        assertThatThrownBy(() -> atendimento.emitirPrescricao(itens))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Emissão rejeitada: só é possível emitir uma prescrição durante um atendimento em andamento.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1003 - deve rejeitar emissao quando nao houver itens na prescricao")
    void deveRejeitarEmissaoQuandoNaoHouverItensNaPrescricao(){
        UUID animalId = UUID.randomUUID();
        Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(animalId));

        List<ItemPrescricao> itens = List.of();

        assertThatThrownBy(() -> atendimento.emitirPrescricao(itens))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Emissão rejeitada: só é possível emitir uma prescrição com pelo menos um item.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1101 - deve editar itens de uma prescricao em aberta")
    void deveEditarItensDeUmaPrescricaoEmAberta(){
        ItemPrescricao item = new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5);
        Prescricao prescricao = new Prescricao(List.of(item));

        ItemPrescricao itemEditado = new ItemPrescricao(item.getId(), "Amoxicilina", 1000, "Não oral", "12 em 12 horas", 7);
        prescricao.editarItem(item.getId(), itemEditado);

        ItemPrescricao itemAtualizado = prescricao.getItens().getFirst();
        assertEquals("Amoxicilina", itemAtualizado.getMedicamento());
        assertEquals(1000, itemAtualizado.getDosagem());
        assertEquals("Não oral", itemAtualizado.getVia());
        assertEquals("12 em 12 horas", itemAtualizado.getFrequencia());
        assertEquals(7, itemAtualizado.getDiasDuracao());
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1102 - deve remover um item de uma prescicao em aberta")
    void deveRemoverUmItemDeUmaPrescicaoEmAberta(){
        ItemPrescricao item = new ItemPrescricao(ItemPrescricaoId.novo(),"Dipirona", 500, "Oral", "8 em 8 horas", 5);
        ItemPrescricao item2 = new ItemPrescricao(ItemPrescricaoId.novo(), "Amoxicilina", 1000, "Não oral", "12 em 12 horas", 7);

        Prescricao prescricao = new Prescricao(List.of(item, item2));

        prescricao.excluirItem(item2.getId());

        assertThat(prescricao.getItens()).containsExactly(item);
    }
}