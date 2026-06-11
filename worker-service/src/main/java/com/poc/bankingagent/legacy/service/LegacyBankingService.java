package com.poc.bankingagent.legacy.service;

import com.poc.bankingagent.legacy.connector.CustomerLegacyConnector;
import com.poc.bankingagent.legacy.dto.TicketResponse;
import org.springframework.stereotype.Service;

@Service
public class LegacyBankingService {
    private final CustomerLegacyConnector customerConnector;

    public LegacyBankingService(CustomerLegacyConnector customerConnector) {
        this.customerConnector = customerConnector;
    }

    public TicketResponse createTicket(String customerId, String title, String description) {
        return customerConnector.createTicket(customerId, title, description);
    }
}
