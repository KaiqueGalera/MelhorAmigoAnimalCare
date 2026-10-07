package br.ifsp.demo.model.prescricao;

import java.time.LocalDateTime;
import java.util.*;

public class Prescricao {
    private final PrescricaoId id;
    private final Map<ItemPrescricaoId, ItemPrescricao> itens = new LinkedHashMap<>();
    private StatusPrescricao status;
    private final LocalDateTime dataHoraEmissao;

    public Prescricao(List<ItemPrescricao> itensIniciais) {
        this(PrescricaoId.novo(), itensIniciais, LocalDateTime.now());
    }

    public Prescricao(PrescricaoId id, List<ItemPrescricao> itensIniciais, LocalDateTime dataHoraEmissao) {
        if (itensIniciais == null || itensIniciais.isEmpty()) {
            throw new IllegalArgumentException("Emissão rejeitada: só é possível emitir uma prescrição com pelo menos um item.");
        }
        for (ItemPrescricao item : itensIniciais) {
            itens.put(item.getId(), item);
        }
        this.id = id;
        this.status = StatusPrescricao.ABERTA;
        this.dataHoraEmissao = dataHoraEmissao;
    }

    public void editarItem(ItemPrescricaoId id, ItemPrescricao novoItem) {
        garantePrescricaoAberta("Prescrição deve estar 'Aberta' para ser alterada");
        if (id == null) {
            throw new IllegalArgumentException("O id do item é obrigatório.");
        }
        if (novoItem == null) {
            throw new IllegalArgumentException("O novo item é obrigatório.");
        }
        garanteItemExistente(id);
        if (!novoItem.getId().equals(id)) {
            throw new IllegalArgumentException("O id do item editado deve ser igual ao id original.");
        }
        itens.put(id, novoItem);
    }

    public void removerItem(ItemPrescricaoId id) {
        garantePrescricaoAberta("Prescrição deve estar 'Aberta' para ser alterada");
        if (id == null) {
            throw new IllegalArgumentException("O id do item é obrigatório.");
        }
        garanteItemExistente(id);
        itens.remove(id);
    }

    private void garanteItemExistente(ItemPrescricaoId id) {
        if (!itens.containsKey(id)) {
            throw new IllegalArgumentException("Item de prescrição não encontrado.");
        }
    }

    public void finalizar() {
        garantePrescricaoAberta("A prescrição deve estar 'Aberta' para ser finalizada.");
        if (itens.isEmpty()) throw new IllegalStateException("A prescrição deve conter pelo menos um item para ser finalizada.");
        status = StatusPrescricao.FINALIZADA;
    }

    public void garantePrescricaoAberta(String mensagem) {
        if (status != StatusPrescricao.ABERTA) throw new IllegalStateException(mensagem);
    }

    public void cancelar() {
        garantePrescricaoAberta("A prescrição deve estar 'Aberta' para ser cancelada.");
        status = StatusPrescricao.CANCELADA;
    }

    public PrescricaoId getId() { return id; }

    public StatusPrescricao getStatus() {
        return status;
    }

    public List<ItemPrescricao> getItens() {
        return List.copyOf(itens.values());
    }

    public LocalDateTime getDataHoraEmissao() { return dataHoraEmissao; }
}