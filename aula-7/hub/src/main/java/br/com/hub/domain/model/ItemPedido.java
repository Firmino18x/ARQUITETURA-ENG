package br.com.hub.domain.model;

import java.math.BigDecimal;

import br.com.hub.domain.exception.DomainException;

public record ItemPedido(String descricao, int quantidade, BigDecimal valorUnitario) {

    public ItemPedido {
        if (descricao == null || descricao.isBlank()) {
            throw new DomainException("A descrição do item é obrigatória.");
        }
        if (quantidade <= 0) {
            throw new DomainException("A quantidade do item \"" + descricao + "\" deve ser maior que zero.");
        }
        if (valorUnitario == null || valorUnitario.signum() < 0) {
            throw new DomainException("O valor unitário do item \"" + descricao + "\" é inválido.");
        }
    }

    public BigDecimal subtotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
