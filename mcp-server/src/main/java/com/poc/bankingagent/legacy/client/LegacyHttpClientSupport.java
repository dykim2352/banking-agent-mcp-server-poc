package com.poc.bankingagent.legacy.client;

import com.poc.bankingagent.common.NotFoundException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

public final class LegacyHttpClientSupport {
    private LegacyHttpClientSupport() {
    }

    public static RuntimeException mapException(String operation, RestClientException exception) {
        if (exception instanceof HttpClientErrorException.NotFound) {
            return new NotFoundException("Legacy resource not found during " + operation);
        }
        return new IllegalStateException("Legacy service call failed during " + operation, exception);
    }
}
