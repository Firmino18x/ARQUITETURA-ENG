package br.com.hub.adapter.out.persistencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.hub.domain.model.StatusFatura;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * Modelo de banco da fatura. Fica no adaptador para o domínio
 * não conhecer anotações de JPA.
 */
@Entity
@Table(name = "fatura")
public class FaturaJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "pedido_id", nullable = false)
    private String pedidoId;

    @Column(name = "cliente_nome", nullable = false)
    private String clienteNome;

    @Column(name = "cliente_email", nullable = false)
    private String clienteEmail;

    @Column(name = "cliente_documento", nullable = false, length = 14)
    private String clienteDocumento;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "fatura_item", joinColumns = @JoinColumn(name = "fatura_id"))
    private List<ItemFaturaEmbeddable> itens = new ArrayList<>();

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusFatura status;

    @Column(name = "chave_acesso_nf", length = 44)
    private String chaveAcessoNotaFiscal;

    @Column(name = "codigo_barras_boleto", length = 44)
    private String codigoBarrasBoleto;

    @Column(name = "hash_email", length = 64)
    private String hashEmail;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    public List<ItemFaturaEmbeddable> getItens() { return itens; }
    public void setItens(List<ItemFaturaEmbeddable> itens) { this.itens = itens; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public StatusFatura getStatus() { return status; }
    public void setStatus(StatusFatura status) { this.status = status; }
    public String getChaveAcessoNotaFiscal() { return chaveAcessoNotaFiscal; }
    public void setChaveAcessoNotaFiscal(String chave) { this.chaveAcessoNotaFiscal = chave; }
    public String getCodigoBarrasBoleto() { return codigoBarrasBoleto; }
    public void setCodigoBarrasBoleto(String codigoBarrasBoleto) { this.codigoBarrasBoleto = codigoBarrasBoleto; }
    public String getHashEmail() { return hashEmail; }
    public void setHashEmail(String hashEmail) { this.hashEmail = hashEmail; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
    public void setAtualizadaEm(LocalDateTime atualizadaEm) { this.atualizadaEm = atualizadaEm; }
}
