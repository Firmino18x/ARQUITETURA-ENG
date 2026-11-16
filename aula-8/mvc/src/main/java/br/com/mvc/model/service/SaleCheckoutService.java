package br.com.mvc.model.service;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.mvc.model.entity.Sale;
import br.com.mvc.model.exception.BusinessException;
import br.com.mvc.model.integration.BillingHubClient;
import br.com.mvc.model.integration.HubIntegrationException;

/**
 * Orquestra o fechamento da venda com o hub de faturamento.
 *
 * Não é transacional de propósito: primeiro o pedido é fechado e o estoque é
 * baixado (transação local confirmada); só depois o hub é chamado. Assim a
 * transação do banco não fica aberta esperando a rede.
 */
@Service
public class SaleCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(SaleCheckoutService.class);

    private final SaleService saleService;
    private final BillingHubClient billingHubClient;

    public SaleCheckoutService(SaleService saleService, BillingHubClient billingHubClient) {
        this.saleService = saleService;
        this.billingHubClient = billingHubClient;
    }

    /** Fecha o pedido e envia ao hub. Devolve o pedido já com o ID da fatura ou com a falha registrada. */
    public Sale close(BigInteger saleId) {
        Sale sale = saleService.closeLocally(saleId);
        return sendToHub(sale);
    }

    /** Reenvia ao hub um pedido fechado que ficou sem fatura. */
    public Sale retryInvoice(BigInteger saleId) {
        Sale sale = saleService.findById(saleId);
        if (!sale.canSendToHub()) {
            throw new BusinessException("O pedido " + saleId + " não está aguardando faturamento.");
        }
        return sendToHub(sale);
    }

    private Sale sendToHub(Sale sale) {
        try {
            String invoiceId = billingHubClient.createInvoice(sale);
            log.info("Pedido {} faturado no hub: fatura {}", sale.getId(), invoiceId);
            return saleService.markInvoiced(sale.getId(), invoiceId);
        } catch (HubIntegrationException e) {
            log.warn("Falha ao faturar o pedido {} no hub: {}", sale.getId(), e.getMessage());
            return saleService.markInvoiceFailure(sale.getId(), e.getMessage());
        }
    }
}
