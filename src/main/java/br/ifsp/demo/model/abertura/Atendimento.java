package br.ifsp.demo.model.abertura;

import java.time.LocalDateTime;

public class Atendimento {
    private final AtendimentoId id;
    private final AnimalId animalId;
    private final AgendamentoId agendamentoId;
    private StatusAtendimento status;
    private final LocalDateTime dataHoraAtendimento;
    private String justificativa;

    public Atendimento(LocalDateTime dataHoraAtendimento, StatusAtendimento status, AgendamentoId agendamentoId, AnimalId animalId, AtendimentoId id) {
        this.dataHoraAtendimento = dataHoraAtendimento;
        this.status = status;
        this.agendamentoId = agendamentoId;
        this.animalId = animalId;
        this.id = id;
    }

    public void cancelar(String justificativa){
        this.status = StatusAtendimento.CANCELADO;
        this.justificativa = justificativa;
    }

    public static Atendimento abrirProntoAtendimento(AnimalId animalId){
        return new Atendimento(
                LocalDateTime.now(),
                StatusAtendimento.EM_ANDAMENTO,
                null,
                animalId,
                AtendimentoId.novo()
        );
    }

    public String getJustificativa() {
        return justificativa;
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
