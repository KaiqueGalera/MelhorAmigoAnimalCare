package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.AgendamentoJaUtilizadoException;
import br.ifsp.demo.exception.AnimalJaEmAtendimentoException;
import br.ifsp.demo.model.abertura.*;
import br.ifsp.demo.model.clinico.Anamnese;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;

import java.time.LocalDateTime;

public class AtendimentoService {
    private final InMemoryAtendimentoRepository repository;

    public AtendimentoService(InMemoryAtendimentoRepository repository) {
        this.repository = repository;
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
