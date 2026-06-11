package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.common.NotFoundException;
import com.poc.bankingagent.legacy.dto.AccountSummary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class MockAccountLegacyConnector implements AccountLegacyConnector {
    private final Map<String, AccountSummary> accounts = Map.of(
            "ACC-1001", new AccountSummary("ACC-1001", "CUST-1001", "DEPOSIT", "ACTIVE", new BigDecimal("12850000"), "KRW", List.of("NO_RECENT_RISK")),
            "ACC-2001", new AccountSummary("ACC-2001", "CUST-2001", "LOAN", "MONITORING", new BigDecimal("-45000000"), "KRW", List.of("HIGH_BALANCE_CHANGE", "MANUAL_REVIEW"))
    );

    @Override
    public AccountSummary getAccountSummary(String accountId) {
        AccountSummary summary = accounts.get(accountId);
        if (summary == null) {
            throw new NotFoundException("Account not found: " + accountId);
        }
        return summary;
    }
}
