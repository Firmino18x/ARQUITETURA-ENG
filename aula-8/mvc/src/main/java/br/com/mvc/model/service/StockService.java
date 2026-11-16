package br.com.mvc.model.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.mvc.model.entity.Product;
import br.com.mvc.model.entity.StockMovement;
import br.com.mvc.model.enums.MovementType;
import br.com.mvc.model.exception.BusinessException;
import br.com.mvc.model.repository.ProductRepository;
import br.com.mvc.model.repository.StockMovementRepository;

@Service
public class StockService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public StockService(ProductRepository productRepository, StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<StockMovement> readMovements() {
        return stockMovementRepository.findAllByOrderByCreatedAtDescIdDesc();
    }

    @Transactional
    public StockMovement register(BigInteger productId, MovementType type, Integer quantity) {
        if (type == null) {
            throw new BusinessException("Informe o tipo da movimentação (entrada ou saída).");
        }
        return type == MovementType.ENTRADA
                ? registerEntry(productId, quantity)
                : registerExit(productId, quantity);
    }

    @Transactional
    public StockMovement registerEntry(BigInteger productId, Integer quantity) {
        Product product = findProduct(productId);
        product.addStock(quantity);
        return save(product, MovementType.ENTRADA, quantity);
    }

    @Transactional
    public StockMovement registerExit(BigInteger productId, Integer quantity) {
        Product product = findProduct(productId);
        product.removeStock(quantity);
        return save(product, MovementType.SAIDA, quantity);
    }

    private Product findProduct(BigInteger productId) {
        if (productId == null) {
            throw new BusinessException("Selecione um produto.");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Produto " + productId + " não encontrado."));
    }

    private StockMovement save(Product product, MovementType type, Integer quantity) {
        productRepository.save(product);
        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(type)
                .quantity(quantity)
                .balanceAfter(product.getQuantity())
                .createdAt(LocalDateTime.now())
                .build();
        return stockMovementRepository.save(movement);
    }
}
