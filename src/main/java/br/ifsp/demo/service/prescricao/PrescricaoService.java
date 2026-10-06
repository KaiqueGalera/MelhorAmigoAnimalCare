package br.ifsp.demo.service.prescricao;

import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AtendimentoId;
import br.ifsp.demo.model.prescricao.*;
import br.ifsp.demo.repository.InMemoryAtendimentoRepository;

import java.util.List;

public class PrescricaoService {
    private final InMemoryAtendimentoRepository repository;

    public PrescricaoService(InMemoryAtendimentoRepository repository) {
        this.repository = repository;
    }

    public Prescricao emitirPrescricao(AtendimentoId atendimentoId, List<ItemPrescricao> itens) {
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(atendimentoId);
        Prescricao prescricao = atendimento.emitirPrescricao(itens);
        repository.salvar(atendimento);
        return prescricao;
    }

    public List<Prescricao> consultarPrescricoes(AtendimentoId atendimentoId) {
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(atendimentoId);
        return atendimento.getPrescricoes();
    }

    public void editarItem(AtendimentoId atendimentoId, PrescricaoId prescricaoId,
                           ItemPrescricaoId itemId, ItemPrescricao novoItem) {
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(atendimentoId);
        Prescricao prescricao = atendimento.encontrarPrescricao(prescricaoId);
        prescricao.editarItem(itemId, novoItem);
        repository.salvar(atendimento);
    }

    public void removerItem(AtendimentoId atendimentoId, PrescricaoId prescricaoId, ItemPrescricaoId itemId) {
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(atendimentoId);
        Prescricao prescricao = atendimento.encontrarPrescricao(prescricaoId);
        prescricao.removerItem(itemId);
        repository.salvar(atendimento);
    }

    public void finalizar(AtendimentoId atendimentoId, PrescricaoId prescricaoId) {
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(atendimentoId);
        Prescricao prescricao = atendimento.encontrarPrescricao(prescricaoId);
        prescricao.finalizar();
        repository.salvar(atendimento);
    }

    public void cancelar(AtendimentoId atendimentoId, PrescricaoId prescricaoId) {
        Atendimento atendimento = repository.buscarUmPorAtendimentoId(atendimentoId);
        Prescricao prescricao = atendimento.encontrarPrescricao(prescricaoId);
        prescricao.cancelar();
        repository.salvar(atendimento);
    }
}