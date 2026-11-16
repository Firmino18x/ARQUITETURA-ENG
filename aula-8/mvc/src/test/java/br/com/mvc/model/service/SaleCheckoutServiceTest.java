package br.com.mvc.model.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import br.com.mvc.model.entity.Sale;
import br.com.mvc.model.enums.SaleStatus;
import br.com.mvc.model.exception.BusinessException;
import br.com.mvc.model.integration.BillingHubClient;
import br.com.mvc.model.integration.HubIntegrationException;

class SaleCheckoutServiceTest {

    private final SaleService saleService = mock(SaleService.class);
    private final BillingHubClient hub = mock(BillingHubClient.class);
    private final SaleCheckoutService checkout = new SaleCheckoutService(saleService, hub);

    private Sale closedSale() {
        return Sale.builder().id(BigInteger.ONE).status(SaleStatus.FECHADA).build();
    }

    @Test
    void fechaEnviaAoHubEGuardaOIdDaFatura() {
        Sale sale = closedSale();
        when(saleService.closeLocally(BigInteger.ONE)).thenReturn(sale);
        when(hub.createInvoice(sale)).thenReturn("fatura-123");

        checkout.close(BigInteger.ONE);

        verify(saleService).markInvoiced(BigInteger.ONE, "fatura-123");
        verify(saleService, never()).markInvoiceFailure(any(), anyString());
    }

    @Test
    void falhaNoHubDeixaPedidoComFalhaDeFaturamento() {
        Sale sale = closedSale();
        when(saleService.closeLocally(BigInteger.ONE)).thenReturn(sale);
        when(hub.createInvoice(sale)).thenThrow(new HubIntegrationException("Hub indisponível"));

        checkout.close(BigInteger.ONE);

        verify(saleService).markInvoiceFailure(eq(BigInteger.ONE), eq("Hub indisponível"));
        verify(saleService, never()).markInvoiced(any(), anyString());
    }

    @Test
    void naoReenviaPedidoJaFaturado() {
        Sale sale = Sale.builder().id(BigInteger.ONE).status(SaleStatus.FATURADA).build();
        when(saleService.findById(BigInteger.ONE)).thenReturn(sale);

        assertThrows(BusinessException.class, () -> checkout.retryInvoice(BigInteger.ONE));
        verify(hub, never()).createInvoice(any());
    }
}
