package br.com.hub.adapter.out.smtp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import br.com.hub.application.port.out.NotificadorClientePort;
import br.com.hub.domain.model.Fatura;
import br.com.hub.simulador.ServidorSmtpSimulado;

/**
 * Adaptador de saída (RF04): monta a mensagem do e-mail, envia pelo servidor
 * SMTP simulado e devolve o SHA-256 do conteúdo enviado, que fica na fatura.
 */
@Component
public class EmailSmtpAdapter implements NotificadorClientePort {

    private static final String REMETENTE = "faturamento@hub.local";

    private final ServidorSmtpSimulado smtp;

    public EmailSmtpAdapter(ServidorSmtpSimulado smtp) {
        this.smtp = smtp;
    }

    @Override
    public String notificar(Fatura fatura) {
        String conteudo = montarMensagem(fatura);
        smtp.enviar(REMETENTE, fatura.getCliente().email(), conteudo);
        return sha256(conteudo);
    }

    private String montarMensagem(Fatura f) {
        return """
                From: Hub de Faturamento <%s>
                To: %s <%s>
                Subject: Fatura do pedido %s
                Content-Type: text/plain; charset=UTF-8

                Olá, %s!

                A fatura do seu pedido %s foi emitida.

                Fatura: %s
                Valor total: R$ %s
                Chave de acesso da NF-e: %s
                Código de barras do boleto: %s

                Obrigado pela compra.
                """.formatted(REMETENTE, f.getCliente().nome(), f.getCliente().email(), f.getPedidoId(),
                f.getCliente().nome(), f.getPedidoId(), f.getId(), f.getValorTotal(),
                f.getChaveAcessoNotaFiscal(), f.getCodigoBarrasBoleto());
    }

    static String sha256(String conteudo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(conteudo.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }
}
