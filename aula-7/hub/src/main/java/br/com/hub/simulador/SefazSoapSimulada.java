package br.com.hub.simulador;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Simula o web service SOAP de autorização de NF-e da SEFAZ.
 * Recebe o envelope e responde outro envelope com a chave de acesso.
 */
@Component
public class SefazSoapSimulada {

    private static final String CNPJ_EMITENTE = "12345678000195";

    private final AtomicLong numeroNota = new AtomicLong();

    public String nfeAutorizacao(String envelopeSoap) {
        if (envelopeSoap == null || !envelopeSoap.contains("<soapenv:Envelope")) {
            return resposta("225", "Rejeição: falha no schema XML", "");
        }
        String chave = ChaveAcessoNfe.gerar(CNPJ_EMITENTE, numeroNota.incrementAndGet(), LocalDate.now());
        return resposta("100", "Autorizado o uso da NF-e", "<chNFe>" + chave + "</chNFe>");
    }

    private String resposta(String cStat, String motivo, String conteudo) {
        return """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body>
                    <nfeResultMsg>
                      <protNFe><infProt><cStat>%s</cStat><xMotivo>%s</xMotivo>%s</infProt></protNFe>
                    </nfeResultMsg>
                  </soapenv:Body>
                </soapenv:Envelope>""".formatted(cStat, motivo, conteudo);
    }
}
