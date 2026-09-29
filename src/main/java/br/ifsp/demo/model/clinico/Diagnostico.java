package br.ifsp.demo.model.clinico;

public record Diagnostico(
        String codigo,
        String descricao,
        TipoDiagnostico tipo
) {
}
