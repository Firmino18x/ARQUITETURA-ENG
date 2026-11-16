package br.com.hub.application.port.out;

import br.com.hub.domain.model.Fatura;

/** Porta de saída: emissão de nota fiscal (RF03), feita por SOAP. */
public interface EmissorNotaFiscalPort {

    /** @return chave de acesso da nota, com 44 dígitos */
    String emitir(Fatura fatura);
}
