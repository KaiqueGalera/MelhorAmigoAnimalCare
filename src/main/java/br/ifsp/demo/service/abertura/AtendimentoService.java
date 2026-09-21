package br.ifsp.demo.service.abertura;

import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;

public class AtendimentoService {
    private final InMemoryAtendimentoRepository repository;

    public AtendimentoService(InMemoryAtendimentoRepository repository) {
        this.repository = repository;
    }

    public Atendimento abrirProntoAtendimento(AnimalId animalId){
        if (repository.existeEmAndamentoParaAnimal(animalId)){
            throw new IllegalStateException("Animal já está em atendimento!");
        }

        Atendimento atendimento = Atendimento.abrirProntoAtendimento(animalId);
        repository.salvar(atendimento);

        return atendimento;
    }
}
