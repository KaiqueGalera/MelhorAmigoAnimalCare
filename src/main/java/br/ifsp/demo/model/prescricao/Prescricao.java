package br.ifsp.demo.model.prescricao;

import java.util.*;

public class Prescricao {
    private final Map<ItemPrescricaoId, ItemPrescricao> itens = new LinkedHashMap<>();
    private StatusPrescricao status;

    public Prescricao(List<ItemPrescricao> itensIniciais) {
        if (itensIniciais == null || itensIniciais.isEmpty()) {
            throw new IllegalArgumentException("Emissão rejeitada: só é possível emitir uma prescrição com pelo menos um item.");
        }
        for (ItemPrescricao item : itensIniciais) {
            itens.put(item.getId(), item);
        }
        this.status = StatusPrescricao.ABERTA;
    }

    public void editarItem(ItemPrescricaoId id, ItemPrescricao novoItem) {
        garantePrescricaoAberta("Prescrição deve estar 'Aberta' para ser alterada");
        itens.put(id, novoItem);
    }

    public void removerItem(ItemPrescricaoId id) {
        garantePrescricaoAberta("Prescrição deve estar 'Aberta' para ser alterada");
        itens.remove(id);
    }

    public void finalizar() {
        garantePrescricaoAberta("A prescrição deve estar 'Aberta' para ser finalizada.");
        if (itens.isEmpty()) throw new IllegalStateException("A prescrição deve conter pelo menos um item para ser finalizada.");
        status = StatusPrescricao.FINALIZADA;
    }

    public void garantePrescricaoAberta(String mensagem) {
        if (status != StatusPrescricao.ABERTA) throw new IllegalStateException(mensagem);
    }

    public StatusPrescricao getStatus() {
        return status;
    }

    public List<ItemPrescricao> getItens() {
        return List.copyOf(itens.values());
    }
}
