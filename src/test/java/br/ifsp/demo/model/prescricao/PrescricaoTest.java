package br.ifsp.demo.model.prescricao;

import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AnimalId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

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

        prescricao.removerItem(item2.getId());

        assertThat(prescricao.getItens()).containsExactly(item);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("acoesInvalidasQuandoNaoAberta")
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1103 - deve retornar erro quando editar ou remover um item existente quando prescricao nao esta aberta")
    void deveRetornarErroQuandoEditarOuRemoverUmItemExistenteQuandoPrescricaoNaoEstaAberta(String descricao, BiConsumer<Prescricao, ItemPrescricao> acao){
        ItemPrescricao item = new ItemPrescricao(ItemPrescricaoId.novo(),"Dipirona", 500, "Oral", "8 em 8 horas", 5);
        Prescricao prescricao = new Prescricao(List.of(item));
        prescricao.finalizar();

        assertThatThrownBy(() -> acao.accept(prescricao, item))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Prescrição deve estar 'Aberta' para ser alterada");
    }

    static Stream<Arguments> acoesInvalidasQuandoNaoAberta() {
        return Stream.of(
                Arguments.of("editar item",
                        (BiConsumer<Prescricao, ItemPrescricao>) (p, item) -> p.editarItem(item.getId(), item)),
                Arguments.of("remover item",
                        (BiConsumer<Prescricao, ItemPrescricao>) (p, item) -> p.removerItem(item.getId()))
        );
    }
    
    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1201 - deve finalizar prescricao aberta com pelo menos um item")
    void deveFinalizarPrescricaoAbertaComPeloMenosUmItem(){
        ItemPrescricao item = new ItemPrescricao(ItemPrescricaoId.novo(),"Dipirona", 500, "Oral", "8 em 8 horas", 5);
        Prescricao prescricao = new Prescricao(List.of(item));

        prescricao.finalizar();

        assertEquals(StatusPrescricao.FINALIZADA, prescricao.getStatus());
    }
    
    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1202 - deve rejeitar quando finalizar prescricao sem itens")
    void deveRejeitarQuandoFinalizarPrescricaoSemItens(){
        ItemPrescricao item = new ItemPrescricao(ItemPrescricaoId.novo(),"Dipirona", 500, "Oral", "8 em 8 horas", 5);
        Prescricao prescricao = new Prescricao(List.of(item));
        prescricao.removerItem(item.getId());

        assertThatThrownBy(prescricao::finalizar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A prescrição deve conter pelo menos um item para ser finalizada.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1203 - deve rejeitar quando finalizar uma prescricao nao aberta")
    void deveRejeitarQuandoFinalizarUmaPrescricaoNaoAberta(){
        ItemPrescricao item = new ItemPrescricao(ItemPrescricaoId.novo(),"Dipirona", 500, "Oral", "8 em 8 horas", 5);
        Prescricao prescricao = new Prescricao(List.of(item));
        prescricao.finalizar();

        assertThatThrownBy(prescricao::finalizar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A prescrição deve estar 'Aberta' para ser finalizada.");
    }
    
    @Test
    @DisplayName("C1301 - deve cancelar uma prescricao em aberta")
    void deveCancelarUmaPrescricaoEmAberta(){
        ItemPrescricao item = new ItemPrescricao(ItemPrescricaoId.novo(),"Dipirona", 500, "Oral", "8 em 8 horas", 5);
        Prescricao prescricao = new Prescricao(List.of(item));
        prescricao.cancelar();

        assertEquals(StatusPrescricao.CANCELADA, prescricao.getStatus());
    }
}