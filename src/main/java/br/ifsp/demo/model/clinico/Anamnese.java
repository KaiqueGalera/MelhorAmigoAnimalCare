package br.ifsp.demo.model.clinico;

public record Anamnese(String queixaPrincipal, String historicoClinico) {

    private static final int LIMITE_QUEIXA_PRINCIPAL = 100;
    private static final int LIMITE_HISTORICO_CLINICO = 500;

    public Anamnese{
        if (queixaPrincipal == null || queixaPrincipal.isBlank()) {
            throw new IllegalArgumentException("A queixa principal da anamnese é obrigatória.");
        }
        if (historicoClinico == null || historicoClinico.isBlank()) {
            throw new IllegalArgumentException("O histórico clínico da anamnese é obrigatório.");
        }

        if(queixaPrincipal.length() > LIMITE_QUEIXA_PRINCIPAL){
            throw new IllegalArgumentException("A queixa principal deve ter no máximo " + LIMITE_QUEIXA_PRINCIPAL + "caracteres. Reduza o texto por gentileza.");
        }

        if(historicoClinico.length() > LIMITE_HISTORICO_CLINICO){
            throw new IllegalArgumentException("O histórico clínico deve ter no máximo " + LIMITE_HISTORICO_CLINICO + "caracteres. Reduza o texto por gentileza.");
        }
    }
}
