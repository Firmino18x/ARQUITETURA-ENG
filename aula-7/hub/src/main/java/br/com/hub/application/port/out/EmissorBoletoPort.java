package br.com.hub.application.port.out;

import br.com.hub.domain.model.Fatura;

/**
 * Porta de saída: emissão de boleto (RF02). A emissão é assíncrona, por fila:
 * o código de barras volta depois e chega pelo RegistrarBoletoUseCase.
 */
public interface EmissorBoletoPort {

    void solicitarEmissao(Fatura fatura);
}
