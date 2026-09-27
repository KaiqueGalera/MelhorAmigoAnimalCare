package br.ifsp.demo.model.prescricao;

public class ItemPrescricao {
    private final String medicamento;
    private final Integer dosagem;
    private final String via;
    private final String frequencia;
    private final Integer diasDuracao;

    public ItemPrescricao(String medicamento, Integer dosagem, String via, String frequencia, Integer diasDuracao) {
        this.medicamento = medicamento;
        this.dosagem = dosagem;
        this.via = via;
        this.frequencia = frequencia;
        this.diasDuracao = diasDuracao;
    }
}
