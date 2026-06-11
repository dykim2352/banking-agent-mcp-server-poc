package com.poc.bankingagent.security;

public record AuthenticatedPrincipal(
        String userId,
        Role role,
        String authType
) {
}
