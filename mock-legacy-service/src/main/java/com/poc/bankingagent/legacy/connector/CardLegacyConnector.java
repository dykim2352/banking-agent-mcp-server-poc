package com.poc.bankingagent.legacy.connector;

import com.poc.bankingagent.legacy.dto.CardTransaction;

import java.util.List;

public interface CardLegacyConnector {
    List<CardTransaction> searchTransactions(String cardId);
}
