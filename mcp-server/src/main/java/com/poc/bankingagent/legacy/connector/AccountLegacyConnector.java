package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.legacy.dto.AccountSummary;

public interface AccountLegacyConnector {
    AccountSummary getAccountSummary(String accountId);
}
