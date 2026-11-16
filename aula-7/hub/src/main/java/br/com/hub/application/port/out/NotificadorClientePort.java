package br.com.hub.application.port.out;

import br.com.hub.domain.model.Fatura;

/** Porta de saída: notificação do cliente por e-mail (RF04), feita por SMTP. */
public interface NotificadorClientePort {

    /** @return hash SHA-256 (hex) do conteúdo do e-mail enviado */
    String notificar(Fatura fatura);
}
