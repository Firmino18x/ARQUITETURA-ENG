package br.com.hub.domain.model;

import br.com.hub.domain.exception.DomainException;

/** Cliente que fez o pedido: recebe a nota, o boleto e o e-mail. */
public record Cliente(String nome, String email, String documento) {

    public Cliente {
        if (nome == null || nome.isBlank()) {
            throw new DomainException("O nome do cliente é obrigatório.");
        }
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new DomainException("O e-mail do cliente é inválido.");
        }
        documento = documento == null ? "" : documento.replaceAll("\\D", "");
        if (documento.length() != 11 && documento.length() != 14) {
            throw new DomainException("O documento do cliente deve ser um CPF (11 dígitos) ou CNPJ (14 dígitos).");
        }
    }
}
