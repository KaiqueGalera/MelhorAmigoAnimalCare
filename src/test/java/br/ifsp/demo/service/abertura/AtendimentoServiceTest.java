package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.*;
import br.ifsp.demo.model.abertura.*;
import br.ifsp.demo.model.clinico.Anamnese;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.model.clinico.TipoDiagnostico;
import br.ifsp.demo.model.exame.Exame;
import br.ifsp.demo.model.exame.ExameId;
import br.ifsp.demo.model.exame.ExameSolicitado;
import br.ifsp.demo.model.exame.ExameStatus;
import br.ifsp.demo.model.prescricao.ItemPrescricao;
import br.ifsp.demo.model.prescricao.Prescricao;
import br.ifsp.demo.model.prescricao.PrescricaoId;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.time.*;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtendimentoServiceTest {
    private InMemoryAtendimentoRepository repository;
    private AtendimentoService service;

    @BeforeEach()
    void prepara() {
        repository = new InMemoryAtendimentoRepository();
        service = new AtendimentoService(repository);
    }

    @Nested
    @DisplayName("Functional Tests - TDD")
    class FunctionalTestsTDD{
        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("# C0101 Devo abrir pronto atendimento para animal sem agendamento prévio")
        void devoAbrirProntoAtendimentoComSucesso() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            Atendimento atendimento = service.abrirProntoAtendimento(animalId);

            assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
            assertThat(atendimento.getAgendamentoId()).isNull();
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("# C0102 Não devo abrir pronto atendimento quando animal já estiver em atendimento")
        void naoDevoAbrirProntoAtendimento() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            service.abrirProntoAtendimento(animalId);

            assertThrows(AnimalJaEmAtendimentoException.class, () -> service.abrirProntoAtendimento(animalId));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("# C0103 Cancelar atendimento com justificativa com sucesso")
        void devoCancelarProntoAtendimentoComSucesso() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            String justificativa = "Animal estava muito inquieto e o dono optou por ir embora";
            Atendimento atendimento = service.abrirProntoAtendimento(animalId);
            Atendimento cancelado = service.cancelarAtendimento(atendimento.getId(), justificativa);

            assertThat(cancelado.getStatus()).isEqualTo(StatusAtendimento.CANCELADO);
            assertThat(cancelado.getJustificativa()).isEqualTo("Animal estava muito inquieto e o dono optou por ir embora");
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("# C0201 Devo abrir atendimento para animal dado agendamento")
        void devoAbrirAtendimentoComAgendamento() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            AgendamentoId agendamentoId = AgendamentoId.of(UUID.randomUUID());
            Atendimento atendimento = service.abrirAtendimentoComAgendamento(animalId, agendamentoId);

            assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
            assertThat(atendimento.getAgendamentoId()).isEqualTo(agendamentoId);
            assertThat(atendimento.getAnimalId()).isEqualTo(animalId);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("# C0202 Não devo abrir atendimento agendado dado animal em atendimento")
        void naoDevoAbrirAtendimentoComAgendamento() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            AgendamentoId agendamentoId = AgendamentoId.of(UUID.randomUUID());
            service.abrirProntoAtendimento(animalId);

            assertThrows(AnimalJaEmAtendimentoException.class, () -> service.abrirAtendimentoComAgendamento(animalId, agendamentoId));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("# C0203-A Devo rejeitar associar agendamento com atendimento caso já tenha atendimento")
        void devoRejeitarAgendamentoJaUtilizado() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            AgendamentoId agendamentoId = AgendamentoId.of(UUID.randomUUID());
            service.abrirAtendimentoComAgendamento(animalId, agendamentoId);
            AnimalId outroAnimal = AnimalId.of(UUID.randomUUID());

            assertThrows(AgendamentoJaUtilizadoException.class, () -> service.abrirAtendimentoComAgendamento(outroAnimal, agendamentoId));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C0302 Não devo cancelar atendimento sem informar justificativa")
        void naoDevoCancelarAtendimentoSemJustificativa() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            Atendimento atendimento = service.abrirProntoAtendimento(animalId);

            assertThrows(JustificativaObrigatoriaException.class,
                    () -> service.cancelarAtendimento(atendimento.getId(), null));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C0303 Não devo cancelar atendimento quando já cancelado")
        void naoDevoCancelarAtendimentoJaCancelado() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            Atendimento atendimento = service.abrirProntoAtendimento(animalId);
            service.cancelarAtendimento(atendimento.getId(), "Motivo para cancelar 1");

            assertThrows(AtendimentoCanceladoExcepiton.class,
                    () -> service.cancelarAtendimento(atendimento.getId(), "Outro motivo"));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C0401 Devo encerrar atendimento")
        void devoEncerrarAtendimento() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            Atendimento atendimento = service.abrirProntoAtendimento(animalId);
            atendimento.registrarSinaisVitais(new SinaisVitais(30.9, 100, 89));
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
            Atendimento concluido = service.concluirAtendimento(atendimento.getId());

            assertThat(concluido.getStatus()).isEqualTo(StatusAtendimento.CONCLUIDO);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C0402 Não devo encerrar atendimento sem sinais vitais registrados")
        void naoDevoEncerrarAtendimentoSemSinaisVitais() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            Atendimento atendimento = service.abrirProntoAtendimento(animalId);
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));

            assertThrows(SinaisVitaisObrigatoriosException.class, () -> service.concluirAtendimento(atendimento.getId()));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C0403 Não devo encerrar atendimento sem diagnóstico registrados")
        void naoDevoEncerrarAtendimentoSemDiagnostico() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            Atendimento atendimento = service.abrirProntoAtendimento(animalId);
            atendimento.registrarSinaisVitais(new SinaisVitais(30.3, 100, 89));

            assertThrows(DiagnosticoObrigatorioException.class, () -> service.concluirAtendimento(atendimento.getId()));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C0501 Devo reabrir atendimento concluído dentro da janela de tempo")
        void devoReabrirAtendimentoDentroDaJanela() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            Atendimento atendimento = service.abrirProntoAtendimento(animalId);
            atendimento.registrarSinaisVitais(new SinaisVitais(30.4, 100, 89));
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
            service.concluirAtendimento(atendimento.getId());
            Atendimento reaberto = service.reabrirAtendimento(atendimento.getId(), LocalDateTime.now());

            assertThat(reaberto.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C0502 Não devo reabrir atendimento concluído fora da janela de tempo")
        void naoDevoReabrirAtendimentoForaDaJanela() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            Atendimento atendimento = service.abrirProntoAtendimento(animalId);
            atendimento.registrarSinaisVitais(new SinaisVitais(30.6, 100, 89));
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
            service.concluirAtendimento(atendimento.getId());

            assertThrows(JanelaReaberturaExpiradaException.class, () -> service.reabrirAtendimento(atendimento.getId(), LocalDateTime.now().plusHours(25)));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C0503 Não devo reabrir atendimento cancelado")
        void naoDevoReabrirAtendimentoCancelado() {
            AnimalId animalId = AnimalId.of(UUID.randomUUID());
            Atendimento atendimento = service.abrirProntoAtendimento(animalId);
            atendimento.registrarSinaisVitais(new SinaisVitais(30.5, 100, 89));
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
            service.cancelarAtendimento(atendimento.getId(), "Alguma justificativa convincente");

            assertThrows(AtendimentoNaoConcluidoException.class, () -> service.reabrirAtendimento(atendimento.getId(), LocalDateTime.now()));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0405 - deve permitir conclusao de atendimento com prescricao cancelada")
        void devePermitirConclusaoDeAtendimentoComPrescricaoCancelada() {
            Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            ItemPrescricao item = new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5);
            atendimento.registrarSinaisVitais(new SinaisVitais(30.6, 100, 89));
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
            Prescricao prescricao = atendimento.emitirPrescricao(List.of(item));
            prescricao.cancelar();

            assertThatCode(atendimento::concluir).doesNotThrowAnyException();
            assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.CONCLUIDO);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0406 - deve rejeitar conclusao de atendimento com prescricao aberta")
        void deveRejeitarConclusaoDeAtendimentoComPrescricaoAberta() {
            Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            atendimento.registrarSinaisVitais(new SinaisVitais(30.6, 100, 89));
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));

            ItemPrescricao item = new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5);
            atendimento.emitirPrescricao(List.of(item));

            assertThatThrownBy(atendimento::concluir)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Finalização de atendimento rejeitada, não é possível a finalização de atendimentos com prescrições abertas.");
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0601 - deve adicionar os exames solicitados à lista de exames do atendimento.")
        void deveAdicionarOsExamesSolicitadosAListaDeExamesDoAtendimento() {
            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );
            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    10,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    10,
                    0)
            );

            Atendimento atendimentoAtualizado = serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2));

            var exames = atendimentoAtualizado.getExames();
            assertThat(exames.size()).isEqualTo(2);
            assertThat(exames.contains(exameSolicitado1)).isEqualTo(true);
            assertThat(exames.contains(exameSolicitado2)).isEqualTo(true);
            assertThat(atendimento).isEqualTo(atendimentoAtualizado);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0602 - não deve prosseguir com a solicitação caso a lista de exames solicitados esteja vazia.")
        void naoDeveProsseguirComASolicitacaoCasoAListaDeExamesSolicitadosEstejaVazia() {
            Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            assertThatThrownBy(() -> service.solicitarExames(
                    atendimento.getId(),
                    List.of()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Solicitação de exame rejeitada: pelo menos um exame deve ser selecionado para prosseguir.");
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0603 - não deve prosseguir com a solicitação caso o atendimento não exista.")
        void naoDeveProsseguirComASolicitacaoCasoOAtendimentoNaoExista() {
            Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.now().plusHours(1));
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.now().plusHours(1));

            assertThatThrownBy(() -> service.solicitarExames(
                    atendimento.getId(),
                    List.of(exameSolicitado1, exameSolicitado2)
            ))
                    .isInstanceOf(AtendimentoNaoEncontradoException.class);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0604 - não deve prosseguir com a solicitação caso pelo menos um exame possua uma data anterior a data do atendimento.")
        void naoDeveProsseguirComASolicitacaoCasoPeloMenosUmExamePossuaUmaDataAnteriorADataDoAtendimento() {
            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );
            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    6,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    10,
                    0)
            );

            assertThatThrownBy(() -> serviceLocal.solicitarExames(
                    atendimento.getId(),
                    List.of(exameSolicitado1, exameSolicitado2)
            ))
                    .isInstanceOf(IllegalStateException.class).hasMessage("Solicitação de exame rejeitada: todos os exames devem estar vinculados a uma data e horário válidos.");
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0605 - não deve prosseguir com a solicitação caso pelo menos um exame possua horário anterior ao horário do atendimento.")
        void naoDeveProsseguirComASolicitacaoCasoPeloMenosUmExamePossuaHorarioAnteriorAoHorarioDoAtendimento() {
            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );
            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    2)
            );

            assertThatThrownBy(() -> serviceLocal.solicitarExames(
                    atendimento.getId(),
                    List.of(exameSolicitado1, exameSolicitado2)
            ))
                    .isInstanceOf(IllegalStateException.class).hasMessage("Solicitação de exame rejeitada: todos os exames devem estar vinculados a uma data e horário válidos.");
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0606 - não deve prosseguir com a solicitação caso pelo menos um exame esteja fora do intervalo de horários permitidos.")
        void naoDeveProsseguirComASolicitacaoCasoPeloMenosUmExameEstejaForaDoIntervaloDeHorariosPermitidos() {
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

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    8,
                    59)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    9,
                    0)
            );

            assertThatThrownBy(() -> serviceLocal.solicitarExames(
                    atendimento.getId(),
                    List.of(exameSolicitado1, exameSolicitado2)
            ))
                    .isInstanceOf(IllegalStateException.class).hasMessage("Solicitação de exame rejeitada: todos os exames devem estar vinculados a um horário válido.");
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0701 - deve devolver uma lista com os exames que já foram feitos por um animal.")
        void deveDevolverUmaListaComOsExamesQueJaForamFeitosPorUmAnimal() {
            var animalId = AnimalId.of(UUID.randomUUID());

            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );

            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(animalId);

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    9,
                    9,
                    0)
            );

            serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2));

            List<ExameSolicitado> exames = repository.buscarExamesPorAnimalId(animalId);
            assertThat(exames.size()).isEqualTo(2);
            assertThat(exames.contains(exameSolicitado1)).isEqualTo(true);
            assertThat(exames.contains(exameSolicitado2)).isEqualTo(true);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0702 - deve devolver uma lista vazia caso o animal não tenha feito nenhum exame.")
        void deveDevolverUmaListaVaziaCasoOAnimalNaoTenhaFeitoNenhumExame() {
            List<ExameSolicitado> exames = repository.buscarExamesPorAnimalId(AnimalId.of(UUID.randomUUID()));
            assertThat(exames.isEmpty()).isEqualTo(true);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0703 - deve devolver uma lista com os exames do animal que foram realizados dentro do perído informado.")
        void deveDevolverUmaListaComOsExamesDoAnimalQueForamRealizadosDentroDoPeriodoInformado() {
            var animalId = AnimalId.of(UUID.randomUUID());

            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );

            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(animalId);

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    9,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado3 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    4,
                    9,
                    0)
            );

            serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2, exameSolicitado3));

            var dataInicial = LocalDate.of(2026, Month.OCTOBER, 8);
            var dataFinal = LocalDate.of(2026, Month.DECEMBER, 8);

            List<ExameSolicitado> exames = repository.buscarExamesPorAnimalIdEPeriodo(animalId, dataInicial, dataFinal);
            assertThat(exames.size()).isEqualTo(2);
            assertThat(exames.contains(exameSolicitado1)).isEqualTo(true);
            assertThat(exames.contains(exameSolicitado3)).isEqualTo(true);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0801 - deve salvar o resultado de um exame solicitado em um atendimento.")
        void deveSalvarOResultadoDeUmExameSolicitadoEmUmAtendimento() {
            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );

            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    9,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado3 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    4,
                    9,
                    0)
            );

            serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2, exameSolicitado3));

            var resultado = "O hemograma apresentou bons resultados, tudo certo!";

            var exame = serviceLocal.registrarResultadoExame(atendimento.getId(), exameSolicitado1.getExame().getId(), resultado);
            assertThat(exame).isEqualTo(exameSolicitado1);
            assertThat(exame.getResultado()).isEqualTo(resultado);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0802 - deve conseguir editar o resultado de um exame solicitado em um atendimento.")
        void deveConseguirEditarOResultadoDeUmExameSolicitadoEmUmAtendimento() {
            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );

            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    9,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado3 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    4,
                    9,
                    0)
            );

            serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2, exameSolicitado3));

            var resultado = "O hemograma apresentou bons resultados, tudo certo!";

            serviceLocal.registrarResultadoExame(atendimento.getId(), exameSolicitado1.getExame().getId(), resultado);

            var novoResultado = "O hemograma não apresentou bons resultados, o retorno deve ser agendado!";

            var exame = serviceLocal.registrarResultadoExame(atendimento.getId(), exameSolicitado1.getExame().getId(), novoResultado);

            assertThat(exame).isEqualTo(exameSolicitado1);
            assertThat(exame.getResultado()).isEqualTo(novoResultado);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0901 - deve devolver o status PENDENTE para um exame que ainda não possui resultado registrado.")
        void deveDevolverOStatusPendenteParaUmExameQueAindaNãoPossuiResultadoRegistrado() {
            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );

            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    9,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado3 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    4,
                    9,
                    0)
            );

            serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2, exameSolicitado3));

            assertThat(exameSolicitado1.getStatus()).isEqualTo(ExameStatus.PENDENTE);
            assertThat(exameSolicitado2.getStatus()).isEqualTo(ExameStatus.PENDENTE);
            assertThat(exameSolicitado3.getStatus()).isEqualTo(ExameStatus.PENDENTE);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C0902 - deve garantir que o status do exame seja alterado automaticamente para CONCLUIDO quando seu resultado é registrado.")
        void deveGarantirQueOStatusDoExameSejaAlteradoAutomaticamenteParaConcluidoQuandoSeuResultadoERegistrado() {
            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );

            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    9,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado3 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    4,
                    9,
                    0)
            );

            serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2, exameSolicitado3));

            var resultado = "O hemograma apresentou bons resultados, tudo certo!";

            var exame = serviceLocal.registrarResultadoExame(atendimento.getId(), exameSolicitado1.getExame().getId(), resultado);
            assertThat(exame).isEqualTo(exameSolicitado1);
            assertThat(exame.getStatus()).isEqualTo(ExameStatus.CONCLUIDO);
        }


        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C1401 - Deve registrar sinais vitais com sucesso em atendimento em andamento")
        void deveRegistrarSinaisVitaisComSucessoEmAtendimentoEmAndamento() {
            Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            atendimento.registrarSinaisVitais(new SinaisVitais(31.5, 110, 35.5));

            assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);

            assertThat(atendimento.getSinaisVitais().temperaturaC()).isEqualTo(31.5);
            assertThat(atendimento.getSinaisVitais().frequenciaCardiaca()).isEqualTo(110);
            assertThat(atendimento.getSinaisVitais().pesoKg()).isEqualTo(35.5);

        }

        @ParameterizedTest
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C1402 Não deve registrar sinais vitais com dados numéricos inválidos")
        @CsvSource({
                "-1, 110, 35.5", //temp neg
                "0, 110, 35.5", //temp 0
                "30.0, -1, 60", // freq neg
                "30.0, 0, 60", //freq 0
                "31.5, 200, -1", //peso neg
                "31.5, 200, 0", //peso 0
        })

        void naoDeveRegistrarSinaisVitaisComDadosInvalidos(double temperatura, int frequenciaCardiaca, double peso) {
            Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            assertThrows(IllegalArgumentException.class,
                    () -> service.registrarSinaisVitais(atendimento.getId(), new SinaisVitais(temperatura, frequenciaCardiaca, peso)));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C1403 Não deve registrar sinais vitais em atendimento inexistente")
        void naoDeveRegistrarSinaisVitaisEmAtendimentoInexistente() {
            AtendimentoId idInexistente = AtendimentoId.novo();
            //já esta testando no lançamento de AtendimentoNaoEncontradoException quando o id não está no repositório, então esse teste é um pouco inútil

            assertThrows(AtendimentoNaoEncontradoException.class,
                    () -> service.registrarSinaisVitais(idInexistente, new SinaisVitais(31.5, 110, 35.5)));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C1404 - Não deve registrar sinais vitais em atendimento concluido")
        void naoDeveRegistrarSinaisVitaisEmAtendimentoConcluido() {
            Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            atendimento.registrarSinaisVitais(new SinaisVitais(20.4, 95, 17.5)); //ja inserido
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
            atendimento.concluir(); //fechar atendimento

            assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.CONCLUIDO);

            assertThatThrownBy(() -> atendimento.registrarSinaisVitais(new SinaisVitais(38.0, 90, 10.0)))
                    .isInstanceOf(IllegalStateException.class); //registrar sinais vitais depois de concluido
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C1501 - Deve registrar anamnese com sucesso em atendimento em andamento")
        void deveRegistrarAnamneseComSucessoEmAtendimentoEmAndamento() {
            Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            Anamnese anamnese = new Anamnese("Vômito há dois dias", "Vacinas em dia");

            Atendimento atualizado = service.registrarAnamnese(atendimento.getId(), anamnese); //adiciona no atendimento

            assertThat(atualizado.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
            assertThat(atualizado.getAnamnese()).isEqualTo(anamnese);
        }

        @ParameterizedTest
        @MethodSource("anamnesesQueUltrapassamLimite") //usei esse ao inves de csv pelo tamanho dos caracteres
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C1502 - Não deve registrar anamnese que ultrapassa o limite de caracteres permitidos")
        void naoDeveRegistrarAnamneseQueUltrapasseOLimiteDeCaracteresPermitidos(String descricao, String queixaPrincipal, String historicoClinico) {
            Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            assertThatThrownBy(() -> service.registrarAnamnese(atendimento.getId(),
                    new Anamnese(queixaPrincipal, historicoClinico)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        static Stream<Arguments> anamnesesQueUltrapassamLimite() {
            return Stream.of(
                    Arguments.of("queixa principal com 101 caracteres", "a".repeat(101), "Vacinas em dia"),
                    Arguments.of("histórico clínico com 501 caracteres", "Vômito há dois dias", "a".repeat(501))
            );
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C1503 Não deve registrar anamnese em atendimento inexistente")
        void naoDeveRegistrarAnamneseEmAtendimentoInexistente() {
            AtendimentoId idInexistente = AtendimentoId.novo();
            //mesma coisa do #C1403 Não deve registrar sinais vitais em atendimento inexistente :/

            assertThrows(AtendimentoNaoEncontradoException.class,
                    () -> service.registrarAnamnese(idInexistente, new Anamnese("Alergia", "Pelo irritado e vermelho")));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C1504 - Não deve registrar anamnese em atendimento concluido")
        void naoDeveRegistrarAnamneseEmAtendimentoConcluido() {
            Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            atendimento.registrarSinaisVitais(new SinaisVitais(20.4, 95, 17.5));
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
            atendimento.registrarAnamnese(new Anamnese("Dor de barriga", "Animal chegou com desconforto intestinal"));
            atendimento.concluir(); //fechar atendimento

            assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.CONCLUIDO);

            assertThatThrownBy(() -> atendimento.registrarAnamnese(new Anamnese("Suspeita de febre", "Animal chegou com suspeita de febre")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C1601 - Deve registrar diagnóstico com sucesso em atendimento em andamento")
        void deveRegistrarDiagnosticoComSucessoEmAtendimentoEmAndamento() {
            Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            Diagnostico diagnostico = new Diagnostico("A001", "Alergia comum", TipoDiagnostico.PRESUNTIVO);

            Atendimento atualizado = service.registrarDiagnostico(atendimento.getId(), diagnostico); //adiciona no atendimento

            assertThat(atualizado.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
            assertThat(atualizado.getDiagnosticos()).isEqualTo(List.of(diagnostico));
        }

        @ParameterizedTest
        @MethodSource("diagnosticosQueUltrapassamLimite")
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C1602 - Não deve registrar diagnostico que ultrapassa o limite de caracteres permitidos")
        void naoDeveRegistrarDiagnosticoQueUltrapasseOLimiteDeCaracteresPermitidos(String codigo, String descricao, TipoDiagnostico tipo) {
            Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            assertThatThrownBy(() -> service.registrarDiagnostico(atendimento.getId(),
                    new Diagnostico(codigo, descricao, tipo)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        static Stream<Arguments> diagnosticosQueUltrapassamLimite() {
            return Stream.of(
                    Arguments.of("a".repeat(11), "Gripe", TipoDiagnostico.PRESUNTIVO),
                    Arguments.of("A001", "a".repeat(201), TipoDiagnostico.PRESUNTIVO)
            );
        }


        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("#C1603 Não deve registrar diagnóstico em atendimento inexistente")
        void naoDeveRegistrarDiagnosticoEmAtendimentoInexistente() {
            AtendimentoId idInexistente = AtendimentoId.novo();
            //mesma coisa do #C1503 Não deve registrar anamnese em atendimento inexistente :/

            assertThrows(AtendimentoNaoEncontradoException.class,
                    () -> service.registrarDiagnostico(idInexistente, new Diagnostico("A001", "Alergia", TipoDiagnostico.PRESUNTIVO)));
        }

        @Test
        @Tag("UnitTest")
        @Tag("TDD")
        @DisplayName("C1604 - Não deve registrar diagnóstico em atendimento concluido")
        void naoDeveRegistrarDiagnosticoEmAtendimentoConcluido() {
            Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            atendimento.registrarSinaisVitais(new SinaisVitais(20.4, 95, 17.5));
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
            atendimento.concluir(); //fechar atendimento

            assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.CONCLUIDO);

            assertThatThrownBy(() -> atendimento.registrarDiagnostico(new Diagnostico("A001", "Alergia", TipoDiagnostico.PRESUNTIVO)))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Functional Tests - AVL / Partiction")
    class FunctionalTests{
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

            Assertions.assertThat(cancelado.getStatus()).isEqualTo(StatusAtendimento.CANCELADO);
            Assertions.assertThat(cancelado.getJustificativa()).isEqualTo(justificativaValida);
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
        @DisplayName("#PE04 Não devo operar sobre um AtendimentoId que não existe no repositório")
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

                Assertions.assertThat(atualizado.getAnamnese().queixaPrincipal()).hasSize(tamanho); //se a queixa tem o tamanho que eu dei
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

                Assertions.assertThat(atualizado.getAnamnese().historicoClinico()).hasSize(tamanho);
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

                Assertions.assertThat(atualizado.getDiagnosticos().getFirst().codigo()).hasSize(tamanho);

            } else {
                assertThrows(IllegalArgumentException.class,
                        () -> new Diagnostico(codigo, "descricao", TipoDiagnostico.PRESUNTIVO));
            }
        }


        // =================================================================
        // VALOR LIMITE — tamanho da descrição do diagnóstico (máx. 200)
        // =================================================================

        @ParameterizedTest(name = "descricao com {0} caracteres -> deveAceitar={1}")
        @Tag("Funcional")
        @Tag("ValorLimite")
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

                Assertions.assertThat(atualizado.getDiagnosticos().getFirst().descricao()).hasSize(tamanho);
            } else {
                assertThrows(IllegalArgumentException.class,
                        () -> new Diagnostico("A001", descricao, TipoDiagnostico.PRESUNTIVO));
            }
        }


        // =================================================================
        // PARTIÇÃO DE EQUIVALÊNCIA — tipo do diagnóstico -> Classes: PRESUNTIVO / EMPIRICO / DEFINITIVO (todas válidas)
        // =================================================================

        @ParameterizedTest(name = "tipo={0}")
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("#PE05 Devo registrar diagnóstico de qualquer tipo em atendimento em andamento")
        @EnumSource(TipoDiagnostico.class)
        void devoRegistrarDiagnosticoDeQualquerTipo(TipoDiagnostico tipo) {
            Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            Diagnostico diagnostico = new Diagnostico("A001", "Alergia comum", tipo);

            Atendimento atualizado = service.registrarDiagnostico(atendimento.getId(), diagnostico);

            Assertions.assertThat(atualizado.getDiagnosticos()).containsExactly(diagnostico);
        }


        // =================================================================
        // VALOR LIMITE — temperatura, frequência cardíaca e peso dos sinais vitais
        // =================================================================

        @ParameterizedTest(name = "temperatura={0}, frequenciaCardiaca={1}, peso={2} -> aceitam = true")
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("#VL07 Valor limite da temperatura, frequência cardíaca e peso nos sinais vitais - deve passar")
        @CsvSource({
                "0.1, 20, 20.0",    // menor valor válido pra temp
                "20.0, 1, 20.0",    // menor valor válido pra a freq card
                "20.0, 20, 0.1"     // menor valor válido pra o peso
        })
        void valorLimiteTemperaturaFrequenciaPesoSinaisVitais(double temperatura, int frequencia, double peso) {
            Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            Atendimento atualizado = service.registrarSinaisVitais(atendimento.getId(),
                    new SinaisVitais(temperatura, frequencia, peso));

            Assertions.assertThat(atualizado.getSinaisVitais().temperaturaC()).isEqualTo(temperatura);
            Assertions.assertThat(atualizado.getSinaisVitais().frequenciaCardiaca()).isEqualTo(frequencia);
            Assertions.assertThat(atualizado.getSinaisVitais().pesoKg()).isEqualTo(peso);

        }

        // =================================================================
        // PARTIÇÃO DE EQUIVALÊNCIA — Não permitir código e descrição vazios no diagnóstico
        // =================================================================

        @ParameterizedTest(name = "codigo=''{0}'', descricao=''{1}''")
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("#PE06 Não deve registrar código e/ou descrição vazios no diagnóstico")
        @CsvSource({
                "'', Alergia",
                "A001, ''",
        })
        void naoDevoCriarDiagnosicoComTextoVazio(String codigo, String descricao) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Diagnostico(codigo, descricao, TipoDiagnostico.PRESUNTIVO));
        }


        // =================================================================
        // PARTIÇÃO DE EQUIVALÊNCIA — Não permitir queixa principal e histórico clínico vazios na anamnese
        // =================================================================

        @ParameterizedTest(name = "queixaPrincipal=''{0}'', historicoClinico=''{1}''")
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("#PE07 Não deve registrar queixa principal e/ou histórico clínico vazios na anamnese")
        @CsvSource({
                "'', Paciente chegou chegando",
                "Diarréia, ''",
        })
        void naoDevoCriarAnamneseComTextoVazio(String queixa, String historico) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Anamnese(queixa, historico));
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
            Assertions.assertThat(exames.size()).isEqualTo(1);
            Assertions.assertThat(exames.contains(exameSolicitado1)).isEqualTo(true);
            Assertions.assertThat(atendimento).isEqualTo(atendimentoAtualizado);
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

        @ParameterizedTest
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("deve aceitar exame com horário dentro do limite permitido.")
        @ValueSource(strings = {
                "09:00",
                "09:01",
                "17:59",
                "18:00",
        })
        void deveAceitarExameComHorarioDentroDoLimitePermitido(String horario) {
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

            Atendimento atendimentoAtualizado = serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2));

            var exames = atendimentoAtualizado.getExames();
            Assertions.assertThat(exames.size()).isEqualTo(2);
            Assertions.assertThat(exames.contains(exameSolicitado1)).isEqualTo(true);
            Assertions.assertThat(exames.contains(exameSolicitado2)).isEqualTo(true);
            Assertions.assertThat(atendimento).isEqualTo(atendimentoAtualizado);
        }

        @Test
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("deve devolver um único exame caso o animal tenha apenas um exame registrado.")
        void deveDevolverUmUnicoExameCasoOAnimalTenhaApenasUmExameRegistrado() {
            var animalId = AnimalId.of(UUID.randomUUID());

            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );

            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(animalId);

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    9,
                    0)
            );

            serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1));

            List<ExameSolicitado> exames = repository.buscarExamesPorAnimalId(animalId);
            Assertions.assertThat(exames.size()).isEqualTo(1);
            Assertions.assertThat(exames.contains(exameSolicitado1)).isEqualTo(true);
        }

        @ParameterizedTest
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("deve devolver uma lista vazia caso não existam exames no periodo informado")
        @ValueSource(strings = {
                "2026-10-07",
                "2026-12-09",
        })
        void deveDevolverUmaListaVaziaCasoNaoExistamExamesNoPeriodoInformado(String data) {
            var animalId = AnimalId.of(UUID.randomUUID());

            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    5,
                    10,
                    0)
            );

            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(animalId);

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            LocalDate periodo = LocalDate.parse(data);

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    periodo.getYear(),
                    periodo.getMonth(),
                    periodo.getDayOfMonth(),
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    periodo.getYear(),
                    periodo.getMonth(),
                    periodo.getDayOfMonth(),
                    9,
                    0)
            );

            serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2));

            var dataInicial = LocalDate.of(2026, Month.OCTOBER, 8);
            var dataFinal = LocalDate.of(2026, Month.DECEMBER, 8);

            List<ExameSolicitado> exames = repository.buscarExamesPorAnimalIdEPeriodo(animalId, dataInicial, dataFinal);
            Assertions.assertThat(exames.isEmpty()).isEqualTo(true);
        }

        @ParameterizedTest
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("deve devolver uma lista com exames que estão dentro do periodo informado")
        @ValueSource(strings = {
                "2026-10-08",
                "2026-10-09",
                "2026-12-07",
                "2026-12-08",
        })
        void deveDevolverUmaListaComExamesQueEstaoDentroDoPeriodoInformado(String data) {
            var animalId = AnimalId.of(UUID.randomUUID());

            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    5,
                    10,
                    0)
            );

            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(animalId);

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            LocalDate periodo = LocalDate.parse(data);

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    periodo.getYear(),
                    periodo.getMonth(),
                    periodo.getDayOfMonth(),
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    periodo.getYear(),
                    periodo.getMonth(),
                    periodo.getDayOfMonth(),
                    9,
                    0)
            );

            serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2));

            var dataInicial = LocalDate.of(2026, Month.OCTOBER, 8);
            var dataFinal = LocalDate.of(2026, Month.DECEMBER, 8);

            List<ExameSolicitado> exames = repository.buscarExamesPorAnimalIdEPeriodo(animalId, dataInicial, dataFinal);
            Assertions.assertThat(exames.size()).isEqualTo(2);
            Assertions.assertThat(exames.contains(exameSolicitado1)).isEqualTo(true);
            Assertions.assertThat(exames.contains(exameSolicitado2)).isEqualTo(true);
        }

        @ParameterizedTest
        @Tag("UnitTest")
        @Tag("Functional")
        @DisplayName("deve lançar uma exceção caso o resultado informado seja nulo ou vazio")
        @MethodSource("resultados")
        void deveLancarUmaExcecaoCasoOResultadoInformadoSejaNuloOuVazio(String resultado) {
            AtendimentoService serviceLocal = new AtendimentoService(repository, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    7,
                    9,
                    1)
            );

            Atendimento atendimento = serviceLocal.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
            Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

            ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.of(
                    2026,
                    Month.OCTOBER,
                    8,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    9,
                    9,
                    0)
            );
            ExameSolicitado exameSolicitado3 = new ExameSolicitado(exame2, LocalDateTime.of(
                    2026,
                    Month.DECEMBER,
                    4,
                    9,
                    0)
            );

            serviceLocal.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2, exameSolicitado3));

            assertThatThrownBy(() -> serviceLocal.registrarResultadoExame(atendimento.getId(), exameSolicitado1.getExame().getId(), resultado)).isInstanceOf(IllegalArgumentException.class);
        }

        static Stream<String> resultados() {
            return Stream.of(
                    null,
                    "",
                    " ",
                    "    "
            );
        }
    }

    @Nested
    @DisplayName("Structural Tests")
    class StructuralTests{
        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("S0001 - deve buscar atendimento existente pelo id")
        void deveBuscarAtendimentoExistentePeloId() {
            Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            assertThat(service.buscarAtendimento(atendimento.getId())).isEqualTo(atendimento);
        }

        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("S0002 - equals e hashCode de AtendimentoId")
        void equalsEHashCodeDeAtendimentoId() {
            UUID uuid = UUID.randomUUID();
            AtendimentoId id = AtendimentoId.of(uuid);

            assertThat(id.equals(id)).isTrue();                                     // mesma instância
            assertThat(id.equals(null)).isFalse();
            assertThat(id.equals("outra classe")).isFalse();
            assertThat(id.equals(AtendimentoId.of(uuid))).isTrue();                 // mesmo valor
            assertThat(id.equals(AtendimentoId.of(UUID.randomUUID()))).isFalse();   // valor diferente
            assertThat(id.hashCode()).isEqualTo(AtendimentoId.of(uuid).hashCode());
        }

        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("S0003 - equals de AgendamentoId")
        void equalsDeAgendamentoId() {
            UUID uuid = UUID.randomUUID();
            AgendamentoId id = AgendamentoId.of(uuid);

            assertThat(id.equals(null)).isFalse();                                  // o == null
            assertThat(id.equals("outra classe")).isFalse();                        // getClass() diferente
            assertThat(id.equals(AgendamentoId.of(uuid))).isTrue();                 // mesmo valor
            assertThat(id.equals(AgendamentoId.of(UUID.randomUUID()))).isFalse();   // valor diferente
        }

        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("S0004 - equals de AnimalId")
        void equalsDeAnimalId() {
            UUID uuid = UUID.randomUUID();
            AnimalId id = AnimalId.of(uuid);

            assertThat(id.equals(null)).isFalse();                              // o == null
            assertThat(id.equals("outra classe")).isFalse();                    // getClass() diferente
            assertThat(id.equals(AnimalId.of(uuid))).isTrue();                  // mesmo valor
            assertThat(id.equals(AnimalId.of(UUID.randomUUID()))).isFalse();    // valor diferente
        }

        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("S0005 - encontra prescrição pelo id")
        void deveEncontrarPrescricaoPeloId() {
            Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            Prescricao emitida = atendimento.emitirPrescricao(
                    List.of(new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5)));

            assertThat(atendimento.encontrarPrescricao(emitida.getId())).isSameAs(emitida);
        }

        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("S0006 - equals e hashCode de Atendimento")
        void equalsEHashCodeDeAtendimento() {
            Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            Atendimento outro = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

            assertThat(atendimento.equals(null)).isFalse();                 // o == null
            assertThat(atendimento.equals("outra classe")).isFalse();       // getClass() diferente
            assertThat(atendimento.equals(atendimento)).isTrue();           // mesma classe, mesmo id
            assertThat(atendimento.equals(outro)).isFalse();                // mesma classe, id diferente
            assertThat(atendimento.hashCode()).isEqualTo(atendimento.hashCode());
        }


        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("S0007 - não emite prescrição em atendimento cancelado")
        void naoDeveEmitirPrescricaoEmAtendimentoCancelado() {
            Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            atendimento.cancelar("Tutor desistiu");

            assertThatThrownBy(() -> atendimento.emitirPrescricao(
                    List.of(new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Emissão rejeitada");
        }

        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("S0008 - método getNome de Exame")
        void metodoGetNomeDeExame() {
            String nomeExame = "Hemograma completo";

            Exame exame = new Exame(ExameId.of(UUID.randomUUID()), nomeExame);

            assertThat(exame.getNome()).isEqualTo(nomeExame);
        }

        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("S0009 - equals e hashCode de Exame")
        void equalsEHashCodeDeExame() {
            ExameId exameId =  ExameId.of(UUID.randomUUID());
            Exame exame = new Exame(exameId, "Hemograma completo");

            assertThat(exame.equals(exameId)).isEqualTo(false);
            assertThat(exame.equals(null)).isEqualTo(false);
            assertThat(exame.hashCode()).isEqualTo(exame.hashCode());
        }

        @Test
        @Tag("UnitTest")
        @Tag("Structural")
        @DisplayName("S0010 - equals e hashCode de ExameSolicitado")
        void equalsEHashCodeDeExameSolicitado() {
            Exame exame = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma completo");
            ExameSolicitado exameSolicitado = new ExameSolicitado(exame, LocalDateTime.of(2026, Month.NOVEMBER, 4, 9, 0));

            assertThat(exameSolicitado.equals(exame)).isEqualTo(false);
            assertThat(exameSolicitado.equals(null)).isEqualTo(false);
            assertThat(exameSolicitado.hashCode()).isEqualTo(exameSolicitado.hashCode());
        }
    }

    @Nested
    @DisplayName("Mutation Tests")
    class AtendimentoMutationTest {

        private Atendimento atendimento;

        @BeforeEach
        void setUp() {
            atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
        }

        private List<ItemPrescricao> criarItens() {
            return List.of(new ItemPrescricao("Dipirona", 500, "Oral", "8 em 8 horas", 5),
                    new ItemPrescricao("Novalgina", 200, "Oral", "6 em 6 horas", 5));
        }

        @Test
        @Tag("UnitTest")
        @Tag("Mutation")
        @DisplayName("M0001 EncontrarPrescricao deve retornar a prescrição com o id informado, e não a primeira da lista")
        void encontrarPrescricaoDeveRetornarPrescricaoDoIdInformado() {
            Prescricao primeira = atendimento.emitirPrescricao(criarItens());
            Prescricao segunda = atendimento.emitirPrescricao(criarItens());
            Prescricao terceira = atendimento.emitirPrescricao(criarItens());

            Prescricao encontrada = atendimento.encontrarPrescricao(segunda.getId());

            assertThat(encontrada).isSameAs(segunda);
            assertThat(encontrada).isNotSameAs(primeira);
            assertThat(encontrada).isNotSameAs(terceira);
        }

        @Test
        @Tag("UnitTest")
        @Tag("Mutation")
        @DisplayName("M0002 EncontrarPrescricao deve lançar exceção quando o id não pertence a nenhuma prescrição")
        void encontrarPrescricaoDeveLancarExcecaoQuandoIdNaoExiste() {
            atendimento.emitirPrescricao(criarItens());

            Atendimento outro = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            PrescricaoId idDeOutroAtendimento = outro.emitirPrescricao(criarItens()).getId();

            assertThatThrownBy(() -> atendimento.encontrarPrescricao(idDeOutroAtendimento))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Prescrição não encontrada");
        }

        @Test
        @Tag("UnitTest")
        @Tag("Mutation")
        @DisplayName("M0003 HashCode deve ser derivado do id do atendimento")
        void hashCodeDeveDependerDoId() {
            AtendimentoId id = AtendimentoId.novo();
            AnimalId animalId = AnimalId.of(UUID.randomUUID());

            Atendimento a1 = Atendimento.reconstituir(id, animalId, null, StatusAtendimento.EM_ANDAMENTO,
                    LocalDateTime.now(), null, null, null, null, List.of(), List.of(), List.of());
            Atendimento a2 = Atendimento.reconstituir(id, animalId, null, StatusAtendimento.EM_ANDAMENTO,
                    LocalDateTime.now(), null, null, null, null, List.of(), List.of(), List.of());
            Atendimento outro = Atendimento.reconstituir(AtendimentoId.novo(), animalId, null,
                    StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now(), null, null, null, null,
                    List.of(), List.of(), List.of());

            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
            assertThat(a1.hashCode()).isEqualTo(Objects.hashCode(id));
            assertThat(a1.hashCode()).isNotEqualTo(outro.hashCode());
        }

        @Test
        @Tag("UnitTest")
        @Tag("Mutation")
        @DisplayName("M0004 HashCode de AnimalId deve ser derivado do UUID")
        void hashCodeDeAnimalIdDevDependerDoUuid() {
            UUID uuid = UUID.randomUUID();
            AnimalId a = AnimalId.of(uuid);
            AnimalId b = AnimalId.of(uuid);
            AnimalId outro = AnimalId.of(UUID.randomUUID());

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
            assertThat(a.hashCode()).isEqualTo(Objects.hashCode(uuid));
            assertThat(a.hashCode()).isNotEqualTo(outro.hashCode());
        }

        @Test
        @Tag("UnitTest")
        @Tag("Mutation")
        @DisplayName("M0005 HashCode de AgendamentoId deve ser derivado do UUID")
        void hashCodeDeAgendamentoIdDeveDependerDoUuid() {
            UUID uuid = UUID.randomUUID();
            AgendamentoId a = AgendamentoId.of(uuid);
            AgendamentoId b = AgendamentoId.of(uuid);
            AgendamentoId outro = AgendamentoId.of(UUID.randomUUID());

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
            assertThat(a.hashCode()).isEqualTo(Objects.hashCode(uuid));
            assertThat(a.hashCode()).isNotEqualTo(outro.hashCode());
        }
    }
    @Nested
    class AtendimentoServiceMutationTest {

        private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 7, 9, 1);

        private static class RepositorioContador extends InMemoryAtendimentoRepository {
            private int salvamentos = 0;

            @Override
            public void salvar(Atendimento atendimento) {
                salvamentos++;
                super.salvar(atendimento);
            }

            int salvamentos() {
                return salvamentos;
            }
        }

        private static ExameSolicitado exame(String nome, int hora) {
            return new ExameSolicitado(
                    new Exame(ExameId.of(UUID.randomUUID()), nome),
                    LocalDateTime.of(2026, 10, 8, hora, 0));
        }

        @Test
        @Tag("UnitTest")
        @Tag("Mutation")
        @DisplayName("M0006 cancelarAtendimento deve salvar o atendimento")
        void cancelarAtendimentoDeveSalvar() {
            RepositorioContador repo = new RepositorioContador();
            AtendimentoService svc = new AtendimentoService(repo);
            Atendimento atendimento = svc.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            int antes = repo.salvamentos();

            svc.cancelarAtendimento(atendimento.getId(), "Tutor desistiu");

            assertThat(repo.salvamentos()).isEqualTo(antes + 1);
        }

        @Test
        @Tag("UnitTest")
        @Tag("Mutation")
        @DisplayName("M0007 concluirAtendimento deve salvar o atendimento")
        void concluirAtendimentoDeveSalvar() {
            RepositorioContador repo = new RepositorioContador();
            AtendimentoService svc = new AtendimentoService(repo);
            Atendimento atendimento = svc.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            atendimento.registrarSinaisVitais(new SinaisVitais(38.5, 100, 12.0));
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
            int antes = repo.salvamentos();

            svc.concluirAtendimento(atendimento.getId());

            assertThat(repo.salvamentos()).isEqualTo(antes + 1);
        }

        @Test
        @Tag("UnitTest")
        @Tag("Mutation")
        @DisplayName("M0008 reabrirAtendimento deve salvar o atendimento")
        void reabrirAtendimentoDeveSalvar() {
            RepositorioContador repo = new RepositorioContador();
            AtendimentoService svc = new AtendimentoService(repo);
            Atendimento atendimento = svc.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
            atendimento.registrarSinaisVitais(new SinaisVitais(38.5, 100, 12.0));
            atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
            svc.concluirAtendimento(atendimento.getId());
            int antes = repo.salvamentos();

            svc.reabrirAtendimento(atendimento.getId(), LocalDateTime.now());

            assertThat(repo.salvamentos()).isEqualTo(antes + 1);
        }
    }
}

