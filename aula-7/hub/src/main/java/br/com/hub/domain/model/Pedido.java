package br.com.hub.domain.model;

import java.math.BigDecimal;
import java.util.List;

import br.com.hub.domain.exception.DomainException;

/** Pedido recebido de um sistema de vendas para ser faturado. */
public record Pedido(String pedidoId, Cliente cliente, List<ItemPedido> itens) {

    public Pedido {
        if (pedidoId == null || pedidoId.isBlank()) {
            throw new DomainException("O identificador do pedido é obrigatório.");
        }
        if (cliente == null) {
            throw new DomainException("O cliente do pedido é obrigatório.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new DomainException("O pedido precisa ter ao menos um item.");
        }
        itens = List.copyOf(itens);
    }

    public BigDecimal valorTotal() {
        return itens.stream().map(ItemPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
