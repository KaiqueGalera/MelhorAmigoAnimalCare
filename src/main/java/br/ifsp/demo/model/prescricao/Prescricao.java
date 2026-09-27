package br.ifsp.demo.model.prescricao;

import java.util.List;

public class Prescricao {

    private final List<ItemPrescricao> itens;
    private StatusPrescricao status;

    public Prescricao(List<ItemPrescricao> itens) {
        this.itens = itens;
        this.status = StatusPrescricao.ABERTA;
    }

    public StatusPrescricao getStatus() {
        return status;
    }
}
