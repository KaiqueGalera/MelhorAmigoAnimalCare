package br.ifsp.demo.service.abertura;

import br.ifsp.demo.exception.AnimalJaEmAtendimentoException;
import br.ifsp.demo.model.abertura.AgendamentoId;
import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AtendimentoId;
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
}
