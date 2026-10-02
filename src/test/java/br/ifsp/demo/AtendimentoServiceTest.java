package br.ifsp.demo;

import br.ifsp.demo.exception.AgendamentoJaComAtendimento;
import br.ifsp.demo.exception.AnimalJaEmAtendimentoException;
import br.ifsp.demo.model.abertura.*;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.model.clinico.TipoDiagnostico;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import br.ifsp.demo.service.abertura.AtendimentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @DisplayName("# C0101 Devo abrir pronto atendimento para animal sem agendamento prévio")
    void devoAbrirProntoAtendimentoComSucesso(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);

        assertThat(atendimento.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
        assertThat(atendimento.getAgendamentoId()).isNull();
    }

    @Test
    @DisplayName("# C0102 Não devo abrir pronto atendimento quando animal já estiver em atendimento")
    void naoDevoAbrirProntoAtendimento(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        service.abrirProntoAtendimento(animalId);

        assertThrows(AnimalJaEmAtendimentoException.class, ()-> service.abrirProntoAtendimento(animalId));
    }

    @Test
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
    @DisplayName("# C0202 Não devo abrir atendimento agendado dado animal em atendimento")
    void naoDevoAbrirAtendimentoComAgendamento(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        AgendamentoId agendamentoId = AgendamentoId.of(UUID.randomUUID());
        service.abrirProntoAtendimento(animalId);

        assertThrows(AnimalJaEmAtendimentoException.class, ()-> service.abrirAtendimentoComAgendamento(animalId, agendamentoId));
    }

    @Test
    @DisplayName("# C0203-A Devo rejeitar associar agendamento com atendimento caso já tenha atendimento")
    void devoRejeitarAgendamentoJaUtilizado(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        AgendamentoId agendamentoId = AgendamentoId.of(UUID.randomUUID());
        service.abrirAtendimentoComAgendamento(animalId, agendamentoId);
        AnimalId outroAnimal = AnimalId.of(UUID.randomUUID());

        assertThrows(AgendamentoJaComAtendimento.class, ()-> service.abrirAtendimentoComAgendamento(outroAnimal, agendamentoId));
    }

    @Test
    @DisplayName("#C0302 Não devo cancelar atendimento sem informar justificativa")
    void naoDevoCancelarAtendimentoSemJustificativa(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);

        assertThrows(IllegalArgumentException.class,
                () -> service.cancelarAtendimento(atendimento.getId(), null));
    }

    @Test
    @DisplayName("#C0303 Não devo cancelar atendimento quando já cancelado")
    void naoDevoCancelarAtendimentoJaCancelado(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        service.cancelarAtendimento(atendimento.getId(), "Motivo para cancelar 1");

        assertThrows(IllegalStateException  .class,
                () -> service.cancelarAtendimento(atendimento.getId(), "Outro motivo"));
    }

    @Test
    @DisplayName("#C0401 Devo encerrar atendimento")
    void devoEncerrarAtendimento(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarSinaisVitais(new SinaisVitais(30, 100, 89));
        atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
        Atendimento concluido = service.concluirAtendimento(atendimento.getId());

        assertThat(concluido.getStatus()).isEqualTo(StatusAtendimento.CONCLUIDO);
    }

    @Test
    @DisplayName("#C0402 Não devo encerrar atendimento sem sinais vitais registrados")
    void naoDevoEncerrarAtendimentoSemSinaisVitais(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));

        assertThrows(IllegalStateException.class, ()-> service.concluirAtendimento(atendimento.getId()));
    }

    @Test
    @DisplayName("#C0403 Não devo encerrar atendimento sem diagnóstico registrados")
    void naoDevoEncerrarAtendimentoSemDiagnostico(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarSinaisVitais(new SinaisVitais(30, 100, 89));

        assertThrows(IllegalStateException.class, ()-> service.concluirAtendimento(atendimento.getId()));
    }

    @Test
    @DisplayName("#C0501 Devo reabrir atendimento concluído dentro da janela de tempo")
    void devoReabrirAtendimentoDentroDaJanela(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarSinaisVitais(new SinaisVitais(30, 100, 89));
        atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
        service.concluirAtendimento(atendimento.getId());
        Atendimento reaberto = service.reabrirAtendimento(atendimento.getId(), LocalDateTime.now());

        assertThat(reaberto.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("#C0502 Não devo reabrir atendimento concluído fora da janela de tempo")
    void naoDevoReabrirAtendimentoForaDaJanela(){
        AnimalId animalId = AnimalId.of(UUID.randomUUID());
        Atendimento atendimento = service.abrirProntoAtendimento(animalId);
        atendimento.registrarSinaisVitais(new SinaisVitais(30, 100, 89));
        atendimento.registrarDiagnostico(new Diagnostico("D001", "Diarréia", TipoDiagnostico.EMPIRICO));
        service.concluirAtendimento(atendimento.getId());

        assertThrows(IllegalStateException.class, ()-> service.reabrirAtendimento(atendimento.getId(), LocalDateTime.now().plusHours(25)));
    }
}
