# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Algorena is a competitive programming platform where developers create bots to battle in games (Chess, Connect4). Monorepo with separate backend (Spring Boot) and frontend (React).

## Commands

### Backend (from `backend/` directory)
```bash
./mvnw spring-boot:run              # Start dev server (port 8080)
./mvnw test                         # Run unit tests only
./mvnw verify                       # Run unit + integration tests
./mvnw test -Dtest=ClassName        # Run single test class
./mvnw test -Dtest=ClassName#method # Run single test method
```

### Frontend (from `frontend/` directory)
```bash
bun install                         # Install dependencies
bun run dev                         # Start dev server (port 5173)
bun run build                       # Production build
bun run lint                        # Run ESLint
bun run api:generate                # Regenerate API client from OpenAPI
```

### Docker
```bash
docker-compose up -d                # Start PostgreSQL (port 5432)
```

## Architecture

### Backend Structure (`backend/src/main/java/com/algorena/`)

Domain-driven design with three main domains:

- **bots/** - Bot management (CRUD, API keys, statistics)
- **games/** - Match engine with game-specific implementations
  - `chess/engine/` - Chess rules via chesslib
  - `connect4/engine/` - Connect4 implementation
- **users/** - User profiles and OAuth2 user data
- **security/** - OAuth2 (Google) + JWT authentication
- **common/** - Shared config, exceptions, base entities

Each domain follows: `controllers/` → `application/` (services) → `domain/` (entities) → `data/` (repositories)

### Frontend Structure (`frontend/src/`)

- **api/generated/** - Auto-generated from OpenAPI (run `bun run api:generate` after backend changes)
- **api/client.ts** - HTTP interceptor with JWT token attachment
- **features/** - Feature modules (auth, bots, matches) with React Query hooks
- **components/ui/** - Reusable shadcn-style components
- **routes/** - Page components

### Key Patterns

- **Authentication flow**: Google OAuth2 → Backend issues JWT → Frontend stores in localStorage → Attached via interceptor
- **API client generation**: Backend OpenAPI spec → `@hey-api/openapi-ts` → TypeScript client
- **Game engines**: Strategy pattern - `GameEngine` interface with `ChessGameEngine` and `Connect4GameEngine` implementations
- **Testing**: Unit tests (`*Test.java`) via Surefire, integration tests (`*IntegrationTest.java`) via Failsafe with Testcontainers

## Tech Stack

**Backend**: Java 25, Spring Boot 4.0.2, PostgreSQL, Flyway, NullAway (null-safety)
**Frontend**: React 19, TypeScript, Vite, Bun, Tailwind CSS v4, TanStack Query
**Game libraries**: chesslib (backend), chess.js (frontend replay)

## Development Notes

- Frontend uses Bun, not npm/yarn
- Dark mode only (zinc palette, emerald accent)
- Vite proxies `/api` and `/oauth2/authorization` to backend in dev
- Swagger UI at http://localhost:8080/swagger-ui.html
