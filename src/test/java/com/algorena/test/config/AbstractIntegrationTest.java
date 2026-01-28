package com.algorena.test.config;


import com.algorena.security.security.SimpleUserPrincipal;
import com.algorena.users.data.UserRepository;
import com.algorena.users.domain.Language;
import com.algorena.users.domain.Provider;
import com.algorena.users.domain.User;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

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
    protected UserRepository userRepository;

    @Autowired
    Flyway flyway;

    private static final SharedPostgresContainer POSTGRES = SharedPostgresContainer.getInstance();
    protected User testUser;

    @DynamicPropertySource
    public static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.clean-disabled", () -> "false");

        registry.add("testcontainers.reuse.enable", () -> "true");
    }


    @BeforeEach
    public void setupIntegrationTest() {
        // Reset DB before each test using Flyway clean/migrate to guarantee clean schema, and isolation of tests
        flyway.clean();
        flyway.migrate();

        // Create and authenticate a default test user
        this.testUser = createTestUser("testuser", "testuser@algorena.dev");
        authenticateAsUser(this.testUser);
    }

    // Helper: create and persist a User for tests
    protected User createTestUser(String username, String email) {
        User user = User.builder()
                .username(username)
                .email(email)
                .language(Language.EN)
                .provider(Provider.GOOGLE)
                .providerId(UUID.randomUUID().toString())
                .build();

        return userRepository.save(user);
    }

    // Helper: set SecurityContext authentication for a given user
    protected void authenticateAsUser(User user) {
        Long userId = user.getId();
        if (userId == null) {
            throw new IllegalArgumentException("User must be persisted before authentication");
        }
        var principal = new SimpleUserPrincipal(
                userId.toString(),
                user.getEmail(),
                user.getUsername(),
                Language.EN
        );
        TestingAuthenticationToken token = new TestingAuthenticationToken(principal, "credentials", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    // Helper: clear SecurityContext
    protected void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
