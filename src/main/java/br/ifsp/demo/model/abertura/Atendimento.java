package br.ifsp.demo.model.abertura;

import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Atendimento {
    private static final Duration JANELA_REABERTURA = Duration.ofHours(24);
    private final AtendimentoId id;
    private final AnimalId animalId;
    private final AgendamentoId agendamentoId;
    private StatusAtendimento status;
    private final LocalDateTime dataHoraAtendimento;
    private String justificativa;
    private SinaisVitais sinaisVitais;
    private final List<Diagnostico> diagnosticos = new ArrayList<>();
    private LocalDateTime dataHoraConclusao;

    public Atendimento(AgendamentoId agendamentoId, AnimalId animalId) {
        this.dataHoraAtendimento = LocalDateTime.now();
        this.status = StatusAtendimento.EM_ANDAMENTO;
        this.agendamentoId = agendamentoId;
        this.animalId = animalId;
        this.id = AtendimentoId.novo();
    }

    public void cancelar(String justificativa){
        if (status == StatusAtendimento.CANCELADO) {
            throw new IllegalStateException("Atendimento cancelado não aceita alterações");
        }
        if (justificativa == null) {
            throw new IllegalArgumentException("Justificativa é obrigatória para cancelar o atendimento");
        }

        this.status = StatusAtendimento.CANCELADO;
        this.justificativa = justificativa;
        this.dataHoraConclusao = LocalDateTime.now();
    }

    public void concluir(){
        if (sinaisVitais == null) {
            throw new IllegalStateException("O atendimento deve possuir registro de sinais vitais para sua conclusao");
        }
        if (diagnosticos.isEmpty()) {
            throw new IllegalStateException("O atendimento devo possui ao menos um diagnóstico para sua conclusão");
        }
        this.status = StatusAtendimento.CONCLUIDO;
        this.dataHoraConclusao = LocalDateTime.now();
    }

    public void reabrir(LocalDateTime agora) {
        Duration decorrido = Duration.between(dataHoraConclusao, agora);
        if (decorrido.compareTo(JANELA_REABERTURA) > 0) {
            throw new IllegalStateException("Janela de tempo para reabertura expirou");
        }

        this.status = StatusAtendimento.EM_ANDAMENTO;
    }

    public void registrarSinaisVitais(SinaisVitais sinaisVitais) {
        this.sinaisVitais = sinaisVitais;
    }

    public void registrarDiagnostico(Diagnostico diagnostico) {
        diagnosticos.add(diagnostico);
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
