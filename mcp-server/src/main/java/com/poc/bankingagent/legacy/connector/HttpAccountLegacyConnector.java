package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.legacy.dto.AccountSummary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static com.poc.bankingagent.legacy.client.LegacyHttpClientSupport.mapException;

@Component
public class HttpAccountLegacyConnector implements AccountLegacyConnector {
    private final RestClient legacyRestClient;

    public HttpAccountLegacyConnector(RestClient legacyRestClient) {
        this.legacyRestClient = legacyRestClient;
    }

    @Override
    public AccountSummary getAccountSummary(String accountId) {
        try {
            return legacyRestClient.get()
                    .uri("/api/v1/legacy/accounts/{accountId}", accountId)
                    .retrieve()
                    .body(AccountSummary.class);
        } catch (RestClientException exception) {
            throw mapException("get account summary", exception);
        }
    }
}
