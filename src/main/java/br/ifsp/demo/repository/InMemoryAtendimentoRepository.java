package br.ifsp.demo.repository;

import br.ifsp.demo.model.abertura.Atendimento;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAtendimentoRepository {
    private final List<Atendimento> atendimentos = new ArrayList<>();

    public void salvar(Atendimento atendimento){
        atendimentos.add(atendimento);
    }
}
