package br.com.hub.adapter.in.rest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.StatusFatura;

/** Resposta do GET /faturas/{id}. */
public record FaturaResponse(
        UUID id,
        String pedidoId,
        String clienteNome,
        String clienteEmail,
        BigDecimal valorTotal,
        StatusFatura status,
        String chaveAcessoNotaFiscal,
        String codigoBarrasBoleto,
        String hashEmail,
        LocalDateTime criadaEm,
        LocalDateTime atualizadaEm) {

    public static FaturaResponse de(Fatura f) {
        return new FaturaResponse(f.getId(), f.getPedidoId(), f.getCliente().nome(), f.getCliente().email(),
                f.getValorTotal(), f.getStatus(), f.getChaveAcessoNotaFiscal(), f.getCodigoBarrasBoleto(),
                f.getHashEmail(), f.getCriadaEm(), f.getAtualizadaEm());
    }
}
