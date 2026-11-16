package br.com.hub.domain.model;

public enum StatusFatura {
    /** Fatura criada e persistida (RF01). */
    CRIADA,
    /** Nota fiscal emitida (RF03) e boleto solicitado na fila (RF02), aguardando o retorno. */
    AGUARDANDO_BOLETO,
    /** Boleto registrado e cliente notificado por e-mail (RF04). */
    FATURADA
}
