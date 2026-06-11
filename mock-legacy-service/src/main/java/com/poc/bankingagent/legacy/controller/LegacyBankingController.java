package com.poc.bankingagent.legacy.controller;

import com.poc.bankingagent.legacy.dto.*;
import com.poc.bankingagent.legacy.service.LegacyBankingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/legacy")
public class LegacyBankingController {
    private final LegacyBankingService service;

    public LegacyBankingController(LegacyBankingService service) {
        this.service = service;
    }

    @GetMapping("/accounts/{accountId}")
    public AccountSummary accountSummary(@PathVariable String accountId) {
        return service.getAccountSummary(accountId);
    }

    @GetMapping("/cards/{cardId}/transactions")
    public List<CardTransaction> cardTransactions(@PathVariable String cardId) {
        return service.searchCardTransactions(cardId);
    }

    @GetMapping("/customers/{customerId}/profile")
    public CustomerProfile customerProfile(@PathVariable String customerId) {
        return service.getCustomerProfile(customerId);
    }

    @GetMapping("/customers/{customerId}/loan-recommendations")
    public LoanRecommendation loanRecommendation(@PathVariable String customerId) {
        return service.recommendLoanProducts(customerId);
    }

    @PostMapping("/customers/{customerId}/tickets")
    public TicketResponse createTicket(@PathVariable String customerId, @RequestBody CreateTicketRequest request) {
        return service.createTicket(customerId, request.title(), request.description());
    }
}
