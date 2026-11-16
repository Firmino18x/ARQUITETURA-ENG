package br.com.hub.application.port.in;

import java.util.UUID;

/** Porta de entrada acionada pelo retorno da fila de boletos. */
public interface RegistrarBoletoUseCase {

    void registrarBoleto(UUID faturaId, String codigoBarras);
}
