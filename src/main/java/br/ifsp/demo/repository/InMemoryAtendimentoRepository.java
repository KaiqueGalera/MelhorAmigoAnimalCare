package br.ifsp.demo.repository;

import br.ifsp.demo.exception.AtendimentoNaoEncontradoException;
import br.ifsp.demo.model.abertura.*;
import br.ifsp.demo.model.exame.ExameSolicitado;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryAtendimentoRepository {
    private final List<Atendimento> atendimentos = new ArrayList<>();

    public void salvar(Atendimento atendimento){
        atendimentos.add(atendimento);
    }

    public boolean existeEmAndamentoParaAnimal(AnimalId animalId) {
        return buscarPorAnimalId(animalId).stream()
                .anyMatch(atendimento -> atendimento.getStatus().equals(StatusAtendimento.EM_ANDAMENTO));
    }

    public List<Atendimento> buscarPorAnimalId(AnimalId animalId){
        return atendimentos.stream()
                .filter(atendimento -> atendimento.getAnimalId().equals(animalId))
                .collect(Collectors.toList());
    }

    public Atendimento buscarUmPorAtendimentoId(AtendimentoId id){
        return atendimentos.stream()
                .filter(atendimento -> atendimento.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AtendimentoNaoEncontradoException(id));
    }

    public boolean existeAtendimentoParaAgendamento(AgendamentoId agendamentoId) {
        return atendimentos.stream()
                .anyMatch(a -> agendamentoId.equals(a.getAgendamentoId()));
    }

    public List<ExameSolicitado> buscarExamesPorAnimalId(AnimalId animalId) {
        return atendimentos.stream().filter(atendimento -> atendimento.getAnimalId().equals(animalId))
                .flatMap(atendimento -> atendimento.getExames().stream())
                .toList();
    }
}
