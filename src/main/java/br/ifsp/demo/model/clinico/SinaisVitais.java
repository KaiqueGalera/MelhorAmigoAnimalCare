package br.ifsp.demo.model.clinico;

public record SinaisVitais(
        double temperaturaC,
        int frequenciaCardiaca,
        double pesoKg
    ) {
    public SinaisVitais{
        if (temperaturaC <= 0) {
            throw new IllegalArgumentException("Temperatura deve ser maior que zero");
        }
        if (frequenciaCardiaca <= 0) {
            throw new IllegalArgumentException("Frequência cardíaca deve ser maior que zero");
        }
        if (pesoKg <= 0) {
            throw new IllegalArgumentException("Peso deve ser maior que zero");
        }
    }
}
