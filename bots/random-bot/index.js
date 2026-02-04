import express from 'express';

const app = express();
const PORT = process.env.PORT || 3000;
const API_KEY = process.env.API_KEY || 'random-bot-key';

app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', bot: 'random-bot' });
});

// Main bot endpoint - returns a random valid move
app.post('/move', (req, res) => {
  const apiKey = req.headers['x-algorena-api-key'];
  const matchId = req.headers['x-algorena-match-id'];

  // Validate API key if configured
  if (API_KEY && apiKey !== API_KEY) {
    return res.status(401).json({ error: 'Invalid API key' });
  }

  const { legalMoves, game, playerIndex, gameState } = req.body;

  // Validate request
  if (!legalMoves || !Array.isArray(legalMoves) || legalMoves.length === 0) {
    return res.status(400).json({ error: 'No legal moves provided' });
  }

  // Pick a random move from legal moves
  const randomIndex = Math.floor(Math.random() * legalMoves.length);
  const move = legalMoves[randomIndex];

  // Return the move in the expected format
  res.json({ move });
});
