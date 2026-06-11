package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.legacy.dto.CreateTicketRequest;
import com.poc.bankingagent.legacy.dto.CustomerProfile;
import com.poc.bankingagent.legacy.dto.LoanRecommendation;
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
    public CustomerProfile getCustomerProfile(String customerId) {
        try {
            return legacyRestClient.get()
                    .uri("/api/v1/legacy/customers/{customerId}/profile", customerId)
                    .retrieve()
                    .body(CustomerProfile.class);
        } catch (RestClientException exception) {
            throw mapException("get customer profile", exception);
        }
    }

    @Override
    public LoanRecommendation recommendLoanProducts(String customerId) {
        try {
            return legacyRestClient.get()
                    .uri("/api/v1/legacy/customers/{customerId}/loan-recommendations", customerId)
                    .retrieve()
                    .body(LoanRecommendation.class);
        } catch (RestClientException exception) {
            throw mapException("recommend loan products", exception);
        }
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
