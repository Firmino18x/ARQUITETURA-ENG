package br.com.hub.adapter.out.soap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import br.com.hub.application.port.out.EmissorNotaFiscalPort;
import br.com.hub.domain.exception.DomainException;
import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.ItemPedido;
import br.com.hub.simulador.SefazSoapSimulada;

/**
 * Adaptador de saída (RF03): monta o envelope SOAP da NF-e, "envia" para a
 * SEFAZ simulada e extrai a chave de acesso (44 dígitos) da resposta.
 */
@Component
public class NotaFiscalSoapAdapter implements EmissorNotaFiscalPort {

    private static final Logger log = LoggerFactory.getLogger(NotaFiscalSoapAdapter.class);
    private static final Pattern CHAVE = Pattern.compile("<chNFe>(\\d{44})</chNFe>");

    private final SefazSoapSimulada sefaz;

    public NotaFiscalSoapAdapter(SefazSoapSimulada sefaz) {
        this.sefaz = sefaz;
    }

    @Override
    public String emitir(Fatura fatura) {
        String envelope = montarEnvelope(fatura);
        log.info("[SOAP] enviando NF-e da fatura {}:\n{}", fatura.getId(), envelope);

        String resposta = sefaz.nfeAutorizacao(envelope);
        log.info("[SOAP] resposta da SEFAZ:\n{}", resposta);

        Matcher matcher = CHAVE.matcher(resposta);
        if (!matcher.find()) {
            throw new DomainException("A SEFAZ não devolveu a chave de acesso da nota fiscal.");
        }
        return matcher.group(1);
    }

    private String montarEnvelope(Fatura fatura) {
        StringBuilder xml = new StringBuilder()
                .append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"")
                .append(" xmlns:nfe=\"http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4\">\n")
                .append("  <soapenv:Header/>\n")
                .append("  <soapenv:Body>\n")
                .append("    <nfe:nfeDadosMsg>\n")
                .append("      <NFe><infNFe>\n")
                .append("        <ide><natOp>VENDA</natOp><xPed>").append(escapar(fatura.getPedidoId()))
                .append("</xPed></ide>\n")
                .append("        <dest><CPFouCNPJ>").append(fatura.getCliente().documento())
                .append("</CPFouCNPJ><xNome>").append(escapar(fatura.getCliente().nome()))
                .append("</xNome><email>").append(escapar(fatura.getCliente().email()))
                .append("</email></dest>\n");
        int n = 1;
        for (ItemPedido item : fatura.getItens()) {
            xml.append("        <det nItem=\"").append(n++).append("\"><prod><xProd>")
                    .append(escapar(item.descricao())).append("</xProd><qCom>").append(item.quantidade())
                    .append("</qCom><vUnCom>").append(item.valorUnitario()).append("</vUnCom></prod></det>\n");
        }
        return xml.append("        <total><vNF>").append(fatura.getValorTotal()).append("</vNF></total>\n")
                .append("      </infNFe></NFe>\n")
                .append("    </nfe:nfeDadosMsg>\n")
                .append("  </soapenv:Body>\n")
                .append("</soapenv:Envelope>")
                .toString();
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
