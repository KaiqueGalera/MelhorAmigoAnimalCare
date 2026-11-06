package br.ifsp.demo.model.abertura;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AtendimentoTest {

    private static final AgendamentoId AGENDAMENTO_ID = AgendamentoId.of(UUID.randomUUID());
    private static final AnimalId ANIMAL_ID = AnimalId.of(UUID.randomUUID());

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("getDataHoraConclusao deve ser nula enquanto o atendimento nao foi encerrado")
    void getDataHoraConclusaoDeveSerNulaEnquantoOAtendimentoNaoFoiEncerrado() {
        Atendimento atendimento = new Atendimento(AGENDAMENTO_ID, ANIMAL_ID);
        assertThat(atendimento.getDataHoraConclusao()).isNull();
    }

    @Test
    @Tag("UnitTest")
    @Tag("Structural")
    @DisplayName("getDataHoraConclusao deve ser preenchida ao cancelar atendimento")
    void getDataHoraConclusaoDeveSerPreenchidaAoCancelarAtendimento() {
        Atendimento atendimento = new Atendimento(AGENDAMENTO_ID, ANIMAL_ID);

        LocalDateTime antes = LocalDateTime.now();
        atendimento.cancelar("Tutor desistiu do atendimento");
        LocalDateTime depois = LocalDateTime.now();

        assertThat(atendimento.getDataHoraConclusao()).isBetween(antes, depois);
    }


}
