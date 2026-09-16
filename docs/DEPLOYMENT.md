# AI Newsroom Automation - Production Deployment Architecture

This document details the multi-tier deployment architecture for the AI Newsroom Automation platform, providing separation between Frontend, Backend Engine, Database, and Background Scheduler.

---

## 1. Architectural Topology

```
┌────────────────────────────────────────────────────────┐
│                   FRONTEND CLIENT                      │
│  - Jetpack Compose Android App / Web PWA               │
│  - Claymorphism Design System                          │
│  - Real-time Observability & Dispatch Controls         │
└──────────────────────────┬─────────────────────────────┘
                           │ HTTPS / gRPC / Local SQLite
┌──────────────────────────▼─────────────────────────────┐
│                 BACKEND SERVICE ENGINE                 │
│  - 14-Step Autonomous Publishing Pipeline              │
│  - Multi-LLM Provider Dynamic Routing & Fallback       │
│  - 12-Point Journalistic Quality Gate Engine           │
│  - SEO, Schema.org & Meta Tags Synthesizer             │
└───────┬──────────────────┬──────────────────────┬──────┘
        │                  │                      │
┌───────▼──────┐   ┌───────▼──────────┐   ┌───────▼──────┐
│  DATA STORE  │   │ BACKGROUND CRON  │   │  SYNDICATION │
│ - Room DB    │   │ - Android Worker │   │ - Blogger V3 │
│ - PostgreSQL │   │ - Cloud Run Job  │   │ - Facebook   │
│ - Migrations │   │ - Cron Schedule  │   │ - Webhooks   │
└──────────────┘   └──────────────────┘   └──────────────┘
```

---

## 2. Component Separation

### Tier 1: Frontend Client
- **Technology**: Jetpack Compose on modern Android runtime (API 26+).
- **Claymorphism System**: Soft 3D tactile elevation, pillowy dual-shadow cards, responsive layout for mobile phones and tablet viewports.
- **Responsiveness**: Replaces wide data tables with adaptive `ClayTableRow` cards on handheld screens, preventing horizontal overflow.

### Tier 2: Core Pipeline Engine
- **Decoupled Architecture**: Pipeline stages operate as pure functional modules (`TavilyService`, `LlmManager`, `DuplicateDetectionEngine`, `FactCheckingEngine`, `SeoEngine`, `QualityGateEngine`).
- **Resilience**: Every network interaction has automatic fallback routing, exponential backoff retries, and comprehensive error logging.

### Tier 3: Data Layer & Persistence
- **Storage**: SQLite via Room with Foreign Keys and cascade deletions.
- **Schema Parity**: SQL migration scripts in `database/migrations/` maintain synchronization with external PostgreSQL relational databases for server-side deployments.

### Tier 4: Background Scheduler
- **Mobile Runtime**: Android `WorkManager` with battery and network constraints.
- **Cloud Runtime**: GitHub Actions scheduled cron workflow (`.github/workflows/scheduled-pipeline.yml`) or Cloud Run jobs.

---

## 3. Environment Variables & Secrets Management

All secrets must be configured via environment variables or secret managers:
- Never commit `.env` containing real credentials.
- AI Studio securely injects `GEMINI_API_KEY` via `BuildConfig`.
- For production servers, inject secrets using Kubernetes Secrets, AWS SSM, or GCP Secret Manager.
