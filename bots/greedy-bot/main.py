from fastapi import FastAPI, Request, HTTPException, Header
from typing import Optional
import os
import uvicorn

app = FastAPI(title="Algorena Greedy Bot")

API_KEY = os.getenv("API_KEY", "greedy-bot-key")


def score_chess_move(move: str) -> int:
    """Score a chess move based on simple heuristics."""
    score = 0

    # Prefer captures (contains 'x')
    if 'x' in move:
        score += 100

    # Prefer center squares (e4, d4, e5, d5, d3, e3, d6, e6)
    center_squares = ['e4', 'd4', 'e5', 'd5', 'e3', 'd3', 'e6', 'd6']
    for center in center_squares:
        if center in move:
            score += 50
            break

    # Slightly prefer pawn moves (longer moves like e2e4)
    if len(move) >= 4 and not 'x' in move:
        score += 10

    return score


def score_connect4_move(move: str) -> int:
    """Score a Connect4 move based on column preference (prefer center)."""
    try:
        column = int(move)
        # Prefer center column (3), then adjacent columns
        # Columns are typically 0-6, so center is 3
        distance_from_center = abs(column - 3)
        # Higher score for center, lower for edges
        return 100 - (distance_from_center * 20)
    except (ValueError, IndexError):
        return 0


def select_best_move(legal_moves: list[str], game: str) -> str:
    """Select the best move using simple greedy strategy."""
    if not legal_moves:
        raise ValueError("No legal moves provided")

    if len(legal_moves) == 1:
        return legal_moves[0]

    # Score all moves based on game type
    if game == "CHESS":
        scored_moves = [(move, score_chess_move(move)) for move in legal_moves]
    elif game == "CONNECT_FOUR":
        scored_moves = [(move, score_connect4_move(move)) for move in legal_moves]
    else:
        # Fallback: just return first move
        return legal_moves[0]

    # Sort by score (highest first) and return best move
    scored_moves.sort(key=lambda x: x[1], reverse=True)
    return scored_moves[0][0]


@app.get("/health")
async def health():
    """Health check endpoint."""
    return {"status": "ok", "bot": "greedy-bot"}


@app.post("/move")
async def make_move(
    request: Request,
    x_algorena_api_key: Optional[str] = Header(None),
    x_algorena_match_id: Optional[str] = Header(None)
):
    """Main bot endpoint - returns a move using greedy strategy."""

    # Validate API key if configured
    if API_KEY and x_algorena_api_key != API_KEY:
        raise HTTPException(status_code=401, detail="Invalid API key")

    body = await request.json()

    legal_moves = body.get("legalMoves", [])
    game = body.get("game", "")
    player_index = body.get("playerIndex", 0)

    # Validate request
    if not legal_moves or not isinstance(legal_moves, list):
        raise HTTPException(status_code=400, detail="No legal moves provided")

    # Select best move using greedy strategy
    try:
        move = select_best_move(legal_moves, game)
        return {"move": move}

    except Exception as e:
        print(f"Error selecting move: {e}")
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    port = int(os.getenv("PORT", 3001))
    if API_KEY:
        print(f"   API Key: {API_KEY}")

    uvicorn.run(app, host="0.0.0.0", port=port)
