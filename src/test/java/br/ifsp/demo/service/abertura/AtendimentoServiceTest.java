package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.*;
import br.ifsp.demo.model.abertura.*;
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
}
