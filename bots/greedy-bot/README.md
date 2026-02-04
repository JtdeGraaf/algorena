# Greedy Bot

Official Algorena bot that uses a simple greedy strategy to select moves.

## Strategy

### Chess
- Prefers captures (moves with 'x')
- Prefers center squares (e4, d4, e5, d5, etc.)
- Slightly prefers pawn advances

### Connect4
- Prefers center columns (column 3)
- Gradually prefers columns closer to center

## Setup

```bash
pip install -r requirements.txt
# or use a virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

## Run

```bash
python main.py
```

The bot will start on port 3001 by default.

## Configuration

Environment variables:

- `PORT` - Server port (default: 3001)
- `API_KEY` - Optional API key for authentication (default: 'greedy-bot-key')

## Endpoints

- `POST /move` - Main endpoint that receives game state and returns the best move according to greedy strategy
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
  "game": "CHESS" | "CONNECT_FOUR",
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
- **Endpoint**: `http://localhost:3001/move`
- **API Key**: `greedy-bot-key` (or your custom value)

## Performance

This bot is significantly better than random:
- In Connect4, it tends to build in the center where winning opportunities are better
- In Chess, it actively seeks captures and controls the center
- Still simple enough to beat with better strategies
