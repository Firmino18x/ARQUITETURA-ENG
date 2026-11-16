package br.com.mvc.model.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.mvc.model.entity.Product;
import br.com.mvc.model.enums.Role;
import br.com.mvc.model.exception.BusinessException;
import br.com.mvc.model.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public Product create(Product product){
        product.prepareForCreation();
        return productRepository.save(product);
    }

    public List<Product> read(){
        return productRepository.findAll();
    }

    public Product findById(BigInteger id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Produto " + id + " não encontrado."));
    }

    @Transactional
    public Product discontinue(BigInteger id, Role role) {
        if (role != Role.ADM) {
            throw new BusinessException("Apenas o administrador (ADM) pode descontinuar um produto.");
        }
        Product product = findById(id);
        product.discontinue();
        return productRepository.save(product);
    }
}
