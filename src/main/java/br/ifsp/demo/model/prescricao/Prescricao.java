package br.ifsp.demo.model.prescricao;

import java.util.List;

public class Prescricao {

    private final List<ItemPrescricao> itens;
    private StatusPrescricao status;

    public Prescricao(List<ItemPrescricao> itens) {
        if (itens.isEmpty())
            throw new IllegalArgumentException("Emissão rejeitada: só é possível emitir uma prescrição com pelo menos um item.");

        this.itens = itens;
        this.status = StatusPrescricao.ABERTA;
    }

    public StatusPrescricao getStatus() {
        return status;
    }
}
