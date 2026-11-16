package br.com.hub.simulador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.hub.adapter.in.fila.RetornoBoletoMensagem;
import br.com.hub.adapter.out.fila.SolicitacaoBoletoMensagem;
import br.com.hub.infra.fila.FilaEmMemoria;
import br.com.hub.infra.fila.Filas;

/**
 * Simula o sistema do banco do outro lado da fila: consome os pedidos de
 * boleto, "demora" um pouco, gera o código de barras e publica o retorno.
 */
@Component
public class BancoSimulado {

    private static final Logger log = LoggerFactory.getLogger(BancoSimulado.class);

    private final FilaEmMemoria fila;
    private final long atrasoMs;

    public BancoSimulado(FilaEmMemoria fila, @Value("${hub.simulador.banco.atraso-ms:1500}") long atrasoMs) {
        this.fila = fila;
        this.atrasoMs = atrasoMs;
        fila.consumir(Filas.BOLETO_SOLICITACAO, SolicitacaoBoletoMensagem.class, this::emitir);
    }

    private void emitir(SolicitacaoBoletoMensagem solicitacao) {
        esperar();
        String codigoBarras = CodigoBarrasBoleto.gerar(solicitacao.valor(), solicitacao.vencimento());
        log.info("[BANCO] boleto emitido para a fatura {}: {}", solicitacao.faturaId(), codigoBarras);
        fila.publicar(Filas.BOLETO_RETORNO, new RetornoBoletoMensagem(solicitacao.faturaId(), codigoBarras));
    }

    private void esperar() {
        try {
            Thread.sleep(atrasoMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
