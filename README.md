# Portfolio API - Backend

🚀 **Backend API** for Frédéric Klein's portfolio chatbot

Backend API powered by Quarkus, Claude AI, OpenAI Embeddings, and RAG (Retrieval-Augmented Generation) for intelligent Q&A.

## Features

- 🤖 AI Chatbot: Claude Sonnet 4.5 (Anthropic)
- 🔍 RAG System: Vector search with OpenAI embeddings
- 📊 CV Data: Structured professional experience
- 🌊 Streaming: Server-Sent Events (SSE)
- 🌐 Multilingual: Auto-detects French/English

## Quick Deploy to Railway

1. Fork this repo
2. Go to https://railway.app
3. New Project → Deploy from GitHub
4. Select this repo
5. Add environment variables:
   - `ANTHROPIC_API_KEY` (get from https://console.anthropic.com/)
   - `OPENAI_API_KEY` (get from https://platform.openai.com/api-keys)
6. Deploy! ✨

Your API: `https://portfolio-api-production.up.railway.app`

## API Endpoints

- `POST /api/chat/stream` - Streaming chat (SSE)
- `GET /api/chat/health` - Health check
- `GET /api/profile` - Get CV data

## Local Development

```bash
# Setup
cp .env.example .env
# Add your ANTHROPIC_API_KEY and OPENAI_API_KEY to .env

# Run
./mvnw quarkus:dev
```

Access at: http://localhost:8080

## Tech Stack

- Quarkus 3.30.6 (Java 21)
- Claude Sonnet 4.5 (Anthropic)
- OpenAI Embeddings (text-embedding-3-small)
- Vector Search (in-memory)

## Related

- Frontend: https://github.com/fklein82/fklein82.github.io

© 2026 Frédéric Klein
