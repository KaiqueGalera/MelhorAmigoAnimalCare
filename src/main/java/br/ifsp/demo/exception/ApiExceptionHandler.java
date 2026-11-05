package br.ifsp.demo.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({
            IllegalArgumentException.class,
            NullPointerException.class,
            JustificativaObrigatoriaException.class
    })
    public ResponseEntity<ApiException> handleBadRequest(RuntimeException e) {
        return build(BAD_REQUEST, e.getMessage(), e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiException> handleUnreadableBody(HttpMessageNotReadableException e) {
        return build(BAD_REQUEST, "Corpo da requisição inválido ou mal formatado.", e);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiException> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return build(BAD_REQUEST, "Valor inválido para o parâmetro '" + e.getName() + "'.", e);
    }

    @ExceptionHandler({
            AtendimentoNaoEncontradoException.class,
            ExameNaoEncontradoException.class,
            EntityNotFoundException.class
    })
    public ResponseEntity<ApiException> handleNotFound(RuntimeException e) {
        return build(NOT_FOUND, e.getMessage(), e);
    }

    @ExceptionHandler({
            AnimalJaEmAtendimentoException.class,
            AgendamentoJaUtilizadoException.class,
            AtendimentoCanceladoExcepiton.class,
            AtendimentoNaoConcluidoException.class,
            EntityAlreadyExistsException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ApiException> handleConflict(RuntimeException e) {
        return build(CONFLICT, e.getMessage(), e);
    }

    @ExceptionHandler({
            SinaisVitaisObrigatoriosException.class,
            DiagnosticoObrigatorioException.class,
            JanelaReaberturaExpiradaException.class,
            InstanteReaberturaInvalidoException.class
    })
    public ResponseEntity<ApiException> handleUnprocessable(RuntimeException e) {
        return build(UNPROCESSABLE_ENTITY, e.getMessage(), e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiException> handleUnexpected(Exception e) {
        if (e instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            return build(status, errorResponse.getBody().getDetail(), e);
        }
        log.error("Erro inesperado ao processar requisição", e);
        return build(INTERNAL_SERVER_ERROR, "Erro interno no servidor.", e);
    }

    private ResponseEntity<ApiException> build(HttpStatus status, String message, Exception e) {
        ApiException body = ApiException.builder()
                .status(status)
                .message(message)
                .developerMessage(e.getClass().getName())
                .timestamp(ZonedDateTime.now(ZoneId.of("Z")))
                .build();
        return new ResponseEntity<>(body, status);
    }
}
