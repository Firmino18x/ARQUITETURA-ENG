package br.com.mvc.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import br.com.mvc.model.enums.SaleStatus;
import br.com.mvc.model.exception.BusinessException;

class SaleTest {

    private Sale newSale() {
        Sale sale = Sale.builder()
                .customerName("Michael Scott")
                .customerEmail("michael@dundermifflin.com")
                .customerDocument("123.456.789-09")
                .build();
        sale.prepareForCreation();
        sale.setId(BigInteger.ONE);
        return sale;
    }

    private Product availableProduct(long id, int stock) {
        Product product = Product.builder().id(BigInteger.valueOf(id)).name("Papel A4")
                .price(new BigDecimal("25.90")).build();
        product.prepareForCreation();
        product.setId(BigInteger.valueOf(id));
        product.addStock(stock);
        return product;
    }

    @Test
    void novoPedidoComecaAbertoComDocumentoSoComDigitos() {
        Sale sale = newSale();
        assertEquals(SaleStatus.ABERTA, sale.getStatus());
        assertEquals("12345678909", sale.getCustomerDocument());
        assertEquals("VENDA-1", sale.getExternalId());
    }

    @Test
    void somaQuantidadeDoMesmoProdutoECalculaTotal() {
        Sale sale = newSale();
        Product papel = availableProduct(1, 10);
        sale.addItem(papel, 2);
        sale.addItem(papel, 3);
        assertEquals(1, sale.getItems().size());
        assertEquals(new BigDecimal("129.50"), sale.getTotal());
    }

    @Test
    void naoAdicionaMaisQueOEstoque() {
        Sale sale = newSale();
        assertThrows(BusinessException.class, () -> sale.addItem(availableProduct(1, 2), 3));
    }

    @Test
    void naoAdicionaProdutoIndisponivel() {
        Sale sale = newSale();
        Product semEstoque = Product.builder().id(BigInteger.TWO).name("Caneta").price(BigDecimal.ONE).build();
        semEstoque.prepareForCreation();
        assertThrows(BusinessException.class, () -> sale.addItem(semEstoque, 1));
    }

    @Test
    void naoFechaPedidoVazio() {
        assertThrows(BusinessException.class, newSale()::close);
    }

    @Test
    void cicloDeFaturamento() {
        Sale sale = newSale();
        sale.addItem(availableProduct(1, 10), 1);
        sale.close();
        assertEquals(SaleStatus.FECHADA, sale.getStatus());

        sale.markInvoiceFailure("hub fora do ar");
        assertEquals(SaleStatus.FALHA_FATURAMENTO, sale.getStatus());

        sale.markInvoiced("7f6ae5b2-4dfa-4627-b352-8408e9229c15");
        assertEquals(SaleStatus.FATURADA, sale.getStatus());
        assertEquals("7f6ae5b2-4dfa-4627-b352-8408e9229c15", sale.getInvoiceId());
        assertEquals(null, sale.getInvoiceError());
        assertThrows(BusinessException.class, () -> sale.addItem(availableProduct(2, 5), 1));
    }

    @Test
    void documentoInvalidoEhRejeitado() {
        Sale sale = Sale.builder().customerName("Pam").customerEmail("pam@dm.com").customerDocument("123").build();
        assertThrows(BusinessException.class, sale::prepareForCreation);
    }
}
