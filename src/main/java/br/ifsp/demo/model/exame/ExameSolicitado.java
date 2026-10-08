package br.ifsp.demo.model.exame;

import java.time.LocalDateTime;
import java.util.Objects;

public class ExameSolicitado {
    private final Exame exame;
    private final LocalDateTime data;
    private String resultado = "";
    private ExameStatus status = ExameStatus.PENDENTE;

    public ExameSolicitado(Exame exame, LocalDateTime data) {
        this.exame = exame;
        this.data = data;
    }

    public LocalDateTime getData() {
        return data;
    }

    public Exame getExame() {
        return exame;
    }

    public String getResultado() {
        return resultado;
    }

    public ExameStatus getStatus() {
        return status;
    }

    public void setResultado(String resultado) {
        if (resultado.isBlank()) return;

        this.resultado = resultado;

        setStatus();
    }

    private void setStatus() {
        this.status = ExameStatus.CONCLUIDO;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExameSolicitado that = (ExameSolicitado) o;
        return Objects.equals(exame, that.exame) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exame, data);
    }
}
