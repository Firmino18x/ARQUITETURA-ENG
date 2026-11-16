package br.com.mvc.model.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.mvc.model.entity.Product;
import br.com.mvc.model.entity.Sale;
import br.com.mvc.model.entity.SaleItem;
import br.com.mvc.model.exception.BusinessException;
import br.com.mvc.model.repository.SaleRepository;

/** Operações locais do pedido de venda, cada uma em sua transação. */
@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductService productService;
    private final StockService stockService;

    public SaleService(SaleRepository saleRepository, ProductService productService, StockService stockService) {
        this.saleRepository = saleRepository;
        this.productService = productService;
        this.stockService = stockService;
    }

    public List<Sale> read() {
        return saleRepository.findAllByOrderByIdDesc();
    }

    public Sale findById(BigInteger id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Pedido " + id + " não encontrado."));
    }

    @Transactional
    public Sale create(Sale sale) {
        sale.prepareForCreation();
        return saleRepository.save(sale);
    }

    @Transactional
    public Sale addItem(BigInteger saleId, BigInteger productId, Integer quantity) {
        if (productId == null) {
            throw new BusinessException("Selecione um produto.");
        }
        Sale sale = findById(saleId);
        Product product = productService.findById(productId);
        sale.addItem(product, quantity);
        return saleRepository.save(sale);
    }

    @Transactional
    public Sale removeItem(BigInteger saleId, BigInteger itemId) {
        Sale sale = findById(saleId);
        sale.removeItem(itemId);
        return saleRepository.save(sale);
    }

    /**
     * Fecha o pedido e dá baixa no estoque de cada item, tudo na mesma transação:
     * se faltar estoque de qualquer item, nada é gravado.
     */
    @Transactional
    public Sale closeLocally(BigInteger saleId) {
        Sale sale = findById(saleId);
        sale.close();
        for (SaleItem item : sale.getItems()) {
            stockService.registerExit(item.getProduct().getId(), item.getQuantity());
        }
        return saleRepository.save(sale);
    }

    @Transactional
    public Sale markInvoiced(BigInteger saleId, String invoiceId) {
        Sale sale = findById(saleId);
        sale.markInvoiced(invoiceId);
        return saleRepository.save(sale);
    }

    @Transactional
    public Sale markInvoiceFailure(BigInteger saleId, String error) {
        Sale sale = findById(saleId);
        sale.markInvoiceFailure(error);
        return saleRepository.save(sale);
    }
}
