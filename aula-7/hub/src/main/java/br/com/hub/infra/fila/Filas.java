package br.com.hub.infra.fila;

/** Nomes das filas usadas na emissão de boleto. */
public final class Filas {

    /** Hub → banco: pedidos de emissão de boleto. */
    public static final String BOLETO_SOLICITACAO = "boleto.solicitacao";

    /** Banco → hub: boletos emitidos, com o código de barras. */
    public static final String BOLETO_RETORNO = "boleto.retorno";

    private Filas() {
    }
}
