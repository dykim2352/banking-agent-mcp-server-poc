package com.poc.bankingagent.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class MockAuthenticationFilter extends OncePerRequestFilter {
    public static final String API_KEY_HEADER = "X-Api-Key";

    private static final Map<String, AuthenticatedPrincipal> API_KEYS = Map.of(
            "mock-user-key", new AuthenticatedPrincipal("mock-user", Role.USER, "API_KEY"),
            "mock-advisor-key", new AuthenticatedPrincipal("mock-advisor", Role.ADVISOR, "API_KEY"),
            "mock-admin-key", new AuthenticatedPrincipal("mock-admin", Role.ADMIN, "API_KEY"),
            "mock-user-token", new AuthenticatedPrincipal("mock-user", Role.USER, "BEARER"),
            "mock-advisor-token", new AuthenticatedPrincipal("mock-advisor", Role.ADVISOR, "BEARER"),
            "mock-admin-token", new AuthenticatedPrincipal("mock-admin", Role.ADMIN, "BEARER")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        AuthenticatedPrincipal principal = resolvePrincipal(request);
        if (principal != null) {
            AuthenticationContext.set(principal);
            MDC.put("userId", principal.userId());
            MDC.put("role", principal.role().name());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            AuthenticationContext.clear();
            MDC.remove("userId");
            MDC.remove("role");
        }
    }

    private AuthenticatedPrincipal resolvePrincipal(HttpServletRequest request) {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey != null && !apiKey.isBlank()) {
            return API_KEYS.get(apiKey);
        }

        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return API_KEYS.get(authorization.substring("Bearer ".length()));
        }

        return null;
    }
}
