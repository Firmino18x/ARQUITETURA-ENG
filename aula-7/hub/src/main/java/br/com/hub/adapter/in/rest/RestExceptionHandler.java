package br.com.hub.adapter.in.rest;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.hub.domain.exception.DomainException;
import br.com.hub.domain.exception.FaturaNaoEncontradaException;

@RestControllerAdvice
public class RestExceptionHandler {

    public record ErroResponse(String mensagem, LocalDateTime momento) {
    }

    @ExceptionHandler(FaturaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> naoEncontrada(FaturaNaoEncontradaException e) {
        return erro(404, e.getMessage());
    }

    /** 422: o JSON chegou, mas o pedido viola uma regra de negócio. */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(DomainException e) {
        return erro(422, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException e) {
        return erro(400, "JSON do pedido inválido.");
    }

    private ResponseEntity<ErroResponse> erro(int status, String mensagem) {
        return ResponseEntity.status(HttpStatusCode.valueOf(status)).body(new ErroResponse(mensagem, LocalDateTime.now()));
    }
}
