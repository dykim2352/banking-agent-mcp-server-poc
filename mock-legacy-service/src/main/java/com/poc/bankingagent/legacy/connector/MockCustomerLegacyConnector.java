package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.common.NotFoundException;
import com.poc.bankingagent.legacy.dto.CustomerProfile;
import com.poc.bankingagent.legacy.dto.LoanRecommendation;
import com.poc.bankingagent.legacy.dto.TicketResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MockCustomerLegacyConnector implements CustomerLegacyConnector {
    private final Map<String, CustomerProfile> customers = Map.of(
            "CUST-1001", new CustomerProfile("CUST-1001", "Mock Customer A", "PRIME", "Digital Branch", List.of("ACCOUNT", "CARD", "LOAN"), List.of("CUSTOMER", "VIP")),
            "CUST-2001", new CustomerProfile("CUST-2001", "Mock Customer B", "STANDARD", "City Branch", List.of("ACCOUNT", "CARD"), List.of("CUSTOMER"))
    );

    @Override
    public CustomerProfile getCustomerProfile(String customerId) {
        CustomerProfile profile = customers.get(customerId);
        if (profile == null) {
            throw new NotFoundException("Customer not found: " + customerId);
        }
        return profile;
    }

    @Override
    public LoanRecommendation recommendLoanProducts(String customerId) {
        if (!customers.containsKey(customerId)) {
            throw new NotFoundException("Customer not found: " + customerId);
        }
        return new LoanRecommendation(customerId, List.of("LN-MOCK-001", "LN-MOCK-002"), "Mock recommendation based on segment and consent scopes", "LOW");
    }

    @Override
    public TicketResponse createTicket(String customerId, String title, String description) {
        if (!customers.containsKey(customerId)) {
            throw new NotFoundException("Customer not found: " + customerId);
        }
        return new TicketResponse("TCK-" + UUID.randomUUID().toString().substring(0, 8), customerId, title, "CREATED", Instant.now());
    }
}
