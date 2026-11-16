package br.com.hub.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.com.hub.domain.model.Fatura;

/** Porta de saída: persistência das faturas. */
public interface FaturaRepositoryPort {

    Fatura salvar(Fatura fatura);

    Optional<Fatura> buscarPorId(UUID id);
}
