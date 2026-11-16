package br.com.hub.domain.exception;

import java.util.UUID;

public class FaturaNaoEncontradaException extends DomainException {

    public FaturaNaoEncontradaException(UUID id) {
        super("Fatura " + id + " não encontrada.");
    }
}
