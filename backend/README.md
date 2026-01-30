# Algorena Backend

Spring Boot REST API for the Algorena platform.

## Technologies

- Java 25
- Spring Boot 4.0.2
- PostgreSQL
- Flyway (Database Migrations)
- JWT Authentication

## Getting Started

### Prerequisites

- Java 25
- PostgreSQL (or use Docker)

### Running Locally

1. Start PostgreSQL (or use docker-compose from root)
2. Set environment variables (see .env.example in root)
3. Run the application:

```bash
./mvnw spring-boot:run
```

### Building

```bash
./mvnw clean package
```

### Running Tests

```bash
./mvnw test
```

## API Documentation

Once running, visit: http://localhost:8080/swagger-ui.html

## Database Migrations

Migrations are in `src/main/resources/db/migration/`
Flyway runs automatically on startup.

## Project Structure

```
src/
├── main/
│   ├── java/com/algorena/
│   │   ├── bots/           # Bot management
│   │   ├── games/          # Game engine & matches
│   │   ├── users/          # User management
│   │   ├── security/       # Authentication & authorization
│   │   └── common/         # Shared utilities
│   └── resources/
│       ├── application.properties
│       └── db/migration/   # Database migrations
└── test/
```
