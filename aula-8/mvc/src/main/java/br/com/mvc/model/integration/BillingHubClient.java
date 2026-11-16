package br.com.mvc.model.integration;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import br.com.mvc.model.entity.Sale;

/**
 * Cliente REST do hub de faturamento (aula 7). Traduz o pedido de venda para
 * o contrato do hub e devolve o ID da fatura criada.
 */
@Component
public class BillingHubClient {

    private final RestClient restClient;

    public BillingHubClient(@Value("${hub.url}") String hubUrl,
            @Value("${hub.timeout-ms:5000}") long timeoutMs) {
        Duration timeout = Duration.ofMillis(timeoutMs);
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(timeout);

        this.restClient = RestClient.builder()
                .baseUrl(hubUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public String createInvoice(Sale sale) {
        try {
            HubInvoiceResponse response = restClient.post()
                    .uri("/faturas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(HubInvoiceRequest.from(sale))
                    .retrieve()
                    .body(HubInvoiceResponse.class);

            if (response == null || response.id() == null || response.id().isBlank()) {
                throw new HubIntegrationException("O hub respondeu, mas não devolveu o ID da fatura.");
            }
            return response.id();
        } catch (RestClientResponseException e) {
            throw new HubIntegrationException("O hub recusou o pedido (HTTP " + e.getStatusCode().value() + "): "
                    + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            throw new HubIntegrationException("Hub de faturamento indisponível ou sem resposta no tempo limite: "
                    + e.getMessage(), e);
        } catch (RestClientException e) {
            throw new HubIntegrationException("Erro na chamada ao hub de faturamento: " + e.getMessage(), e);
        }
    }
}
