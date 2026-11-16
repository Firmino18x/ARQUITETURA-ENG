package br.com.mvc.model.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.mvc.model.enums.ProductStatus;
import br.com.mvc.model.enums.SaleStatus;
import br.com.mvc.model.exception.BusinessException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Pedido de venda. Ao ser fechado, é enviado ao hub de faturamento. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sale")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    /** CPF ou CNPJ, só dígitos. */
    @Column(name = "customer_document", nullable = false, length = 14)
    private String customerDocument;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<SaleItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SaleStatus status = SaleStatus.ABERTA;

    /** ID da fatura devolvido pelo hub. */
    @Column(name = "invoice_id")
    private String invoiceId;

    /** Última mensagem de erro da integração com o hub. */
    @Column(name = "invoice_error", length = 1000)
    private String invoiceError;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /** Valida os dados do cliente e prepara um pedido novo. */
    public void prepareForCreation() {
        if (customerName == null || customerName.isBlank()) {
            throw new BusinessException("Informe o nome do cliente.");
        }
        if (customerEmail == null || !customerEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BusinessException("Informe um e-mail válido para o cliente.");
        }
        customerDocument = customerDocument == null ? "" : customerDocument.replaceAll("\\D", "");
        if (customerDocument.length() != 11 && customerDocument.length() != 14) {
            throw new BusinessException("O documento do cliente deve ser um CPF (11 dígitos) ou CNPJ (14 dígitos).");
        }
        id = null;
        items = new ArrayList<>();
        status = SaleStatus.ABERTA;
        invoiceId = null;
        invoiceError = null;
        createdAt = LocalDateTime.now();
        closedAt = null;
    }

    /** Adiciona um item ao pedido aberto; se o produto já estiver nele, soma a quantidade. */
    public void addItem(Product product, Integer quantity) {
        ensureOpen();
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("A quantidade deve ser maior que zero.");
        }
        if (product.getStatus() != ProductStatus.DISPONIVEL) {
            throw new BusinessException("O produto \"" + product.getName() + "\" não está disponível para venda.");
        }
        SaleItem existing = items.stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);
        int total = quantity + (existing == null ? 0 : existing.getQuantity());
        if (total > product.getQuantity()) {
            throw new BusinessException("Estoque insuficiente para \"" + product.getName()
                    + "\". Disponível: " + product.getQuantity() + ", no pedido: " + total + ".");
        }
        if (existing != null) {
            existing.setQuantity(total);
            return;
        }
        items.add(SaleItem.builder()
                .sale(this)
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .build());
    }

    public void removeItem(BigInteger itemId) {
        ensureOpen();
        items.removeIf(i -> i.getId() != null && i.getId().equals(itemId));
    }

    /** Fecha o pedido localmente. A baixa de estoque é feita pelo service. */
    public void close() {
        ensureOpen();
        if (items.isEmpty()) {
            throw new BusinessException("Adicione ao menos um item antes de fechar o pedido.");
        }
        status = SaleStatus.FECHADA;
        closedAt = LocalDateTime.now();
    }

    public void markInvoiced(String invoiceId) {
        if (status != SaleStatus.FECHADA && status != SaleStatus.FALHA_FATURAMENTO) {
            throw new BusinessException("Só um pedido fechado pode receber o ID da fatura.");
        }
        this.invoiceId = invoiceId;
        this.invoiceError = null;
        this.status = SaleStatus.FATURADA;
    }

    public void markInvoiceFailure(String error) {
        if (status != SaleStatus.FECHADA && status != SaleStatus.FALHA_FATURAMENTO) {
            throw new BusinessException("Só um pedido fechado pode registrar falha de faturamento.");
        }
        this.invoiceError = error;
        this.status = SaleStatus.FALHA_FATURAMENTO;
    }

    public boolean canSendToHub() {
        return status == SaleStatus.FECHADA || status == SaleStatus.FALHA_FATURAMENTO;
    }

    public boolean isOpen() {
        return status == SaleStatus.ABERTA;
    }

    public BigDecimal getTotal() {
        return items.stream().map(SaleItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Identificador do pedido enviado ao hub. */
    public String getExternalId() {
        return "VENDA-" + id;
    }

    private void ensureOpen() {
        if (!isOpen()) {
            throw new BusinessException("O pedido " + id + " não está mais aberto.");
        }
    }
}
