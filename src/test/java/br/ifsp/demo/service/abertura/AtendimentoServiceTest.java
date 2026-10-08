package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.*;
import br.ifsp.demo.model.abertura.*;
import br.ifsp.demo.model.clinico.Anamnese;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.model.clinico.TipoDiagnostico;
import br.ifsp.demo.model.prescricao.ItemPrescricao;
import br.ifsp.demo.model.prescricao.Prescricao;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
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
    void prepara() {
        repository = new InMemoryAtendimentoRepository();
        service = new AtendimentoService(repository);
    }

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

}

