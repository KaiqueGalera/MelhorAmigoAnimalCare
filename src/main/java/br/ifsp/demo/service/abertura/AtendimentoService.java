package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.AgendamentoJaUtilizadoException;
import br.ifsp.demo.exception.AnimalJaEmAtendimentoException;
import br.ifsp.demo.exception.ExameNaoEncontradoException;
import br.ifsp.demo.model.abertura.*;
import br.ifsp.demo.model.exame.ExameId;
import br.ifsp.demo.model.exame.ExameSolicitado;
import br.ifsp.demo.model.clinico.Anamnese;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.repository.AtendimentoRepository;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Supplier;

public class AtendimentoService {
    private final LocalTime INICIO_HORARIO_EXAME = LocalTime.of(9, 0);
    private final LocalTime FIM_HORARIO_EXAME = LocalTime.of(18, 0);

    private final AtendimentoRepository repository;
    private final Supplier<LocalDateTime> relogio;

    // Construtor usado pelo Spring: o "agora" é lido a cada chamada.
    @Autowired
    public AtendimentoService(AtendimentoRepository repository) {
        this.repository = repository;
        this.relogio = LocalDateTime::now;
    }

    // Construtor usado nos testes para fixar o "agora".
    AtendimentoService(AtendimentoRepository repository, LocalDateTime date) {
        this.repository = repository;
        this.relogio = () -> date;
    }

    public Atendimento buscarAtendimento(AtendimentoId id) {
        return repository.buscarUmPorAtendimentoId(id);
    }

    public Atendimento abrirProntoAtendimento(AnimalId animalId){
        if (repository.existeEmAndamentoParaAnimal(animalId)){
            throw new AnimalJaEmAtendimentoException(animalId);
        }

        Atendimento atendimento = Atendimento.abrirProntoAtendimento(animalId);
        repository.salvar(atendimento);

        return atendimento;
    }

    public Atendimento abrirAtendimentoComAgendamento(AnimalId animalId, AgendamentoId agendamentoId){
        if (repository.existeEmAndamentoParaAnimal(animalId)){
            throw new AnimalJaEmAtendimentoException(animalId);
        }

        if (repository.existeAtendimentoParaAgendamento(agendamentoId)) {
            throw new AgendamentoJaUtilizadoException(agendamentoId);
        }

        Atendimento atendimento = new Atendimento(agendamentoId, animalId);
        repository.salvar(atendimento);

        return atendimento;
    }

    public Atendimento cancelarAtendimento(AtendimentoId id, String justificativa){
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(id);
        atendimento.cancelar(justificativa);
        repository.salvar(atendimento);

        return atendimento;
    }

    public Atendimento concluirAtendimento(AtendimentoId atendimentoId){
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(atendimentoId);
        atendimento.concluir();
        repository.salvar(atendimento);

        return atendimento;
    }

    public Atendimento reabrirAtendimento(AtendimentoId id, LocalDateTime agora) {
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(id);
        atendimento.reabrir(agora);
        repository.salvar(atendimento);
        return atendimento;
    }

    public Atendimento solicitarExames(AtendimentoId atendimentoId, List<ExameSolicitado> exames) {
        if (exames.isEmpty())
            throw new IllegalArgumentException("Solicitação de exame rejeitada: pelo menos um exame deve ser selecionado para prosseguir.");

        Atendimento atendimento = repository.buscarUmPorAtendimentoId(atendimentoId);
        LocalDateTime agora = relogio.get();

        if (exames.stream().anyMatch(exame -> exame.getData().isBefore(agora)))
            throw new IllegalStateException("Solicitação de exame rejeitada: todos os exames devem estar vinculados a uma data e horário válidos.");

        if (exames.stream().anyMatch(exame -> exame.getData().toLocalTime().isBefore(INICIO_HORARIO_EXAME) || exame.getData().toLocalTime().isAfter(FIM_HORARIO_EXAME)))
            throw new IllegalStateException("Solicitação de exame rejeitada: todos os exames devem estar vinculados a um horário válido.");

        exames.forEach(atendimento::addExame);

        return atendimento;
    }

    public ExameSolicitado registrarResultadoExame(AtendimentoId atendimentoId, ExameId exameId, String resultado) {
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(atendimentoId);

        var exame = atendimento.getExames().stream().filter(e -> e.getExame()
                        .getId()
                        .equals(exameId))
                .findFirst().orElseThrow(() -> new ExameNaoEncontradoException("Exame não encontrado!")
        );

        exame.setResultado(resultado);

        return exame;
    }

    public Atendimento registrarSinaisVitais(AtendimentoId id, SinaisVitais sinais){
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(id);
        atendimento.registrarSinaisVitais(sinais);
        repository.salvar(atendimento);
        return atendimento;
    }

    public Atendimento registrarAnamnese(AtendimentoId id, Anamnese anamnese){
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(id);
        atendimento.registrarAnamnese(anamnese);
        repository.salvar(atendimento);
        return atendimento;

    }

    public Atendimento registrarDiagnostico(AtendimentoId id, Diagnostico diagnostico){
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(id);
        atendimento.registrarDiagnostico(diagnostico);
        repository.salvar(atendimento);
        return atendimento;
    }
}
