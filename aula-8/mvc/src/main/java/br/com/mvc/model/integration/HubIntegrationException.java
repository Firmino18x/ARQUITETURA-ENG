package br.com.mvc.model.integration;

/** Falha na comunicação com o hub de faturamento. */
public class HubIntegrationException extends RuntimeException {

    public HubIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public HubIntegrationException(String message) {
        super(message);
    }
}
