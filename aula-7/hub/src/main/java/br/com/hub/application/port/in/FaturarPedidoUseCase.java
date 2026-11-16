package br.com.hub.application.port.in;

import java.util.UUID;

import br.com.hub.domain.model.Pedido;

/** Porta de entrada: faturar um pedido recebido (RF01 a RF04). */
public interface FaturarPedidoUseCase {

    /** Cria e persiste a fatura, dispara as integrações e devolve o ID da fatura. */
    UUID faturar(Pedido pedido);
}
