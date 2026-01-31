# Algorena - Bot Battle Arena

## Project Overview

Algorena is a competitive programming platform where developers can create bots to battle against each other in various games (currently Chess). The project follows a developer-first philosophy, inspired by Advent of Code, with clean APIs and detailed documentation.

## Technology Stack

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.x with Java 21
- **Database**: PostgreSQL with Flyway migrations
- **Authentication**: OAuth2 (Google) with JWT tokens
- **API Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven

### Frontend (React)
- **Runtime/Package Manager**: Bun
- **Build Tool**: Vite
- **Framework**: React 19 with TypeScript
- **Styling**: Tailwind CSS v4 (dark mode only)
- **Routing**: React Router v7
- **State Management**: TanStack Query (React Query)
- **i18n**: i18next (English only, ready for expansion)
- **UI Components**: Custom shadcn-style components
- **API Client**: Auto-generated from OpenAPI using @hey-api/openapi-ts
- **Icons**: Lucide React

## Project Structure

```
algorena-root/
├── docker-compose.yml
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/algorena/
│   │   ├── AlgorenaApplication.java
│   │   ├── bots/                    # Bot management domain
│   │   │   ├── controllers/
│   │   │   ├── application/
│   │   │   ├── domain/
│   │   │   └── dto/
│   │   ├── games/                   # Match/game engine domain
│   │   │   ├── controllers/
│   │   │   ├── application/
│   │   │   ├── domain/
│   │   │   ├── chess/engine/        # Chess game engine
│   │   │   └── dto/
│   │   ├── users/                   # User management domain
│   │   │   ├── controllers/
│   │   │   ├── application/
│   │   │   ├── domain/
│   │   │   └── dto/
│   │   ├── security/                # OAuth2, JWT, Security config
│   │   └── common/                  # Shared utilities, exceptions
│   └── src/main/resources/
│       ├── application.properties
│       └── db/migration/            # Flyway migrations
└── frontend/
    ├── package.json
    ├── vite.config.ts
    ├── openapi-ts.config.ts         # API client generation config
    ├── src/
    │   ├── App.tsx
    │   ├── main.tsx
    │   ├── index.css
    │   ├── api/
    │   │   ├── client.ts            # Token management & interceptors
    │   │   └── generated/           # Auto-generated API client
    │   ├── components/
    │   │   ├── Layout.tsx
    │   │   ├── Navbar.tsx
    │   │   └── ui/                  # Reusable UI components
    │   │       ├── button.tsx
    │   │       ├── card.tsx
    │   │       ├── dialog.tsx
    │   │       ├── input.tsx
    │   │       ├── label.tsx
    │   │       ├── select.tsx
    │   │       └── textarea.tsx
    │   ├── features/
    │   │   ├── auth/
    │   │   │   └── AuthContext.tsx  # OAuth2 handling, JWT storage
    │   │   ├── bots/
    │   │   │   ├── useBots.ts       # React Query hooks
    │   │   │   ├── BotCard.tsx
    │   │   │   ├── CreateBotDialog.tsx
    │   │   │   ├── EditBotDialog.tsx
    │   │   │   ├── DeleteBotDialog.tsx
    │   │   │   └── ApiKeysDialog.tsx
    │   │   └── matches/
    │   │       ├── useMatches.ts    # React Query hooks
    │   │       ├── MatchCard.tsx
    │   │       ├── CreateMatchDialog.tsx
    │   │       └── MatchDetailsDialog.tsx
    │   ├── i18n/
    │   │   ├── i18n.ts
    │   │   └── locales/en/translation.json
    │   ├── lib/
    │   │   ├── config.ts
    │   │   └── utils.ts
    │   └── routes/
    │       ├── HomePage.tsx
    │       ├── BotsPage.tsx
    │       ├── MatchesPage.tsx
    │       ├── LeaderboardPage.tsx
    │       ├── DocsPage.tsx
    │       ├── ProfilePage.tsx
    │       └── NotFoundPage.tsx
```

## Key Features

### Authentication
- OAuth2 login via Google
- JWT tokens stored in localStorage (not cookies)
- Token automatically attached to API requests via interceptor
- OAuth2 redirect flow: `/oauth2/authorization/google` → Google → `/oauth2/redirect?token=...`

### Bots Management
- Create, edit, delete bots
- Bot statistics (wins, losses, draws, win rate)
- API key management (generate, view, revoke)
- Bots can be active/inactive
- Currently only Chess game supported

### Matches
- Create matches between bots (own bot vs any opponent)
- View active and completed matches
- Filter by status and bot
- Match details with moves history
- Abort active matches
- Chess game state (FEN, PGN)

## API Endpoints

### Bots (`/api/v1/bots`)
- `GET /api/v1/bots` - List bots (with filters: userId, name, game, active)
- `POST /api/v1/bots` - Create bot
- `GET /api/v1/bots/{botId}` - Get bot
- `PUT /api/v1/bots/{botId}` - Update bot
- `DELETE /api/v1/bots/{botId}` - Delete bot
- `GET /api/v1/bots/{botId}/stats` - Get bot stats
- `GET /api/v1/bots/{botId}/api-keys` - List API keys
- `POST /api/v1/bots/{botId}/api-keys` - Create API key
- `DELETE /api/v1/bots/{botId}/api-keys/{apiKeyId}` - Revoke API key

### Matches (`/api/v1/matches`)
- `GET /api/v1/matches` - List matches (optional botId filter)
- `POST /api/v1/matches` - Create match
- `GET /api/v1/matches/{matchId}` - Get match
- `GET /api/v1/matches/{matchId}/moves` - Get match moves
- `POST /api/v1/matches/{matchId}/move` - Make a move
- `POST /api/v1/matches/{matchId}/abort` - Abort match
- `GET /api/v1/matches/recent` - Get recent matches

### Users (`/api/v1/users`)
- `GET /api/v1/users/me` - Get current user profile
- `PATCH /api/v1/users/me` - Update current user profile

## Key DTOs

```typescript
interface BotDto {
  id: number;
  name: string;
  description?: string;
  game: 'CHESS';
  active: boolean;
  created: string;
  lastUpdated: string;
}

interface MatchDto {
  id: string;
  game: 'CHESS';
  status: 'CREATED' | 'IN_PROGRESS' | 'FINISHED' | 'ABORTED';
  startedAt: string;
  finishedAt?: string;
  participants: MatchParticipantDto[];
  state?: ChessGameStateDto;
}

interface MatchParticipantDto {
  id: string;
  botId: number;
  botName: string;
  playerIndex: number;  // 0 = white, 1 = black
  score: number;
}

interface ChessGameStateDto {
  fen: string;
  pgn: string;
  halfMoveClock: number;
  fullMoveNumber: number;
}
```

## Development Commands

### Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
bun install
bun run dev          # Start dev server (port 5173)
bun run build        # Production build
bun run api:generate # Regenerate API client from OpenAPI
```

### Docker
```bash
docker-compose up -d  # Start PostgreSQL
```

## Configuration

### Frontend Environment
- `VITE_API_BASE_URL` - API base URL (empty by default, uses Vite proxy in dev)

### Backend Environment
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` - Database config
- `GOOGLE_OAUTH_CLIENT_ID`, `GOOGLE_OAUTH_CLIENT_SECRET` - OAuth2
- `BACKEND_URL` - Backend URL for OAuth2 redirect
- `app.frontend.url` - Frontend URL (default: http://localhost:5173)

## Vite Proxy Configuration

In development, Vite proxies these paths to the backend (localhost:8080):
- `/api` - API endpoints
- `/oauth2/authorization` - OAuth2 login initiation

Note: `/oauth2/redirect` is NOT proxied - it's handled by the frontend React Router.

## UI Design

- Dark mode only (zinc color palette)
- Accent color: Emerald green
- Font: System font stack with monospace for code
- Developer-focused aesthetic inspired by Advent of Code
- Responsive design with Tailwind CSS

