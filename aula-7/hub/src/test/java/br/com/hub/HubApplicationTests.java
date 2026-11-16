package br.com.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.hub.application.port.in.ConsultarFaturaUseCase;
import br.com.hub.application.port.in.FaturarPedidoUseCase;
import br.com.hub.domain.model.Cliente;
import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.ItemPedido;
import br.com.hub.domain.model.Pedido;
import br.com.hub.domain.model.StatusFatura;

/** Fluxo completo com os adaptadores reais (simulados): JPA, fila, SOAP e SMTP. */
@SpringBootTest(properties = "hub.simulador.banco.atraso-ms=10")
class HubApplicationTests {

    @Autowired
    private FaturarPedidoUseCase faturarPedido;

    @Autowired
    private ConsultarFaturaUseCase consultarFatura;

    @Test
    void faturaPedidoDePontaAPonta() throws InterruptedException {
        Pedido pedido = new Pedido("PED-1",
                new Cliente("Michael Scott", "michael@dundermifflin.com", "123.456.789-09"),
                List.of(new ItemPedido("Papel A4", 10, new BigDecimal("25.90"))));

        UUID id = faturarPedido.faturar(pedido);
        assertNotNull(id);

        Fatura fatura = consultarFatura.consultar(id);
        for (int i = 0; i < 50 && fatura.getStatus() != StatusFatura.FATURADA; i++) {
            Thread.sleep(100);
            fatura = consultarFatura.consultar(id);
        }

        assertEquals(StatusFatura.FATURADA, fatura.getStatus());
        assertEquals(new BigDecimal("259.00"), fatura.getValorTotal());
        assertTrue(fatura.getChaveAcessoNotaFiscal().matches("\\d{44}"));
        assertTrue(fatura.getCodigoBarrasBoleto().matches("\\d{44}"));
        assertTrue(fatura.getHashEmail().matches("[0-9a-f]{64}"));
    }
}
