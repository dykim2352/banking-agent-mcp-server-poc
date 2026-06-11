package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.legacy.dto.TicketResponse;

public interface CustomerLegacyConnector {
    TicketResponse createTicket(String customerId, String title, String description);
}
