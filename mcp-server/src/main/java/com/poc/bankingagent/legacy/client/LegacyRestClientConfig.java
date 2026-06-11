package com.poc.bankingagent.legacy.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class LegacyRestClientConfig {
    @Bean
    RestClient legacyRestClient(RestClient.Builder builder,
                                @Value("${app.legacy.base-url:http://localhost:8081}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
