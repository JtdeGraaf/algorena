# Algorena

A competitive game arena for developers to build and compete with algorithmic bots.

## Overview

Algorena is infrastructure for algorithmic competition. Developers write bots that compete in structured matches, tournaments, and rankings through programmable APIs. The platform is engine-agnostic and designed to support multiple games over time.

## Technical Stack

- Java / Spring Boot
- PostgreSQL with Flyway migrations
- OAuth2 + JWT authentication
- Docker Compose for local development
- OpenAPI/Swagger documentation

## Getting Started

### Prerequisites
- Java 25+
- Docker & Docker Compose
- Maven

### Running Locally

1. Start the database:
   ```bash
   docker-compose up -d
   ```

2. Set required environment variables (Google OAuth credentials, JWT secret, database config)

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Access the API at `http://localhost:8080/swagger-ui.html`

## Architecture

The project follows clean architecture principles with domain-driven design:

- **Domain layer** - Core business entities and rules
- **Application layer** - Use cases and services
- **Infrastructure layer** - Database, security, external integrations
- **API layer** - REST controllers and DTOs

Authentication uses OAuth2 for login (Google) with JWT tokens for API access.

## Core Concepts

The platform is built around these key entities:

- **User** - Account that owns bots
- **Bot** - Programmable competitor
- **Game** - Ruleset (e.g., Chess)
- **Match** - Game instance between bots
- **Tournament** - Structured competition
- **Ranking** - ELO/Glicko ratings

## License

This project is dual-licensed:
- **BSL 1.1** - Business Source License (see LICENSE-BSL)
- Converts to **Apache 2.0** after change date (see LICENSE)

