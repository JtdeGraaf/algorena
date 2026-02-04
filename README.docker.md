# Docker Setup for Algorena

## Quick Start

Start all services:
```bash
docker-compose up -d
```

View logs:
```bash
docker-compose logs -f
```

Stop all services:
```bash
docker-compose down
```

## Services

- **PostgreSQL**: `localhost:5432`
- **Backend**: `localhost:8080`
- **Frontend**: `localhost:5173`
- **Random Bot**: `localhost:3000`
- **Greedy Bot**: `localhost:3001`

## Hot Reload

### Frontend
✅ **Hot reload works!** Source files are mounted as volumes:
- Edit files in `frontend/src/` and see changes instantly
- Vite HMR (Hot Module Replacement) works in Docker

### Backend
⚠️ **Partial hot reload**: 
- Java source changes require Maven recompilation
- For full hot reload, add spring-boot-devtools dependency
- Or run backend locally with `./mvnw spring-boot:run`

### Bots
❌ **No hot reload**: Restart containers after changes:
```bash
docker-compose restart random-bot greedy-bot
```

## Development Workflow

### Option 1: All in Docker
```bash
docker-compose up -d
```

### Option 2: Mixed (recommended for development)
```bash
# Run database and bots in Docker
docker-compose up -d postgres random-bot greedy-bot

# Run backend and frontend locally for faster iteration
cd backend && ./mvnw spring-boot:run
cd frontend && bun run dev
```

## Registering Bots

When running in Docker, use these bot endpoints:

**If backend is in Docker:**
- Random Bot: `http://random-bot:3000/move`
- Greedy Bot: `http://greedy-bot:3001/move`

**If backend is local:**
- Random Bot: `http://localhost:3000/move`
- Greedy Bot: `http://localhost:3001/move`

## Troubleshooting

### Database connection issues
```bash
docker-compose logs postgres
docker-compose restart backend
```

### Frontend not updating
```bash
docker-compose restart frontend
```

### Rebuild after dependency changes
```bash
docker-compose build
docker-compose up -d
```
