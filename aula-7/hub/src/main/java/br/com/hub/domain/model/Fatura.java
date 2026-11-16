package br.com.hub.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.hub.domain.exception.DomainException;

/**
 * Agregado central do hub. Não depende de framework nenhum: quem persiste,
 * emite boleto, nota ou e-mail são os adaptadores, através das portas.
 */
public class Fatura {

    private final UUID id;
    private final String pedidoId;
    private final Cliente cliente;
    private final List<ItemPedido> itens;
    private final BigDecimal valorTotal;
    private final LocalDateTime criadaEm;
    private StatusFatura status;
    private String chaveAcessoNotaFiscal;
    private String codigoBarrasBoleto;
    private String hashEmail;
    private LocalDateTime atualizadaEm;

    private Fatura(UUID id, String pedidoId, Cliente cliente, List<ItemPedido> itens, BigDecimal valorTotal,
            StatusFatura status, String chaveAcessoNotaFiscal, String codigoBarrasBoleto, String hashEmail,
            LocalDateTime criadaEm, LocalDateTime atualizadaEm) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.cliente = cliente;
        this.itens = List.copyOf(itens);
        this.valorTotal = valorTotal;
        this.status = status;
        this.chaveAcessoNotaFiscal = chaveAcessoNotaFiscal;
        this.codigoBarrasBoleto = codigoBarrasBoleto;
        this.hashEmail = hashEmail;
        this.criadaEm = criadaEm;
        this.atualizadaEm = atualizadaEm;
    }

    /** RF01: cria a fatura a partir do pedido recebido. */
    public static Fatura criar(Pedido pedido) {
        BigDecimal total = pedido.valorTotal();
        if (total.signum() <= 0) {
            throw new DomainException("O valor total do pedido deve ser maior que zero.");
        }
        LocalDateTime agora = LocalDateTime.now();
        return new Fatura(UUID.randomUUID(), pedido.pedidoId(), pedido.cliente(), pedido.itens(), total,
                StatusFatura.CRIADA, null, null, null, agora, agora);
    }

    /** Reconstrói uma fatura já persistida (usado pelo adaptador de persistência). */
    public static Fatura restaurar(UUID id, String pedidoId, Cliente cliente, List<ItemPedido> itens,
            BigDecimal valorTotal, StatusFatura status, String chaveAcessoNotaFiscal, String codigoBarrasBoleto,
            String hashEmail, LocalDateTime criadaEm, LocalDateTime atualizadaEm) {
        return new Fatura(id, pedidoId, cliente, itens, valorTotal, status, chaveAcessoNotaFiscal,
                codigoBarrasBoleto, hashEmail, criadaEm, atualizadaEm);
    }

    /** RF03 – restrição: a nota emitida gera uma chave de acesso de 44 dígitos, guardada na fatura. */
    public void registrarNotaFiscal(String chaveAcesso) {
        if (status != StatusFatura.CRIADA) {
            throw new DomainException("A nota fiscal só pode ser registrada em uma fatura recém-criada.");
        }
        if (chaveAcesso == null || !chaveAcesso.matches("\\d{44}")) {
            throw new DomainException("A chave de acesso da nota fiscal deve ter 44 dígitos.");
        }
        this.chaveAcessoNotaFiscal = chaveAcesso;
        tocar();
    }

    /** RF02: depois da nota, o boleto é solicitado por fila e a fatura passa a aguardar o retorno. */
    public void aguardarBoleto() {
        if (chaveAcessoNotaFiscal == null) {
            throw new DomainException("Emita a nota fiscal antes de solicitar o boleto.");
        }
        this.status = StatusFatura.AGUARDANDO_BOLETO;
        tocar();
    }

    /** RF02 – restrição: o boleto emitido gera um código de barras, guardado na fatura. */
    public void registrarBoleto(String codigoBarras) {
        if (status != StatusFatura.AGUARDANDO_BOLETO) {
            throw new DomainException("A fatura " + id + " não está aguardando boleto (status " + status + ").");
        }
        if (codigoBarras == null || !codigoBarras.matches("\\d{44}")) {
            throw new DomainException("O código de barras do boleto deve ter 44 dígitos.");
        }
        this.codigoBarrasBoleto = codigoBarras;
        tocar();
    }

    /** RF04 – restrição: o e-mail enviado gera um hash do conteúdo, guardado na fatura. */
    public void registrarNotificacao(String hashConteudo) {
        if (codigoBarrasBoleto == null) {
            throw new DomainException("O cliente só é notificado depois que o boleto é emitido.");
        }
        if (hashConteudo == null || !hashConteudo.matches("[0-9a-f]{64}")) {
            throw new DomainException("O hash do e-mail deve ser um SHA-256 em hexadecimal (64 caracteres).");
        }
        this.hashEmail = hashConteudo;
        this.status = StatusFatura.FATURADA;
        tocar();
    }

    private void tocar() {
        this.atualizadaEm = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getPedidoId() { return pedidoId; }
    public Cliente getCliente() { return cliente; }
    public List<ItemPedido> getItens() { return itens; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public StatusFatura getStatus() { return status; }
    public String getChaveAcessoNotaFiscal() { return chaveAcessoNotaFiscal; }
    public String getCodigoBarrasBoleto() { return codigoBarrasBoleto; }
    public String getHashEmail() { return hashEmail; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }
}
