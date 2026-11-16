package br.com.mvc.model.integration;

import java.math.BigDecimal;
import java.util.List;

import br.com.mvc.model.entity.Sale;

/** JSON enviado ao POST /faturas do hub (contrato definido pelo hub da aula 7). */
public record HubInvoiceRequest(String pedidoId, Customer cliente, List<Item> itens) {

    public record Customer(String nome, String email, String documento) {
    }

    public record Item(String descricao, Integer quantidade, BigDecimal valorUnitario) {
    }

    public static HubInvoiceRequest from(Sale sale) {
        return new HubInvoiceRequest(
                sale.getExternalId(),
                new Customer(sale.getCustomerName(), sale.getCustomerEmail(), sale.getCustomerDocument()),
                sale.getItems().stream()
                        .map(i -> new Item(i.getProduct().getName(), i.getQuantity(), i.getUnitPrice()))
                        .toList());
    }
}
