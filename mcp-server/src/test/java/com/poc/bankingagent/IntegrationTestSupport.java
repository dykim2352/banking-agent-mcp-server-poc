package com.poc.bankingagent;

import com.poc.bankingagent.audit.AuditEventStore;
import com.poc.bankingagent.legacy.connector.AccountLegacyConnector;
import com.poc.bankingagent.legacy.connector.CardLegacyConnector;
import com.poc.bankingagent.legacy.connector.CustomerLegacyConnector;
import com.poc.bankingagent.legacy.dto.AccountSummary;
import com.poc.bankingagent.legacy.dto.CardTransaction;
import com.poc.bankingagent.legacy.dto.CustomerProfile;
import com.poc.bankingagent.legacy.dto.LoanRecommendation;
import com.poc.bankingagent.legacy.dto.TicketResponse;
import com.poc.bankingagent.job.AgentJobStore;
import com.poc.bankingagent.kafka.KafkaDeadLetterStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTestSupport.LegacyConnectorTestConfig.class)
@TestPropertySource(properties = {
        "app.kafka.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:banking_agent;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true"
})
abstract class IntegrationTestSupport {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AuditEventStore auditStore;

    @Autowired
    KafkaDeadLetterStore deadLetterStore;

    @Autowired
    AgentJobStore jobStore;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void clearStores() {
        auditStore.clear();
        deadLetterStore.clear();
        jobStore.clear();
    }

    String initializeSpringAiMcpSession() throws Exception {
        return initializeSpringAiMcpSession("mock-user-key");
    }

    String initializeSpringAiMcpSession(String apiKey) throws Exception {
        MvcResult result = mockMvc.perform(post("/mcp/sdk")
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .header("X-Api-Key", apiKey)
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"sdk-init\",\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},\"clientInfo\":{\"name\":\"mockmvc-test\",\"version\":\"0.1.0\"}}}"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getHeader("Mcp-Session-Id");
    }

    @TestConfiguration
    static class LegacyConnectorTestConfig {
        @Bean
        @Primary
        AccountLegacyConnector testAccountLegacyConnector() {
            return accountId -> new AccountSummary(accountId, "CUST-1001", "DEPOSIT", "ACTIVE", new BigDecimal("12850000"), "KRW", List.of("NO_RECENT_RISK"));
        }

        @Bean
        @Primary
        CardLegacyConnector testCardLegacyConnector() {
            return cardId -> List.of(
                    new CardTransaction("TX-9001", cardId, LocalDateTime.now().minusDays(1), "Mock Retail Store", new BigDecimal("52000"), "KRW", "RETAIL", "APPROVED"),
                    new CardTransaction("TX-9002", cardId, LocalDateTime.now().minusHours(5), "Mock Online Mall", new BigDecimal("185000"), "KRW", "ONLINE", "APPROVED")
            );
        }

        @Bean
        @Primary
        CustomerLegacyConnector testCustomerLegacyConnector() {
            return new CustomerLegacyConnector() {
                @Override
                public CustomerProfile getCustomerProfile(String customerId) {
                    return new CustomerProfile(customerId, "Mock Customer A", "PRIME", "Digital Branch", List.of("ACCOUNT", "CARD", "LOAN"), List.of("CUSTOMER", "VIP"));
                }

                @Override
                public LoanRecommendation recommendLoanProducts(String customerId) {
                    return new LoanRecommendation(customerId, List.of("LN-MOCK-001", "LN-MOCK-002"), "Mock recommendation based on segment and consent scopes", "LOW");
                }

                @Override
                public TicketResponse createTicket(String customerId, String title, String description) {
                    return new TicketResponse("TCK-" + UUID.randomUUID().toString().substring(0, 8), customerId, title, "CREATED", Instant.now());
                }
            };
        }
    }
}
