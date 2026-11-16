package br.com.hub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.hub.application.port.out.EmissorBoletoPort;
import br.com.hub.application.port.out.EmissorNotaFiscalPort;
import br.com.hub.application.port.out.FaturaRepositoryPort;
import br.com.hub.application.port.out.NotificadorClientePort;
import br.com.hub.application.service.FaturamentoService;

/**
 * Único ponto onde o núcleo encontra o Spring: aqui as portas de saída
 * recebem seus adaptadores. Trocar um adaptador simulado por um real
 * não exige mudar nada no domínio nem no service.
 */
@Configuration
public class BeanConfig {

    @Bean
    public FaturamentoService faturamentoService(FaturaRepositoryPort faturaRepository,
            EmissorNotaFiscalPort emissorNotaFiscal, EmissorBoletoPort emissorBoleto,
            NotificadorClientePort notificadorCliente) {
        return new FaturamentoService(faturaRepository, emissorNotaFiscal, emissorBoleto, notificadorCliente);
    }
}
