package br.ifsp.demo.repository;

import br.ifsp.demo.exception.AtendimentoNaoEncontradoException;
import br.ifsp.demo.model.abertura.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAtendimentoRepository implements AtendimentoRepository {
    private final Map<AtendimentoId, Atendimento> atendimentos = new ConcurrentHashMap<>();

    @Override
    public void salvar(Atendimento atendimento) {
        atendimentos.put(atendimento.getId(), atendimento);
    }

    @Override
    public boolean existeEmAndamentoParaAnimal(AnimalId animalId) {
        return buscarPorAnimalId(animalId).stream()
                .anyMatch(a -> a.getStatus().equals(StatusAtendimento.EM_ANDAMENTO));
    }

    @Override
    public List<Atendimento> buscarPorAnimalId(AnimalId animalId) {
        return atendimentos.values().stream()
                .filter(a -> a.getAnimalId().equals(animalId))
                .toList();
    }

    @Override
    public Atendimento buscarUmPorAtendimentoId(AtendimentoId id) {
        Atendimento atendimento = atendimentos.get(id);
        if (atendimento == null) {
            throw new AtendimentoNaoEncontradoException(id);
        }
        return atendimento;
    }

    @Override
    public boolean existeAtendimentoParaAgendamento(AgendamentoId agendamentoId) {
        return atendimentos.values().stream()
                .anyMatch(a -> agendamentoId.equals(a.getAgendamentoId()));
    }
}