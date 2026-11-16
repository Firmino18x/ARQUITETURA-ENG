package br.com.hub.application.port.in;

import java.util.UUID;

import br.com.hub.domain.model.Fatura;

public interface ConsultarFaturaUseCase {

    Fatura consultar(UUID faturaId);
}
