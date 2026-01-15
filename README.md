# Portfolio API - Backend

🚀 **Backend API** for Frédéric Klein's portfolio chatbot

Backend API powered by Quarkus, OpenAI, and RAG (Retrieval-Augmented Generation) for intelligent Q&A.

## Features

- 🤖 AI Chatbot: OpenAI GPT-4
- 🔍 RAG System: Vector search with semantic embeddings  
- 📊 CV Data: Structured professional experience
- 🌊 Streaming: Server-Sent Events (SSE)
- 🌐 Multilingual: Auto-detects French/English

## Quick Deploy to Railway

1. Fork this repo
2. Go to https://railway.app
3. New Project → Deploy from GitHub
4. Select this repo
5. Add env var: `OPENAI_API_KEY`
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
# Add your OPENAI_API_KEY to .env

# Run
./mvnw quarkus:dev
```

## Tech Stack

- Quarkus 3.30.6 (Java 21)
- OpenAI GPT-4 + Embeddings
- Vector Search (in-memory)

## Related

- Frontend: https://github.com/fklein82/fklein82.github.io

© 2026 Frédéric Klein
