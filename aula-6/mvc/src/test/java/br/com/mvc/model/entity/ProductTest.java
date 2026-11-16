package br.com.mvc.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.mvc.model.enums.ProductStatus;
import br.com.mvc.model.exception.BusinessException;

class ProductTest {

    private Product newProduct() {
        Product product = Product.builder().name("Papel A4").price(new BigDecimal("25.90")).build();
        product.prepareForCreation();
        return product;
    }

    @Test
    void novoProdutoComecaIndisponivelESemEstoque() {
        Product product = newProduct();
        assertEquals(ProductStatus.INDISPONIVEL, product.getStatus());
        assertEquals(0, product.getQuantity());
    }

    @Test
    void entradaQueSaiDeZeroDeixaDisponivel() {
        Product product = newProduct();
        product.addStock(10);
        assertEquals(ProductStatus.DISPONIVEL, product.getStatus());
        assertEquals(10, product.getQuantity());
    }

    @Test
    void saidaQueZeraDeixaIndisponivel() {
        Product product = newProduct();
        product.addStock(5);
        product.removeStock(3);
        assertEquals(ProductStatus.DISPONIVEL, product.getStatus());
        product.removeStock(2);
        assertEquals(ProductStatus.INDISPONIVEL, product.getStatus());
        assertEquals(0, product.getQuantity());
    }

    @Test
    void naoPermiteSaidaMaiorQueOSaldo() {
        Product product = newProduct();
        product.addStock(2);
        assertThrows(BusinessException.class, () -> product.removeStock(3));
        assertEquals(2, product.getQuantity());
    }

    @Test
    void naoPermiteQuantidadeZeroOuNegativa() {
        Product product = newProduct();
        assertThrows(BusinessException.class, () -> product.addStock(0));
        assertThrows(BusinessException.class, () -> product.addStock(-1));
    }

    @Test
    void produtoDescontinuadoNaoPodeSerMovimentado() {
        Product product = newProduct();
        product.addStock(5);
        product.discontinue();
        assertEquals(ProductStatus.DESCONTINUADO, product.getStatus());
        assertThrows(BusinessException.class, () -> product.addStock(1));
        assertThrows(BusinessException.class, () -> product.removeStock(1));
        assertThrows(BusinessException.class, product::discontinue);
    }
}
