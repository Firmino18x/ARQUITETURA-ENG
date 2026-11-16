package br.com.mvc.model.entity;

import java.math.BigDecimal;
import java.math.BigInteger;

import br.com.mvc.model.enums.ProductStatus;
import br.com.mvc.model.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.INDISPONIVEL;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    /** Todo produto novo começa INDISPONÍVEL e sem estoque. */
    public void prepareForCreation() {
        this.id = null;
        this.status = ProductStatus.INDISPONIVEL;
        this.quantity = 0;
    }

    public boolean isDiscontinued() {
        return status == ProductStatus.DESCONTINUADO;
    }

    /** Entrada de estoque: se o saldo sai de zero, o produto fica DISPONÍVEL. */
    public void addStock(Integer amount) {
        ensureCanMoveStock(amount);
        boolean wasEmpty = quantity == 0;
        quantity += amount;
        if (wasEmpty) {
            status = ProductStatus.DISPONIVEL;
        }
    }

    /** Saída de estoque: se o saldo zera, o produto fica INDISPONÍVEL. */
    public void removeStock(Integer amount) {
        ensureCanMoveStock(amount);
        if (amount > quantity) {
            throw new BusinessException("Estoque insuficiente para o produto \"" + name
                    + "\". Disponível: " + quantity + ", solicitado: " + amount + ".");
        }
        quantity -= amount;
        if (quantity == 0) {
            status = ProductStatus.INDISPONIVEL;
        }
    }

    /** Descontinuar é um estado final. A checagem de perfil (só ADM) fica no service. */
    public void discontinue() {
        if (isDiscontinued()) {
            throw new BusinessException("O produto \"" + name + "\" já está descontinuado.");
        }
        status = ProductStatus.DESCONTINUADO;
    }

    private void ensureCanMoveStock(Integer amount) {
        if (isDiscontinued()) {
            throw new BusinessException("O produto \"" + name
                    + "\" está descontinuado e não pode ser movimentado no estoque.");
        }
        if (amount == null || amount <= 0) {
            throw new BusinessException("A quantidade deve ser maior que zero.");
        }
    }
}
