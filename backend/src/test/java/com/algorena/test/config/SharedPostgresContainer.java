package com.algorena.test.config;

import org.jspecify.annotations.Nullable;
import org.testcontainers.containers.PostgreSQLContainer;

public class SharedPostgresContainer extends PostgreSQLContainer<SharedPostgresContainer> {
    private static final String IMAGE_VERSION = "postgres:18";
    @Nullable
    private static SharedPostgresContainer container;

    private SharedPostgresContainer() {
        super(IMAGE_VERSION);
        withDatabaseName("test");
        withUsername("test");
        withPassword("test");
    }

    public static SharedPostgresContainer getInstance() {
        if (container == null) {
            container = new SharedPostgresContainer();
            container.start();
        }
        return container;
    }

    @Override
    public void start() {
        if (!isRunning()) {
            super.start();
        }
    }
}
