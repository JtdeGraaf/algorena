package com.algorena.test.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * Test configuration to ensure Flyway bean is available in test context.
 * This is needed for Spring Boot 4.0 when using dynamic properties with Testcontainers.
 */
@SuppressWarnings("")
@TestConfiguration
public class TestFlywayConfig {

    /**
     * Explicitly create a Flyway bean for tests.
     * This ensures Flyway is available even when datasource is configured via @DynamicPropertySource.
     */
    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .cleanDisabled(false)
                .locations("classpath:db/migration")
                .load();
    }

}

