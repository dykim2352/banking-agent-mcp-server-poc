package com.poc.bankingagent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.bankingagent.job.AgentJobStore;
import com.poc.bankingagent.job.AgentJobWorker;
import com.poc.bankingagent.kafka.KafkaDeadLetterStore;
import com.poc.bankingagent.legacy.connector.CustomerLegacyConnector;
import com.poc.bankingagent.legacy.dto.TicketResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
@Import(IntegrationTestSupport.LegacyConnectorTestConfig.class)
@TestPropertySource(properties = {
        "app.kafka.enabled=false",
        "app.kafka.consumer.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:banking_agent_worker;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true"
})
abstract class IntegrationTestSupport {
    @Autowired
    AgentJobStore jobStore;

    @Autowired
    KafkaDeadLetterStore deadLetterStore;

    @Autowired
    AgentJobWorker jobWorker;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearStores() {
        deadLetterStore.clear();
        jobStore.clear();
    }

    @TestConfiguration
    static class LegacyConnectorTestConfig {
        @Bean
        @Primary
        CustomerLegacyConnector testCustomerLegacyConnector() {
            return new CustomerLegacyConnector() {
                @Override
                public TicketResponse createTicket(String customerId, String title, String description) {
                    return new TicketResponse("TCK-" + UUID.randomUUID().toString().substring(0, 8), customerId, title, "CREATED", Instant.now());
                }
            };
        }
    }
}
