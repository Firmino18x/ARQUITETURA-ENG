package br.com.hub.adapter.in.fila;

import java.util.UUID;

/** Mensagem que o banco devolve na fila boleto.retorno. */
public record RetornoBoletoMensagem(UUID faturaId, String codigoBarras) {
}
