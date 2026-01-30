# Algorena

A competitive programming platform for bot battles.

## Project Structure

- `/backend` - Spring Boot REST API (Java 21)
- `/frontend` - Frontend application (to be created)

## Quick Start

### Using Docker Compose (Recommended)

```bash
docker-compose up
```

### Manual Setup

#### Backend
```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Documentation

- [Backend README](backend/README.md)
- [Frontend README](frontend/README.md)
- [Monorepo Structure Guide](MONOREPO_STRUCTURE.md)

## API Documentation

http://localhost:8080/swagger-ui.html

## License

See LICENSE and LICENSE-BSL files.
