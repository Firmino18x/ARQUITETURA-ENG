package br.com.mvc.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.mvc.model.entity.Product;
import br.com.mvc.model.enums.ProductStatus;
import br.com.mvc.model.enums.Role;
import br.com.mvc.model.exception.BusinessException;
import br.com.mvc.model.repository.ProductRepository;

class ProductServiceTest {

    private final ProductRepository repository = mock(ProductRepository.class);
    private final ProductService service = new ProductService(repository);

    @Test
    void apenasAdmPodeDescontinuar() {
        assertThrows(BusinessException.class, () -> service.discontinue(BigInteger.ONE, Role.USUARIO));
        verify(repository, never()).save(any());
    }

    @Test
    void admDescontinuaProduto() {
        Product product = Product.builder().id(BigInteger.ONE).name("Grampeador").build();
        when(repository.findById(BigInteger.ONE)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.discontinue(BigInteger.ONE, Role.ADM);

        assertEquals(ProductStatus.DESCONTINUADO, result.getStatus());
    }

    @Test
    void criarIgnoraStatusEEstoqueEnviadosNoFormulario() {
        Product product = Product.builder().name("Caneta").status(ProductStatus.DISPONIVEL).quantity(99).build();
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = service.create(product);

        assertEquals(ProductStatus.INDISPONIVEL, result.getStatus());
        assertEquals(0, result.getQuantity());
    }
}
