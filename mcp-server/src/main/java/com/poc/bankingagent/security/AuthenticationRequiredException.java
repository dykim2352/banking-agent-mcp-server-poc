package com.poc.bankingagent.security;

public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException(String message) {
        super("UNAUTHENTICATED: " + message);
    }
}
