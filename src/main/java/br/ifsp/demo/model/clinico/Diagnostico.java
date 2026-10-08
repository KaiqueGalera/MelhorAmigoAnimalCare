package br.ifsp.demo.model.clinico;

public record Diagnostico(
        String codigo,
        String descricao,
        TipoDiagnostico tipo
) {
    private static final int LIMITE_CODIGO = 10;
    private static final int LIMITE_DESCRICAO = 200;

    public Diagnostico{
        if(codigo.length() > LIMITE_CODIGO){
            throw new IllegalArgumentException("O código deve ter no máximo " + LIMITE_CODIGO + "caracteres. Reduza o texto por gentileza.");
        }

        if(descricao.length() > LIMITE_DESCRICAO){
            throw new IllegalArgumentException("A descriçao deve ter no máximo " + LIMITE_DESCRICAO + "caracteres. Reduza o texto por gentileza.");
        }
    }
}
