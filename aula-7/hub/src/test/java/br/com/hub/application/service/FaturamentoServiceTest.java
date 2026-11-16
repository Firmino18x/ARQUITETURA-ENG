package br.com.hub.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.hub.application.port.out.EmissorBoletoPort;
import br.com.hub.application.port.out.EmissorNotaFiscalPort;
import br.com.hub.application.port.out.FaturaRepositoryPort;
import br.com.hub.application.port.out.NotificadorClientePort;
import br.com.hub.domain.exception.DomainException;
import br.com.hub.domain.exception.FaturaNaoEncontradaException;
import br.com.hub.domain.model.Cliente;
import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.ItemPedido;
import br.com.hub.domain.model.Pedido;
import br.com.hub.domain.model.StatusFatura;

/**
 * Testa o núcleo sem Spring: as portas de saída são trocadas por fakes em
 * memória. É o ganho principal da arquitetura hexagonal.
 */
class FaturamentoServiceTest {

    private static final String CHAVE = "35261112345678000195550010000000011123456780";
    private static final String CODIGO_BARRAS = "00191100000000259001234567890123456789012345";
    private static final String HASH = "a".repeat(64);

    private final Map<UUID, Fatura> banco = new HashMap<>();
    private final List<UUID> boletosSolicitados = new ArrayList<>();
    private final List<UUID> emailsEnviados = new ArrayList<>();

    private final FaturaRepositoryPort repositorio = new FaturaRepositoryPort() {
        public Fatura salvar(Fatura f) { banco.put(f.getId(), f); return f; }
        public Optional<Fatura> buscarPorId(UUID id) { return Optional.ofNullable(banco.get(id)); }
    };
    private final EmissorNotaFiscalPort notaFiscal = fatura -> CHAVE;
    private final EmissorBoletoPort boleto = fatura -> boletosSolicitados.add(fatura.getId());
    private final NotificadorClientePort email = fatura -> { emailsEnviados.add(fatura.getId()); return HASH; };

    private final FaturamentoService service = new FaturamentoService(repositorio, notaFiscal, boleto, email);

    private Pedido pedido() {
        return new Pedido("PED-42",
                new Cliente("Dwight Schrute", "dwight@dundermifflin.com", "12345678000195"),
                List.of(new ItemPedido("Papel A4", 2, new BigDecimal("25.90")),
                        new ItemPedido("Grampeador", 1, new BigDecimal("40.00"))));
    }

    @Test
    void rf01_criaPersisteEDevolveId() {
        UUID id = service.faturar(pedido());

        Fatura fatura = banco.get(id);
        assertEquals("PED-42", fatura.getPedidoId());
        assertEquals(new BigDecimal("91.80"), fatura.getValorTotal());
    }

    @Test
    void rf02_rf03_emiteNotaEPedeBoletoPelaFila() {
        UUID id = service.faturar(pedido());

        Fatura fatura = banco.get(id);
        assertEquals(CHAVE, fatura.getChaveAcessoNotaFiscal());
        assertEquals(StatusFatura.AGUARDANDO_BOLETO, fatura.getStatus());
        assertEquals(List.of(id), boletosSolicitados);
        assertNull(fatura.getCodigoBarrasBoleto(), "o código de barras só chega pelo retorno da fila");
        assertTrue(emailsEnviados.isEmpty());
    }

    @Test
    void rf02_rf04_retornoDoBoletoGuardaCodigoENotificaCliente() {
        UUID id = service.faturar(pedido());

        service.registrarBoleto(id, CODIGO_BARRAS);

        Fatura fatura = banco.get(id);
        assertEquals(CODIGO_BARRAS, fatura.getCodigoBarrasBoleto());
        assertEquals(HASH, fatura.getHashEmail());
        assertEquals(StatusFatura.FATURADA, fatura.getStatus());
        assertEquals(List.of(id), emailsEnviados);
    }

    @Test
    void naoRegistraBoletoDuasVezes() {
        UUID id = service.faturar(pedido());
        service.registrarBoleto(id, CODIGO_BARRAS);

        assertThrows(DomainException.class, () -> service.registrarBoleto(id, CODIGO_BARRAS));
    }

    @Test
    void rejeitaChaveDeNotaInvalida() {
        FaturamentoService comChaveRuim = new FaturamentoService(repositorio, f -> "123", boleto, email);

        assertThrows(DomainException.class, () -> comChaveRuim.faturar(pedido()));
        assertTrue(boletosSolicitados.isEmpty());
    }

    @Test
    void faturaInexistente() {
        assertThrows(FaturaNaoEncontradaException.class, () -> service.consultar(UUID.randomUUID()));
    }

    @Test
    void pedidoSemItensEhRejeitado() {
        Cliente cliente = new Cliente("Pam", "pam@dundermifflin.com", "12345678909");
        assertThrows(DomainException.class, () -> new Pedido("PED-1", cliente, List.of()));
    }
}
