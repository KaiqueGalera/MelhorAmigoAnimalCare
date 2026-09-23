package br.ifsp.demo.model.abertura;

import java.time.LocalDateTime;
import java.util.UUID;

public class Atendimento {
    private final AtendimentoId id;
    private final AnimalId animalId;
    private final AgendamentoId agendamentoId;
    private StatusAtendimento status;
    private final LocalDateTime dataHoraAtendimento;
    private String justificativa;

    public Atendimento(AgendamentoId agendamentoId, AnimalId animalId) {
        this.dataHoraAtendimento = LocalDateTime.now();
        this.status = StatusAtendimento.EM_ANDAMENTO;
        this.agendamentoId = agendamentoId;
        this.animalId = animalId;
        this.id = AtendimentoId.novo();
    }

    public void cancelar(String justificativa){
        this.status = StatusAtendimento.CANCELADO;
        this.justificativa = justificativa;
    }

    public static Atendimento abrirProntoAtendimento(AnimalId animalId){
        return new Atendimento(null, animalId);
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
