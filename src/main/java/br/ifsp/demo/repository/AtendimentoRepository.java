package br.ifsp.demo.repository;

import br.ifsp.demo.model.abertura.AgendamentoId;
import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AtendimentoId;

import java.util.List;

public interface AtendimentoRepository {
    void salvar(Atendimento atendimento);
    boolean existeEmAndamentoParaAnimal(AnimalId animalId);
    List<Atendimento> buscarPorAnimalId(AnimalId animalId);
    Atendimento buscarUmPorAtendimentoId(AtendimentoId id);
    boolean existeAtendimentoParaAgendamento(AgendamentoId agendamentoId);
}