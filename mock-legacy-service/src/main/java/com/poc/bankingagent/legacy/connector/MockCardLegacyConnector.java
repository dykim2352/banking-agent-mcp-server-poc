package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.common.NotFoundException;
import com.poc.bankingagent.legacy.dto.CardTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class MockCardLegacyConnector implements CardLegacyConnector {
    private final Map<String, List<CardTransaction>> transactions = Map.of(
            "CARD-1001", List.of(
                    new CardTransaction("TX-9001", "CARD-1001", LocalDateTime.now().minusDays(1), "Mock Retail Store", new BigDecimal("52000"), "KRW", "RETAIL", "APPROVED"),
                    new CardTransaction("TX-9002", "CARD-1001", LocalDateTime.now().minusHours(5), "Mock Online Mall", new BigDecimal("185000"), "KRW", "ONLINE", "APPROVED")
            )
    );

    @Override
    public List<CardTransaction> searchTransactions(String cardId) {
        List<CardTransaction> result = transactions.get(cardId);
        if (result == null) {
            throw new NotFoundException("Card transactions not found: " + cardId);
        }
        return result;
    }
}
