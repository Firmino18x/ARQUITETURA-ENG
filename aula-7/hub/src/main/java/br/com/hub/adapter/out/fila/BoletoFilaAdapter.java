package br.com.hub.adapter.out.fila;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import br.com.hub.application.port.out.EmissorBoletoPort;
import br.com.hub.domain.model.Fatura;
import br.com.hub.infra.fila.FilaEmMemoria;
import br.com.hub.infra.fila.Filas;

/**
 * Adaptador de saída (RF02): pede a emissão do boleto publicando uma mensagem
 * na fila. Não espera resposta; o código de barras volta pela fila de retorno.
 */
@Component
public class BoletoFilaAdapter implements EmissorBoletoPort {

    private static final int DIAS_PARA_VENCIMENTO = 3;

    private final FilaEmMemoria fila;

    public BoletoFilaAdapter(FilaEmMemoria fila) {
        this.fila = fila;
    }

    @Override
    public void solicitarEmissao(Fatura fatura) {
        fila.publicar(Filas.BOLETO_SOLICITACAO, new SolicitacaoBoletoMensagem(
                fatura.getId(),
                fatura.getValorTotal(),
                fatura.getCliente().nome(),
                fatura.getCliente().documento(),
                LocalDate.now().plusDays(DIAS_PARA_VENCIMENTO)));
    }
}
