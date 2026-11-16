package br.com.hub.simulador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Simula um servidor SMTP: só registra no log o diálogo que seria trocado. */
@Component
public class ServidorSmtpSimulado {

    private static final Logger log = LoggerFactory.getLogger(ServidorSmtpSimulado.class);

    public void enviar(String remetente, String destinatario, String conteudo) {
        log.info("""
                [SMTP] diálogo simulado:
                > HELO hub.local
                < 250 smtp.simulado
                > MAIL FROM:<{}>
                < 250 OK
                > RCPT TO:<{}>
                < 250 OK
                > DATA
                {}
                > .
                < 250 Mensagem aceita para entrega
                > QUIT""", remetente, destinatario, conteudo);
    }
}
