package br.com.hub.adapter.in.rest;

import java.math.BigDecimal;
import java.util.List;

import br.com.hub.domain.model.Cliente;
import br.com.hub.domain.model.ItemPedido;
import br.com.hub.domain.model.Pedido;

/** JSON recebido em POST /faturas. */
public record PedidoRequest(String pedidoId, ClienteRequest cliente, List<ItemRequest> itens) {

    public record ClienteRequest(String nome, String email, String documento) {
    }

    public record ItemRequest(String descricao, Integer quantidade, BigDecimal valorUnitario) {
    }

    public Pedido paraDominio() {
        Cliente clienteDominio = cliente == null ? null
                : new Cliente(cliente.nome(), cliente.email(), cliente.documento());
        List<ItemPedido> itensDominio = itens == null ? null
                : itens.stream()
                        .map(i -> new ItemPedido(i.descricao(), i.quantidade() == null ? 0 : i.quantidade(),
                                i.valorUnitario()))
                        .toList();
        return new Pedido(pedidoId, clienteDominio, itensDominio);
    }
}
