package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.legacy.dto.CardTransaction;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

import static com.poc.bankingagent.legacy.client.LegacyHttpClientSupport.mapException;

@Component
public class HttpCardLegacyConnector implements CardLegacyConnector {
    private final RestClient legacyRestClient;

    public HttpCardLegacyConnector(RestClient legacyRestClient) {
        this.legacyRestClient = legacyRestClient;
    }

    @Override
    public List<CardTransaction> searchTransactions(String cardId) {
        try {
            CardTransaction[] transactions = legacyRestClient.get()
                    .uri("/api/v1/legacy/cards/{cardId}/transactions", cardId)
                    .retrieve()
                    .body(CardTransaction[].class);
            return transactions == null ? List.of() : Arrays.asList(transactions);
        } catch (RestClientException exception) {
            throw mapException("search card transactions", exception);
        }
    }
}
