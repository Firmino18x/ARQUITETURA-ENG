package br.com.hub.adapter.out.fila;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Mensagem publicada na fila boleto.solicitacao. */
public record SolicitacaoBoletoMensagem(
        UUID faturaId,
        BigDecimal valor,
        String pagadorNome,
        String pagadorDocumento,
        LocalDate vencimento) {
}
