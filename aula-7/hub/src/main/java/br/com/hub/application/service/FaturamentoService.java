package br.com.hub.application.service;

import java.util.UUID;

import br.com.hub.application.port.in.ConsultarFaturaUseCase;
import br.com.hub.application.port.in.FaturarPedidoUseCase;
import br.com.hub.application.port.in.RegistrarBoletoUseCase;
import br.com.hub.application.port.out.EmissorBoletoPort;
import br.com.hub.application.port.out.EmissorNotaFiscalPort;
import br.com.hub.application.port.out.FaturaRepositoryPort;
import br.com.hub.application.port.out.NotificadorClientePort;
import br.com.hub.domain.exception.FaturaNaoEncontradaException;
import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.Pedido;

/**
 * Núcleo da aplicação. Orquestra o faturamento conversando só com portas:
 * não conhece REST, JPA, fila, SOAP nem SMTP. É Java puro; o Spring só
 * instancia esta classe em config/BeanConfig.
 */
public class FaturamentoService implements FaturarPedidoUseCase, RegistrarBoletoUseCase, ConsultarFaturaUseCase {

    private final FaturaRepositoryPort faturaRepository;
    private final EmissorNotaFiscalPort emissorNotaFiscal;
    private final EmissorBoletoPort emissorBoleto;
    private final NotificadorClientePort notificadorCliente;

    public FaturamentoService(FaturaRepositoryPort faturaRepository, EmissorNotaFiscalPort emissorNotaFiscal,
            EmissorBoletoPort emissorBoleto, NotificadorClientePort notificadorCliente) {
        this.faturaRepository = faturaRepository;
        this.emissorNotaFiscal = emissorNotaFiscal;
        this.emissorBoleto = emissorBoleto;
        this.notificadorCliente = notificadorCliente;
    }

    @Override
    public UUID faturar(Pedido pedido) {
        // RF01: cria e persiste a fatura
        Fatura fatura = faturaRepository.salvar(Fatura.criar(pedido));

        // RF03: emite a nota fiscal (SOAP, síncrono) e guarda a chave de acesso
        fatura.registrarNotaFiscal(emissorNotaFiscal.emitir(fatura));

        // RF02: persiste ANTES de publicar na fila, para o retorno do boleto nunca
        // encontrar uma versão desatualizada da fatura
        fatura.aguardarBoleto();
        faturaRepository.salvar(fatura);
        emissorBoleto.solicitarEmissao(fatura);

        // RF01: devolve o ID à camada REST
        return fatura.getId();
    }

    @Override
    public void registrarBoleto(UUID faturaId, String codigoBarras) {
        Fatura fatura = consultar(faturaId);

        // RF02 (restrição): guarda o código de barras na fatura
        fatura.registrarBoleto(codigoBarras);

        // RF04: com nota e boleto prontos, notifica o cliente por e-mail e guarda o hash
        fatura.registrarNotificacao(notificadorCliente.notificar(fatura));

        faturaRepository.salvar(fatura);
    }

    @Override
    public Fatura consultar(UUID faturaId) {
        return faturaRepository.buscarPorId(faturaId)
                .orElseThrow(() -> new FaturaNaoEncontradaException(faturaId));
    }
}
