package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.AtendimentoNaoEncontradoException;
import br.ifsp.demo.exception.JanelaReaberturaExpiradaException;
import br.ifsp.demo.exception.JustificativaObrigatoriaException;
import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AtendimentoId;
import br.ifsp.demo.model.abertura.StatusAtendimento;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.model.clinico.TipoDiagnostico;
import br.ifsp.demo.model.exame.Exame;
import br.ifsp.demo.model.exame.ExameId;
import br.ifsp.demo.model.exame.ExameSolicitado;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.junit.jupiter.api.function.Executable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("deve adicionar um único exame solicitado a lista de exames do atendimento.")
    void deveAdicionarUmUnicoExameSolicitadoAListaDeExamesDoAtendimento() {
        AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                2026,
                Month.OCTOBER,
                7,
                9,
                1)
        );

        Atendimento atendimento = serviceLocal.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

        Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");

        ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                2026,
                Month.OCTOBER,
                8,
                10,
                0)
        );

        Atendimento atendimentoAtualizado = serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1));

        var exames = atendimentoAtualizado.getExames();
        assertThat(exames.size()).isEqualTo(1);
        assertThat(exames.contains(exameSolicitado1)).isEqualTo(true);
        assertThat(atendimento).isEqualTo(atendimentoAtualizado);
    }

    @ParameterizedTest
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("deve rejeitar exame com horário posterior ao limite permitido.")
    @ValueSource(strings = {
            "08:59",
            "18:01"
    })
    void deveRejeitarExameComHorarioPosteriorAoLimitePermitido(String horario) {
        AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                2026,
                Month.OCTOBER,
                7,
                9,
                0)
        );
        Atendimento atendimento = serviceLocal.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

        Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
        Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

        LocalTime horaExame = LocalTime.parse(horario);

        ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                2026,
                Month.OCTOBER,
                8,
                horaExame.getHour(),
                horaExame.getMinute())
        );
        ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                2026,
                Month.OCTOBER,
                8,
                horaExame.getHour(),
                horaExame.getMinute())
        );

        assertThatThrownBy(() -> serviceLocal.solicitarExames(
                atendimento.getId(),
                List.of(exameSolicitado1, exameSolicitado2)
        ))
                .isInstanceOf(IllegalStateException.class).hasMessage("Solicitação de exame rejeitada: todos os exames devem estar vinculados a um horário válido.");
    }
}