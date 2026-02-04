# Random Bot

Official Algorena bot that makes random valid moves.

## Setup

```bash
npm install
# or
bun install
```

## Run

```bash
npm start
# or
bun start
```

The bot will start on port 3000 by default.

## Configuration

Environment variables:

- `PORT` - Server port (default: 3000)
- `API_KEY` - Optional API key for authentication (default: 'random-bot-key')

## Endpoints

- `POST /move` - Main endpoint that receives game state and returns a random valid move
- `GET /health` - Health check endpoint

## Request Format

```json
POST /move
Headers:
  Content-Type: application/json
  X-Algorena-Match-ID: <uuid>
  X-Algorena-API-Key: <key> (optional)

Body:
{
  "matchId": "uuid",
  "game": "CHESS" | "CONNECT4",
  "playerIndex": 0 | 1,
  "gameState": { ... },
  "legalMoves": ["e2e4", "d2d4", ...]
}
```

## Response Format

```json
{
  "move": "e2e4"
}
```

## Registering with Backend

When creating the bot in the backend:
- **Endpoint**: `http://localhost:3000/move`
- **API Key**: `random-bot-key` (or your custom value)
