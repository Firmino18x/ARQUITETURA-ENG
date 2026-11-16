package br.com.hub.adapter.in.fila;

import org.springframework.stereotype.Component;

import br.com.hub.application.port.in.RegistrarBoletoUseCase;
import br.com.hub.infra.fila.FilaEmMemoria;
import br.com.hub.infra.fila.Filas;

/**
 * Adaptador de entrada da fila: escuta os boletos emitidos e entrega
 * o código de barras ao núcleo pela porta RegistrarBoletoUseCase.
 */
@Component
public class RetornoBoletoListener {

    public RetornoBoletoListener(FilaEmMemoria fila, RegistrarBoletoUseCase registrarBoleto) {
        fila.consumir(Filas.BOLETO_RETORNO, RetornoBoletoMensagem.class,
                msg -> registrarBoleto.registrarBoleto(msg.faturaId(), msg.codigoBarras()));
    }
}
