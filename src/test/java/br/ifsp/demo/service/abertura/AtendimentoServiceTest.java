package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.*;
import br.ifsp.demo.model.abertura.*;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.model.clinico.TipoDiagnostico;
import br.ifsp.demo.model.exame.Exame;
import br.ifsp.demo.model.exame.ExameId;
import br.ifsp.demo.model.exame.ExameSolicitado;
import br.ifsp.demo.model.prescricao.ItemPrescricao;
import br.ifsp.demo.model.prescricao.Prescricao;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtendimentoServiceTest {
    private InMemoryAtendimentoRepository repository;
    private AtendimentoService service;

    @BeforeEach()
    void prepara(){
        repository = new InMemoryAtendimentoRepository();
        service = new AtendimentoService(repository);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("# C0101 Devo abrir pronto atendimento para animal sem agendamento prévio")
    void devoAbrirProntoAtendimentoComSucesso(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);

        assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
        assertThat(atendimento.getAgendamentoId()).isNull();
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("# C0102 Não devo abrir pronto atendimento quando animal já estiver em atendimento")
    void naoDevoAbrirProntoAtendimento(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        service.abrirProntoAtendimento(animalId);

        assertThrows(AnimalJaEmAtendimentoException.class, ()-> service.abrirProntoAtendimento(animalId));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("# C0103 Cancelar atendimento com justificativa com sucesso")
    void devoCancelarProntoAtendimentoComSucesso(){
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
    void devoAbrirAtendimentoComAgendamento(){
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
    void naoDevoAbrirAtendimentoComAgendamento(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        AgendamentoId agendamentoId = AgendamentoId.of(UUID.randomUUID());
        service.abrirProntoAtendimento(animalId);

        assertThrows(AnimalJaEmAtendimentoException.class, ()-> service.abrirAtendimentoComAgendamento(animalId, agendamentoId));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("# C0203-A Devo rejeitar associar agendamento com atendimento caso já tenha atendimento")
    void devoRejeitarAgendamentoJaUtilizado(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        AgendamentoId agendamentoId = AgendamentoId.of(UUID.randomUUID());
        service.abrirAtendimentoComAgendamento(animalId, agendamentoId);
        AnimalId outroAnimal = AnimalId.of(UUID.randomUUID());

        assertThrows(AgendamentoJaUtilizadoException.class, ()-> service.abrirAtendimentoComAgendamento(outroAnimal, agendamentoId));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("#C0302 Não devo cancelar atendimento sem informar justificativa")
    void naoDevoCancelarAtendimentoSemJustificativa(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);

        assertThrows(JustificativaObrigatoriaException.class,
                () -> service.cancelarAtendimento(atendimento.getId(), null));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("#C0303 Não devo cancelar atendimento quando já cancelado")
    void naoDevoCancelarAtendimentoJaCancelado(){
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
    void devoEncerrarAtendimento(){
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
    void naoDevoEncerrarAtendimentoSemSinaisVitais(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));

        assertThrows(SinaisVitaisObrigatoriosException.class, ()-> service.concluirAtendimento(atendimento.getId()));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("#C0403 Não devo encerrar atendimento sem diagnóstico registrados")
    void naoDevoEncerrarAtendimentoSemDiagnostico(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarSinaisVitais(new SinaisVitais(30.3, 100, 89));

        assertThrows(DiagnosticoObrigatorioException.class, ()-> service.concluirAtendimento(atendimento.getId()));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("#C0501 Devo reabrir atendimento concluído dentro da janela de tempo")
    void devoReabrirAtendimentoDentroDaJanela(){
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
    void naoDevoReabrirAtendimentoForaDaJanela(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarSinaisVitais(new SinaisVitais(30.6, 100, 89));
        atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
        service.concluirAtendimento(atendimento.getId());

        assertThrows(JanelaReaberturaExpiradaException.class, ()-> service.reabrirAtendimento(atendimento.getId(), LocalDateTime.now().plusHours(25)));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("#C0503 Não devo reabrir atendimento cancelado")
    void naoDevoReabrirAtendimentoCancelado(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarSinaisVitais(new SinaisVitais(30.5, 100, 89));
        atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
        service.cancelarAtendimento(atendimento.getId(), "Alguma justificativa convincente");

        assertThrows(AtendimentoNaoConcluidoException.class, ()-> service.reabrirAtendimento(atendimento.getId(), LocalDateTime.now()));
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
        Atendimento atendimento = service.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));

        Exame exame1 = new Exame(ExameId.of(UUID.randomUUID()), "Hemograma");
        Exame exame2 = new Exame(ExameId.of(UUID.randomUUID()), "Glicemia");

        ExameSolicitado exameSolicitado1 = new ExameSolicitado(exame1, LocalDateTime.now().plusHours(1));
        ExameSolicitado exameSolicitado2 = new ExameSolicitado(exame2, LocalDateTime.now().plusHours(1));

        Atendimento atendimentoAtualizado = service.solicitarExames(atendimento.getId(), List.of(exameSolicitado1, exameSolicitado2));

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
}
