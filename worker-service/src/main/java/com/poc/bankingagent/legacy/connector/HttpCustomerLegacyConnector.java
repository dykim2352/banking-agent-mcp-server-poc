package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.legacy.dto.CreateTicketRequest;
import com.poc.bankingagent.legacy.dto.TicketResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static com.poc.bankingagent.legacy.client.LegacyHttpClientSupport.mapException;

@Component
public class HttpCustomerLegacyConnector implements CustomerLegacyConnector {
    private final RestClient legacyRestClient;

    public HttpCustomerLegacyConnector(RestClient legacyRestClient) {
        this.legacyRestClient = legacyRestClient;
    }

    @Override
    public TicketResponse createTicket(String customerId, String title, String description) {
        try {
            return legacyRestClient.post()
                    .uri("/api/v1/legacy/customers/{customerId}/tickets", customerId)
                    .body(new CreateTicketRequest(title, description))
                    .retrieve()
                    .body(TicketResponse.class);
        } catch (RestClientException exception) {
            throw mapException("create customer ticket", exception);
        }
    }
}
