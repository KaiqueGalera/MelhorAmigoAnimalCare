package br.ifsp.demo.model.abertura;

import java.time.LocalDateTime;

public class Atendimento {
    private final AtendimentoId id;
    private final AnimalId animalId;
    private final AgendamentoId agendamentoId;
    private StatusAtendimento status;
    private final LocalDateTime dataHoraAtendimento;

    public Atendimento(AtendimentoId id, AnimalId animalId, AgendamentoId agendamentoId, LocalDateTime dataHoraAtendimento) {
        this.id = id;
        this.animalId = animalId;
        this.agendamentoId = agendamentoId;
        this.dataHoraAtendimento = dataHoraAtendimento;
    }

    public static Atendimento abrirProntoAtendimento(){
        return new Atendimento(
                AtendimentoId.novo(),
                animalId,
                null,
                StatusAtendimento.EM_ANDAMENTO,
                LocalDateTime.now()
        );
    }

    public AtendimentoId getId() {
        return id;
    }

    public AnimalId getAnimalId() {
        return animalId;
    }

    public AgendamentoId getAgendamentoId() {
        return agendamentoId;
    }

    public StatusAtendimento getStatus() {
        return status;
    }

    public LocalDateTime getDataHoraAtendimento() {
        return dataHoraAtendimento;
    }
}
