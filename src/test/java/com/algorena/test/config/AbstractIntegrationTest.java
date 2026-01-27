package com.algorena.test.config;


import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Base class for integration tests that require a Postgres Testcontainer and common dynamic properties.
 * Extend this class in integration tests to inherit container lifecycle and dynamic property registration.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(TestFlywayConfig.class)
public abstract class AbstractIntegrationTest {
    @Autowired
    Flyway flyway;

    private static final SharedPostgresContainer POSTGRES = SharedPostgresContainer.getInstance();

    @DynamicPropertySource
    public static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.clean-disabled", () -> "false");

        registry.add("testcontainers.reuse.enable", () -> "true");
    }

    // Reset DB before each test using Flyway clean/migrate to guarantee clean schema, and isolation of tests
    @BeforeEach
    public void resetDatabase() {
        flyway.clean();
        flyway.migrate();
    }
}
