package com.poc.bankingagent.security;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super("ACCESS_DENIED: " + message);
    }
}
