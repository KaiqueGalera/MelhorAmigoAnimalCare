package br.ifsp.demo.model.abertura;

import br.ifsp.demo.exception.*;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.model.exame.Exame;
import br.ifsp.demo.model.prescricao.ItemPrescricao;
import br.ifsp.demo.model.prescricao.Prescricao;
import br.ifsp.demo.model.prescricao.PrescricaoId;
import br.ifsp.demo.model.prescricao.StatusPrescricao;
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
    private final List<Prescricao> prescricoes = new ArrayList<>();
    private final List<Exame> exames = new ArrayList<>();

    public Atendimento(AgendamentoId agendamentoId, AnimalId animalId) {
        this.dataHoraAtendimento = LocalDateTime.now();
        this.status = StatusAtendimento.EM_ANDAMENTO;
        this.agendamentoId = agendamentoId;
        this.animalId = animalId;
        this.id = AtendimentoId.novo();
    }

    public void cancelar(String justificativa){
        if (status == StatusAtendimento.CANCELADO) {
            throw new AtendimentoCanceladoExcepiton("Atendimento cancelado não aceita alterações");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new JustificativaObrigatoriaException("Justificativa é obrigatória para cancelar o atendimento");
        }

        this.status = StatusAtendimento.CANCELADO;
        this.justificativa = justificativa;
        this.dataHoraConclusao = LocalDateTime.now();
    }

    public void concluir(){
        if (sinaisVitais == null) {
            throw new SinaisVitaisObrigatoriosException("O atendimento deve possuir registro de sinais vitais para sua conclusao");
        }
        if (diagnosticos.isEmpty()) {
            throw new DiagnosticoObrigatorioException("O atendimento devo possui ao menos um diagnóstico para sua conclusão");
        }

        boolean possuiPrescricaoAberta = prescricoes.stream()
                .anyMatch(p -> p.getStatus() == StatusPrescricao.ABERTA);

        if (possuiPrescricaoAberta) {
            throw new IllegalStateException(
                    "Finalização de atendimento rejeitada, não é possível a finalização de atendimentos com prescrições abertas.");
        }

        this.status = StatusAtendimento.CONCLUIDO;
        this.dataHoraConclusao = LocalDateTime.now();
    }

    public void reabrir(LocalDateTime agora) {
        Duration decorrido = Duration.between(dataHoraConclusao, agora);
        if (status != StatusAtendimento.CONCLUIDO) {
            throw new AtendimentoNaoConcluidoException("Somente atendimentos concluídos podem ser reabertos");
        }
        if (decorrido.isNegative()) {
            throw new InstanteReaberturaInvalidoException(id);
        }
        if (decorrido.compareTo(JANELA_REABERTURA) > 0) {
            throw new JanelaReaberturaExpiradaException("Janela de tempo para reabertura expirou");
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

    public Prescricao emitirPrescricao(List<ItemPrescricao> itens) {
        if (status != StatusAtendimento.EM_ANDAMENTO) {
            throw new IllegalStateException("Emissão rejeitada: só é possível emitir uma prescrição durante um atendimento em andamento.");
        }
        Prescricao prescricao = new Prescricao(itens);
        prescricoes.add(prescricao);
        return prescricao;
    }

    public Prescricao encontrarPrescricao(PrescricaoId prescricaoId) {
        return prescricoes.stream()
                .filter(p -> p.getId().equals(prescricaoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Prescrição não encontrada: " + prescricaoId));
    }

    public void addExame(Exame exame) {
        this.exames.add(exame);
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

    public List<Prescricao> getPrescricoes() { return prescricoes; }

    public List<Exame> getExames() {
        return exames;
    }
}
