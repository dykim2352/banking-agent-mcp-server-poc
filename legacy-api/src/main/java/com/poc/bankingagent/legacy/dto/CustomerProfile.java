package com.poc.bankingagent.legacy.dto;

import java.util.List;

public record CustomerProfile(
        String customerId,
        String name,
        String segment,
        String assignedBranch,
        List<String> consentScopes,
        List<String> roles
) {}
