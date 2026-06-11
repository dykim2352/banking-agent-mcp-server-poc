package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.legacy.dto.CustomerProfile;
import com.poc.bankingagent.legacy.dto.LoanRecommendation;
import com.poc.bankingagent.legacy.dto.TicketResponse;

public interface CustomerLegacyConnector {
    CustomerProfile getCustomerProfile(String customerId);
    LoanRecommendation recommendLoanProducts(String customerId);
    TicketResponse createTicket(String customerId, String title, String description);
}
