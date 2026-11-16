package br.com.hub.domain.exception;

/** Violação de regra de negócio do domínio de faturamento. */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
