package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.AtendimentoNaoEncontradoException;
import br.ifsp.demo.exception.JanelaReaberturaExpiradaException;
import br.ifsp.demo.exception.JustificativaObrigatoriaException;
import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AtendimentoId;
import br.ifsp.demo.model.abertura.StatusAtendimento;
import br.ifsp.demo.model.clinico.Anamnese;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.model.clinico.TipoDiagnostico;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.junit.jupiter.api.function.Executable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

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
    @Tag("Functional")
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
    @Tag("UnitTest")
    @Tag("Functional")
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

    // =================================================================
    // VALOR LIMITE — janela de reabertura (24h)
    // =================================================================

    @ParameterizedTest(name = "reabrir {0} após conclusão -> deveReabrir={1}")
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("#VL01 Valor limite da janela de reabertura (24h)")
    @MethodSource("limitesJanelaReabertura")
    void valorLimiteJanelaReabertura(Duration tempoDecorrido, boolean deveReabrir) {
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarSinaisVitais(new SinaisVitais(38.5, 100, 77));
        atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
        service.concluirAtendimento(atendimento.getId());

        LocalDateTime instanteReabertura = LocalDateTime.now().plus(tempoDecorrido);

        if (deveReabrir) {
            assertDoesNotThrow(() -> service.reabrirAtendimento(atendimento.getId(), instanteReabertura));
        } else {
            assertThrows(JanelaReaberturaExpiradaException.class,
                    () -> service.reabrirAtendimento(atendimento.getId(), instanteReabertura));
        }
    }

    static Stream<Arguments> limitesJanelaReabertura() {
        return Stream.of(
                Arguments.of(Duration.ofHours(23).plusMinutes(59), true),   // logo antes do limite
                Arguments.of(Duration.ofHours(24), true),                   // exatamente no limite
                Arguments.of(Duration.ofHours(24).plusMinutes(1), false)    // logo depois do limite
        );
    }

    // =================================================================
    // PARTIÇÃO DE EQUIVALÊNCIA — faixas fisiológicas de SinaisVitais
    // Classes: negativo / zero
    // =================================================================

    @ParameterizedTest(name = "temperatura={0}, FC={1}, FR={2}")
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("#PE03 Não devo criar SinaisVitais com valores fisiologicamente inválidos")
    @CsvSource({
            "-1, 100, 24",     // temperatura negativa
            "0, 100, 24",      // temperatura zero
            "38.5, -1, 24",    // frequência cardíaca negativa
            "38.5, 0, 24",     // frequência cardíaca zero
            "38.5, 100, -1",   // peso negativo
            "38.5, 100, 0"     // peso zero
    })
    void naoDevoCriarSinaisVitaisComValoresInvalidos(double temperatura, int frequenciaCardiaca, int frequenciaRespiratoria) {
        assertThrows(RuntimeException.class,
                () -> new SinaisVitais(temperatura, frequenciaCardiaca, frequenciaRespiratoria));
    }

    // =================================================================
    // PARTIÇÃO DE EQUIVALÊNCIA — AtendimentoId existente x inexistente
    // =================================================================

    @ParameterizedTest(name = "{0} sobre atendimento inexistente")
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("#PE05 Não devo operar sobre um AtendimentoId que não existe no repositório")
    @MethodSource("acoesSobreAtendimentoInexistente")
    void naoDevoOperarSobreAtendimentoInexistente(String descricaoAcao, Executable acao) {
        assertThrows(AtendimentoNaoEncontradoException.class, acao);
    }

    static Stream<Arguments> acoesSobreAtendimentoInexistente() {
        InMemoryAtendimentoRepository repo = new InMemoryAtendimentoRepository();
        AtendimentoService srv = new AtendimentoService(repo);
        AtendimentoId idInexistente = AtendimentoId.novo();

        return Stream.of(
                Arguments.of("cancelarAtendimento",
                        (Executable) () -> srv.cancelarAtendimento(idInexistente, "pq sim")),
                Arguments.of("concluirAtendimento",
                        (Executable) () -> srv.concluirAtendimento(idInexistente)),
                Arguments.of("reabrirAtendimento",
                        (Executable) () -> srv.reabrirAtendimento(idInexistente, LocalDateTime.now()))
        );
    }

    // =================================================================
    // VALOR LIMITE — instante de reabertura anterior à conclusão
    // =================================================================

    @ParameterizedTest(name = "reabertura {0} antes da conclusão")
    @Tag("Funcional")
    @Tag("ValorLimite")
    @DisplayName("#VL02 Não devo reabrir atendimento com instante anterior ao da conclusão")
    @MethodSource("instantesAnterioresAConclusao")
    void naoDevoReabrirComInstanteAnteriorAConclusao(Duration antesDaConclusao) {
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarSinaisVitais(new SinaisVitais(38.5, 100, 77));
        atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
        service.concluirAtendimento(atendimento.getId());

        LocalDateTime instanteReabertura = LocalDateTime.now().minus(antesDaConclusao);

        assertThrows(RuntimeException.class,
                () -> service.reabrirAtendimento(atendimento.getId(), instanteReabertura));
    }

    static Stream<Arguments> instantesAnterioresAConclusao() {
        return Stream.of(
                Arguments.of(Duration.ofMinutes(1)),
                Arguments.of(Duration.ofHours(1)),
                Arguments.of(Duration.ofDays(1))
        );
    }


    // =================================================================
    // VALOR LIMITE — tamanho da queixa principal da anamnese
    // =================================================================

    @ParameterizedTest(name = "queixaPrincipal com {0} caracteres -> deve aceitar = {1}")
    @Tag("Funcional")
    @Tag("ValorLimite")
    @DisplayName("#VL03 Valor limite do tamanho da queixa principal da anamnese")
    @CsvSource({
            "99, true",
            "100, true",
            "101, false",
    })
    void valorLimiteQueixaPrincipal(int tamanho, boolean deveAceitar) {
        Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
        String queixa = "a".repeat(tamanho);

        if (deveAceitar) {
            Atendimento atualizado = service.registrarAnamnese(atendimento.getId(), new Anamnese(queixa, "histórico"));

            assertThat(atualizado.getAnamnese().queixaPrincipal()).hasSize(tamanho); //se a queixa tem o tamanho que eu dei
        } else {
            assertThrows(IllegalArgumentException.class,
                    () -> new Anamnese(queixa, "histórico")); //se nao, lança exceção
        }
    }


    // =================================================================
    // VALOR LIMITE — tamanho do histórico clínico da anamnese
    // =================================================================

    @ParameterizedTest(name = "historicoClinico com {0} caracteres -> deve aceitar = {1}")
    @Tag("Funcional")
    @Tag("ValorLimite")
    @DisplayName("#VL04 Valor limite do tamanho do histórico clínico da anamnese")
    @CsvSource({
            "499, true",
            "500, true",
            "501, false",
    })
    void valorLimiteHistoricoClinico(int tamanho, boolean deveAceitar) {
        Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
        String historico = "a".repeat(tamanho);

        if (deveAceitar) {
            Atendimento atualizado = service.registrarAnamnese(atendimento.getId(), new Anamnese("queixa", historico));

            assertThat(atualizado.getAnamnese().historicoClinico()).hasSize(tamanho);
        } else {
            assertThrows(IllegalArgumentException.class,
                    () -> new Anamnese("queixa", historico));
        }
    }


    // =================================================================
    // VALOR LIMITE — tamanho do código no diagnóstico
    // =================================================================

    @ParameterizedTest(name = "codigo com {0} caracteres -> deve aceitar = {1}")
    @Tag("Funcional")
    @Tag("ValorLimite")
    @DisplayName("#VL05 Valor limite do tamanho do código do diagnóstico")
    @CsvSource({
            "9, true",
            "10, true",
            "11, false",
    })
    void valorLimiteCodigo(int tamanho, boolean deveAceitar) {
        Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
        String codigo = "a".repeat(tamanho);

        if (deveAceitar) {
            Atendimento atualizado = service.registrarDiagnostico(atendimento.getId(), new Diagnostico(codigo, "descricao", TipoDiagnostico.PRESUNTIVO));

            assertThat(atualizado.getDiagnosticos().getFirst().codigo()).hasSize(tamanho);

        } else {
            assertThrows(IllegalArgumentException.class,
                    () -> new Diagnostico(codigo, "descricao", TipoDiagnostico.PRESUNTIVO));
        }
    }


    // =================================================================
    // VALOR LIMITE — tamanho da descrição do diagnóstico (máx. 200)
    // =================================================================

        @ParameterizedTest(name = "descricao com {0} caracteres -> deveAceitar={1}")
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("#VL06 Valor limite do tamanho da descrição do diagnóstico")
        @CsvSource({
                "199, true",
                "200, true",
                "201, false"
        })
        void valorLimiteDescricaoDiagnostico(int tamanho, boolean deveAceitar) {
            Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            String descricao = "a".repeat(tamanho);

            if (deveAceitar) {
                Atendimento atualizado = service.registrarDiagnostico(atendimento.getId(),
                        new Diagnostico("A001", descricao, TipoDiagnostico.PRESUNTIVO));

                assertThat(atualizado.getDiagnosticos().getFirst().descricao()).hasSize(tamanho);
            } else {
                assertThrows(IllegalArgumentException.class,
                        () -> new Diagnostico("A001", descricao, TipoDiagnostico.PRESUNTIVO));
            }
        }


}