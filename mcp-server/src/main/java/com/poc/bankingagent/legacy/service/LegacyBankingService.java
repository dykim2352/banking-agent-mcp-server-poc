package com.poc.bankingagent.legacy.service;

import com.poc.bankingagent.legacy.connector.AccountLegacyConnector;
import com.poc.bankingagent.legacy.connector.CardLegacyConnector;
import com.poc.bankingagent.legacy.connector.CustomerLegacyConnector;
import com.poc.bankingagent.legacy.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LegacyBankingService {
    private final AccountLegacyConnector accountConnector;
    private final CardLegacyConnector cardConnector;
    private final CustomerLegacyConnector customerConnector;

    public LegacyBankingService(AccountLegacyConnector accountConnector, CardLegacyConnector cardConnector, CustomerLegacyConnector customerConnector) {
        this.accountConnector = accountConnector;
        this.cardConnector = cardConnector;
        this.customerConnector = customerConnector;
    }

    public AccountSummary getAccountSummary(String accountId) {
        return accountConnector.getAccountSummary(accountId);
    }

    public List<CardTransaction> searchCardTransactions(String cardId) {
        return cardConnector.searchTransactions(cardId);
    }

    public CustomerProfile getCustomerProfile(String customerId) {
        return customerConnector.getCustomerProfile(customerId);
    }

    public LoanRecommendation recommendLoanProducts(String customerId) {
        return customerConnector.recommendLoanProducts(customerId);
    }

    public TicketResponse createTicket(String customerId, String title, String description) {
        return customerConnector.createTicket(customerId, title, description);
    }
}
