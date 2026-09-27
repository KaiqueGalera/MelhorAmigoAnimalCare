package br.ifsp.demo.model.prescricao;

import java.util.UUID;

public class ItemPrescricao {
    private final ItemPrescricaoId id;
    private String medicamento;
    private Integer dosagem;
    private String via;
    private String frequencia;
    private Integer diasDuracao;

    public ItemPrescricao(String medicamento, Integer dosagem, String via, String frequencia, Integer diasDuracao) {
        this(ItemPrescricaoId.novo(), medicamento, dosagem, via, frequencia, diasDuracao);
    }

    public ItemPrescricao(ItemPrescricaoId id, String medicamento, Integer dosagem, String via, String frequencia, Integer diasDuracao) {
        this.id = id;
        this.medicamento = medicamento;
        this.dosagem = dosagem;
        this.via = via;
        this.frequencia = frequencia;
        this.diasDuracao = diasDuracao;
    }

    public ItemPrescricaoId getId() {
        return id;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public Integer getDosagem() {
        return dosagem;
    }

    public Integer getDiasDuracao() {
        return diasDuracao;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public String getVia() {
        return via;
    }
}
