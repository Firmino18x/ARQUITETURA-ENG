package br.com.mvc.model.exception;

/**
 * Violação de uma regra de negócio. É tratada pelo ErrorController,
 * que mostra a mensagem na tela de erro.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
