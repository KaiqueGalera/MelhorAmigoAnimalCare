package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.AgendamentoJaComAtendimento;
import br.ifsp.demo.exception.AnimalJaEmAtendimentoException;
import br.ifsp.demo.model.abertura.*;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;

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
            throw new AgendamentoJaComAtendimento(agendamentoId);
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
}
