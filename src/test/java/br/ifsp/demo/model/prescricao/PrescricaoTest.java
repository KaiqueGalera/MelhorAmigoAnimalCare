package br.ifsp.demo.model.prescricao;

import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AnimalId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrescricaoTest {

    private static Atendimento atendimentoEmAndamento() {
        return Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
    }

    private static ItemPrescricao itemValido() {
        return new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1001 - deve criar nova prescricao com status ABERTA")
    void deveCriarNovaPrescricaoComStatusAberta(){
        Atendimento atendimento = atendimentoEmAndamento();

        Prescricao prescricao = atendimento.emitirPrescricao(List.of(itemValido()));

        assertEquals(StatusPrescricao.ABERTA, prescricao.getStatus());
        assertThat(atendimento.getPrescricoes()).contains(prescricao);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1002 - deve rejeitar prescricao quando atendimento nao esta em andamento")
    void deveRejeitarPrescricaoQuandoAtendimentoNaoEstaEmAndamento(){
        Atendimento atendimento = atendimentoEmAndamento();
        atendimento.cancelar("TESTE");

        assertThatThrownBy(() -> atendimento.emitirPrescricao(List.of(itemValido())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Emissão rejeitada: só é possível emitir uma prescrição durante um atendimento em andamento.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1003 - deve rejeitar emissao quando nao houver itens na prescricao")
    void deveRejeitarEmissaoQuandoNaoHouverItensNaPrescricao(){
        Atendimento atendimento = atendimentoEmAndamento();

        assertThatThrownBy(() -> atendimento.emitirPrescricao(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Emissão rejeitada: só é possível emitir uma prescrição com pelo menos um item.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1101 - deve editar itens de uma prescricao em aberta")
    void deveEditarItensDeUmaPrescricaoEmAberta(){
        ItemPrescricao item = itemValido();
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
        ItemPrescricao item = itemValido();
        ItemPrescricao item2 = new ItemPrescricao("Amoxicilina", 1000, "Não oral", "12 em 12 horas", 7);
        Prescricao prescricao = new Prescricao(List.of(item, item2));

        prescricao.removerItem(item2.getId());

        assertThat(prescricao.getItens()).containsExactly(item);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1201 - deve finalizar prescricao aberta com pelo menos um item")
    void deveFinalizarPrescricaoAbertaComPeloMenosUmItem(){
        Prescricao prescricao = new Prescricao(List.of(itemValido()));

        prescricao.finalizar();

        assertEquals(StatusPrescricao.FINALIZADA, prescricao.getStatus());
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1202 - deve rejeitar quando finalizar prescricao sem itens")
    void deveRejeitarQuandoFinalizarPrescricaoSemItens(){
        ItemPrescricao item = itemValido();
        Prescricao prescricao = new Prescricao(List.of(item));
        prescricao.removerItem(item.getId());

        assertThatThrownBy(prescricao::finalizar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A prescrição deve conter pelo menos um item para ser finalizada.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1301 - deve cancelar uma prescricao em aberta")
    void deveCancelarUmaPrescricaoEmAberta(){
        Prescricao prescricao = new Prescricao(List.of(itemValido()));

        prescricao.cancelar();

        assertEquals(StatusPrescricao.CANCELADA, prescricao.getStatus());
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1302 - deve rejeitar cancelamento de prescricao finalizada")
    void deveRejeitarCancelamentoDePrescricaoFinalizada(){
        Prescricao prescricao = new Prescricao(List.of(itemValido()));
        prescricao.finalizar();

        assertThatThrownBy(prescricao::cancelar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A prescrição deve estar 'Aberta' para ser cancelada.");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("acoesInvalidasQuandoNaoAberta")
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1103/C1203 - deve rejeitar operacao quando prescricao nao esta aberta")
    void deveRejeitarOperacaoQuandoPrescricaoNaoEstaAberta(
            String descricao, BiConsumer<Prescricao, ItemPrescricao> acao, String mensagemEsperada){
        ItemPrescricao item = itemValido();
        Prescricao prescricao = new Prescricao(List.of(item));
        prescricao.finalizar();

        assertThatThrownBy(() -> acao.accept(prescricao, item))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(mensagemEsperada);
    }

    static Stream<Arguments> acoesInvalidasQuandoNaoAberta() {
        String mensagemEditarRemover = "Prescrição deve estar 'Aberta' para ser alterada";
        String mensagemFinalizar = "A prescrição deve estar 'Aberta' para ser finalizada.";
        return Stream.of(
                Arguments.of("editar item",
                        (BiConsumer<Prescricao, ItemPrescricao>) (p, item) -> p.editarItem(item.getId(), item),
                        mensagemEditarRemover),
                Arguments.of("remover item",
                        (BiConsumer<Prescricao, ItemPrescricao>) (p, item) -> p.removerItem(item.getId()),
                        mensagemEditarRemover),
                Arguments.of("finalizar",
                        (BiConsumer<Prescricao, ItemPrescricao>) (p, item) -> p.finalizar(),
                        mensagemFinalizar)
        );
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("C1701 - deve exibir detalhes de todas as prescricoes vinculadas ao atendimento")
    void deveExibirDetalhesDeTodasAsPrescricoesVinculadasAoAtendimento() {
        Atendimento atendimento = atendimentoEmAndamento();
        atendimento.emitirPrescricao(List.of(itemValido()));

        List<Prescricao> prescricoes = atendimento.getPrescricoes();

        assertThat(prescricoes).hasSize(1);
        assertThat(prescricoes).allSatisfy(prescricao -> {
            assertThat(prescricao.getStatus()).isEqualTo(StatusPrescricao.ABERTA);
            assertThat(prescricao.getDataHoraEmissao()).isInstanceOf(LocalDateTime.class);
        });
    }

    @ParameterizedTest(name = "dosagem = {0}")
    @Tag("UnitTest")
    @Tag("Functional")
    @NullSource
    @ValueSource(ints = {0, -1})
    @DisplayName("US10a - deve rejeitar item com dosagem invalida")
    void deveRejeitarItemComDosagemInvalida(Integer dosagem) {
        assertThatThrownBy(() -> new ItemPrescricao("Dipirona", dosagem, "Oral", "12/12h", 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor Inválido: Dosagem deve ser maior que zero");
    }

    @ParameterizedTest(name = "medicamento = \"{0}\"")
    @Tag("UnitTest")
    @Tag("Functional")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("US10b - deve rejeitar item com medicamento invalido")
    void deveRejeitarItemComMedicamentoInvalido(String medicamento) {
        assertThatThrownBy(() -> new ItemPrescricao(medicamento, 500, "Oral", "12/12h", 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor Inválido: Medicamento é obrigatório");
    }

    @ParameterizedTest(name = "via = \"{0}\"")
    @Tag("UnitTest")
    @Tag("Functional")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("US10c - deve rejeitar item com via de administracao invalida")
    void deveRejeitarItemComViaInvalida(String via) {
        assertThatThrownBy(() -> new ItemPrescricao("Dipirona", 500, via, "12/12h", 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor Inválido: Via de administração é obrigatória");
    }

    @ParameterizedTest(name = "frequencia = \"{0}\"")
    @Tag("UnitTest")
    @Tag("Functional")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("US10d - deve rejeitar item com frequencia invalida")
    void deveRejeitarItemComFrequenciaInvalida(String frequencia) {
        assertThatThrownBy(() -> new ItemPrescricao("Dipirona", 500, "Oral", frequencia, 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor Inválido: Frequência é obrigatória");
    }

    @ParameterizedTest(name = "duracao = {0}")
    @Tag("UnitTest")
    @Tag("Functional")
    @NullSource
    @ValueSource(ints = {0, -1})
    @DisplayName("US10e - deve rejeitar item com duracao invalida")
    void deveRejeitarItemComDuracaoInvalida(Integer duracao) {
        assertThatThrownBy(() -> new ItemPrescricao("Dipirona", 500, "Oral", "12/12h", duracao))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor Inválido: Duração deve ser maior que zero");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("acoesComIdInexistente")
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US11a - deve rejeitar operacao com ID de item inexistente")
    void deveRejeitarOperacaoComIdDeItemInexistente(String descricao, BiConsumer<Prescricao, ItemPrescricaoId> acao) {
        Prescricao prescricao = new Prescricao(List.of(itemValido()));
        ItemPrescricaoId idInexistente = ItemPrescricaoId.novo();

        assertThatThrownBy(() -> acao.accept(prescricao, idInexistente))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item de prescrição não encontrado.");
    }

    static Stream<Arguments> acoesComIdInexistente() {
        return Stream.of(
                Arguments.of("editar item",
                        (BiConsumer<Prescricao, ItemPrescricaoId>) (p, id) ->
                                p.editarItem(id, new ItemPrescricao("Amoxicilina", 1000, "Oral", "12/12h", 7))),
                Arguments.of("remover item",
                        (BiConsumer<Prescricao, ItemPrescricaoId>) Prescricao::removerItem)
        );
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US11b - deve rejeitar edicao quando novo item tem ID divergente da chave")
    void deveRejeitarEdicaoQuandoNovoItemTemIdDivergente() {
        ItemPrescricao item = itemValido();
        Prescricao prescricao = new Prescricao(List.of(item));
        ItemPrescricao comOutroId = new ItemPrescricao("Amoxicilina", 1000, "Oral", "12/12h", 7);

        assertThatThrownBy(() -> prescricao.editarItem(item.getId(), comOutroId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O id do item editado deve ser igual ao id original.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US11c - deve permitir remover o ultimo item restante retornando prescricao vazia")
    void devePermitirRemoverUltimoItemRestanteRetornandoPrescricaoVazia() {
        ItemPrescricao item = itemValido();
        Prescricao prescricao = new Prescricao(List.of(item));

        prescricao.removerItem(item.getId());

        assertThat(prescricao.getItens()).isEmpty();
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US11d - deve rejeitar edicao com id nulo")
    void deveRejeitarEdicaoComIdNulo() {
        Prescricao prescricao = new Prescricao(List.of(itemValido()));

        assertThatThrownBy(() -> prescricao.editarItem(null, itemValido()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US11e - deve rejeitar edicao com novo item nulo")
    void deveRejeitarEdicaoComNovoItemNulo() {
        ItemPrescricao item = itemValido();
        Prescricao prescricao = new Prescricao(List.of(item));

        assertThatThrownBy(() -> prescricao.editarItem(item.getId(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "cancelada - {0}")
    @MethodSource("acoesInvalidasQuandoCancelada")
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US12a - deve rejeitar operacao quando prescricao esta cancelada")
    void deveRejeitarOperacaoQuandoPrescricaoEstaCancelada(
            String descricao, BiConsumer<Prescricao, ItemPrescricao> acao, String mensagemEsperada) {
        ItemPrescricao item = itemValido();
        Prescricao prescricao = new Prescricao(List.of(item));
        prescricao.cancelar();

        assertThatThrownBy(() -> acao.accept(prescricao, item))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(mensagemEsperada);
    }

    static Stream<Arguments> acoesInvalidasQuandoCancelada() {
        String mensagemEditarRemover = "Prescrição deve estar 'Aberta' para ser alterada";
        String mensagemFinalizar = "A prescrição deve estar 'Aberta' para ser finalizada.";
        return Stream.of(
                Arguments.of("editar item",
                        (BiConsumer<Prescricao, ItemPrescricao>) (p, item) -> p.editarItem(item.getId(), item),
                        mensagemEditarRemover),
                Arguments.of("remover item",
                        (BiConsumer<Prescricao, ItemPrescricao>) (p, item) -> p.removerItem(item.getId()),
                        mensagemEditarRemover),
                Arguments.of("finalizar",
                        (BiConsumer<Prescricao, ItemPrescricao>) (p, item) -> p.finalizar(),
                        mensagemFinalizar)
        );
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US13a - deve rejeitar cancelamento de prescricao ja cancelada")
    void deveRejeitarCancelamentoDePrescricaoJaCancelada() {
        Prescricao prescricao = new Prescricao(List.of(itemValido()));
        prescricao.cancelar();

        assertThatThrownBy(prescricao::cancelar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A prescrição deve estar 'Aberta' para ser cancelada.");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US14a - deve retornar lista vazia quando atendimento nao possui nenhuma prescricao")
    void deveRetornarListaVaziaQuandoAtendimentoNaoPossuiNenhumaPrescricao() {
        Atendimento atendimento = atendimentoEmAndamento();

        assertThat(atendimento.getPrescricoes()).isEmpty();
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US14b - deve exibir detalhes de prescricao com multiplos itens")
    void deveExibirDetalhesDePrescricaoComMultiplosItens() {
        Atendimento atendimento = atendimentoEmAndamento();
        ItemPrescricao item1 = itemValido();
        ItemPrescricao item2 = new ItemPrescricao("Amoxicilina", 1000, "Oral", "12/12h", 10);
        atendimento.emitirPrescricao(List.of(item1, item2));

        Prescricao prescricao = atendimento.getPrescricoes().get(0);

        assertThat(prescricao.getItens()).containsExactly(item1, item2);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US14c - deve exibir todas as prescricoes quando atendimento possui mais de uma")
    void deveExibirTodasAsPrescricoesQuandoAtendimentoPossuiMaisDeUma() {
        Atendimento atendimento = atendimentoEmAndamento();
        Prescricao prescricaoCancelada = atendimento.emitirPrescricao(List.of(itemValido()));
        prescricaoCancelada.cancelar();
        atendimento.emitirPrescricao(List.of(itemValido()));

        List<Prescricao> prescricoes = atendimento.getPrescricoes();

        assertThat(prescricoes).hasSize(2);
        assertThat(prescricoes).extracting(Prescricao::getStatus)
                .containsExactlyInAnyOrder(StatusPrescricao.CANCELADA, StatusPrescricao.ABERTA);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US14d - nao deve exibir prescricoes de outro atendimento")
    void naoDeveExibirPrescricoesDeOutroAtendimento() {
        Atendimento atendimentoA = atendimentoEmAndamento();
        Atendimento atendimentoB = atendimentoEmAndamento();

        atendimentoA.emitirPrescricao(List.of(itemValido()));

        assertThat(atendimentoA.getPrescricoes()).hasSize(1);
        assertThat(atendimentoB.getPrescricoes()).isEmpty();
    }
}