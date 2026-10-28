package br.ifsp.demo.model.prescricao;

public class ItemPrescricao {
    private final ItemPrescricaoId id;
    private final String medicamento;
    private final Integer dosagem;
    private final String via;
    private final String frequencia;
    private final Integer diasDuracao;

    public ItemPrescricao(String medicamento, Integer dosagem, String via, String frequencia, Integer diasDuracao) {
        this(ItemPrescricaoId.novo(), medicamento, dosagem, via, frequencia, diasDuracao);
    }

    public ItemPrescricao(ItemPrescricaoId id, String medicamento, Integer dosagem, String via, String frequencia, Integer diasDuracao) {
        validarId(id);
        validarMedicamento(medicamento);
        validarDosagem(dosagem);
        validarVia(via);
        validarFrequencia(frequencia);
        validarDuracao(diasDuracao);

        this.id = id;
        this.medicamento = medicamento;
        this.dosagem = dosagem;
        this.via = via;
        this.frequencia = frequencia;
        this.diasDuracao = diasDuracao;
    }

    private static void validarMedicamento(String medicamento) {
        if (medicamento == null || medicamento.isBlank()) {
            throw new IllegalArgumentException("Valor Inválido: Medicamento é obrigatório");
        }
    }

    private static void validarDosagem(Integer dosagem) {
        if (dosagem == null || dosagem <= 0) {
            throw new IllegalArgumentException("Valor Inválido: Dosagem deve ser maior que zero");
        }
    }

    private static void validarVia(String via) {
        if (via == null || via.isBlank()) {
            throw new IllegalArgumentException("Valor Inválido: Via de administração é obrigatória");
        }
    }

    private static void validarFrequencia(String frequencia) {
        if (frequencia == null || frequencia.isBlank()) {
            throw new IllegalArgumentException("Valor Inválido: Frequência é obrigatória");
        }
    }

    private static void validarDuracao(Integer diasDuracao) {
        if (diasDuracao == null || diasDuracao <= 0) {
            throw new IllegalArgumentException("Valor Inválido: Duração deve ser maior que zero");
        }
    }

    private static void validarId(ItemPrescricaoId id) {
        if (id == null) throw new IllegalArgumentException("Valor Inválido: Id é obrigatório");
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