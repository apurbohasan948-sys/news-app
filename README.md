# AI-Powered Autonomous Newsroom Publishing Platform

A production-grade, autonomous news intelligence and publishing platform. The system orchestrates end-to-end journalistic research, multi-provider LLM synthesis, rigorous 12-point quality gate verification, SEO schema packaging, and multi-channel syndication (Google Blogger and Facebook Pages)—all wrapped in a Claymorphism design system.

---

## 🏛️ System Architecture

```
ai-news-automation/
├── app/                        # Android Jetpack Compose Application & Client
│   ├── src/main/java/com/example/
│   │   ├── data/               # Room Local Database, Entities, Repositories, Remote Clients
│   │   ├── engine/             # 14-Step Publishing Pipeline, SEO, Fact-Checking, Quality Gate
│   │   └── ui/                 # Claymorphism Component Library, Screens, ViewModels, Themes
├── database/                   # Canonical SQL Schema and Versioned Migrations
│   ├── schema.sql              # Master SQLite / PostgreSQL Schema
│   └── migrations/             # Incremental Migration DDL scripts
├── docs/                       # Technical & Architectural Documentation
│   ├── API.md                  # REST & Graph API specifications
│   ├── DEPLOYMENT.md           # Production Deployment & Service Separation Architecture
│   ├── ERROR_HANDLING.md       # Fault Tolerance, Retries, and Circuit Breaking
│   └── QUALITY_GATE.md         # 12-Point Journalistic Quality Gate Verification Matrix
├── .github/workflows/          # GitHub Actions CI/CD & Automation Workflows
│   ├── ci.yml                  # Build, Test, Lint, and Schema Validation
│   └── scheduled-pipeline.yml  # Autonomous Scheduled Pipeline Dispatcher
├── .env.example                # Environment Configuration Template
├── .gitignore                  # Production Security & Artifact Ignore Rules
└── README.md                   # System Documentation
```

---

## 🚀 8-Stage Visual Pipeline

The autonomous engine executes an 8-stage sequence for every editorial dispatch:

```
Research (Tavily API raw web search & multi-domain extraction)
   ↓
Writing (Structured journalistic synthesis & balanced narrative)
   ↓
Fact Check (Cross-domain claim corroboration & certainty scoring)
   ↓
SEO (Slug generation, meta descriptions <160 chars, JSON-LD Schema.org)
   ↓
Image (Editorial visual resolution, captions, and licensing metadata)
   ↓
Quality Gate (Strict 12-point automated audit; score must be ≥80/100)
   ↓
Blogger (Google Blogger REST API v3 publication)
   ↓
Facebook (Facebook Graph API page feed broadcast)
```

---

## 🎨 Claymorphism Design System

The entire user interface is built with an intentional Claymorphism aesthetic:
- **Soft 3D Tactile Cards**: Elevated with dual soft shadows and delicate light bevels.
- **Pressed-Depth Animations**: Buttons respond to physical touch with smooth spring animations.
- **Puffy Rounded Controls**: Inputs, toggles, badges, and progress indicators have generous 16–24dp corners.
- **Mobile-First & Accessible**: Touch targets meet 48dp standards; data tables adapt into responsive clay cards on mobile screens with zero horizontal overflow.
- **Soft Pastel Canvas**: Warm, eye-friendly backgrounds with high-contrast slate typography.

---

## 🧠 Multi-LLM Dynamic Routing & Fallback

Supports seamless failover across leading AI models:
1. **Google Gemini** (`gemini-2.5-flash`, `gemini-1.5-pro`)
2. **OpenAI** (`gpt-4o`, `gpt-4o-mini`)
3. **Anthropic Claude** (`claude-3-5-sonnet`, `claude-3-haiku`)
4. **Groq Cloud** (`llama-3.3-70b-versatile`)
5. **Mistral AI** (`mistral-large`, `pixtral`)
6. **Local Ollama / vLLM** endpoints

If the primary provider experiences rate limits (HTTP 429), timeouts, or service interruptions, the pipeline automatically fails over to the next priority provider without dropping the active editorial task.

---

## 🛡️ 12-Point Quality Gate Engine

Before any article is permitted to syndicate or publish, it must score **≥ 80/100** across 12 mandatory journalistic criteria:
1. **Sufficient Source Evidence** (Minimum 2–3 independent domains)
2. **Supported Claims** (Corroborated facts)
3. **Uncertainty Preservation** (Unresolved questions marked clearly)
4. **Original Synthesis** (Zero plagiarism)
5. **Deduplication Fingerprint** (SHA-256 semantic deduplication)
6. **Objective Headline** (Non-sensational, zero clickbait)
7. **Readability & Structure** (Executive summary, context, implications)
8. **Complete SEO Package** (Meta tags & JSON-LD `NewsArticle` schema)
9. **Source Attribution** (Citations and outbound links)
10. **Source Diversity** (Multi-source cross-verification)
11. **Visual Asset & Rights** (Verified alt-text & licensing)
12. **Speculation Boundary** (Clear division of facts and analysis)

Failing articles trigger up to 3 automated rewriting loops with targeted feedback before human review.

---

## ⚙️ Quickstart & Local Setup

### Prerequisites
- JDK 17+
- Android SDK (API 34 / Android 14) or Gradle 8.0+

### Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-org/ai-news-automation.git
   cd ai-news-automation
   ```

2. **Configure Environment Variables**:
   Copy `.env.example` to `.env` and provide your API keys:
   ```bash
   cp .env.example .env
   ```

3. **Build the Application**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run JVM & Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 🔒 Security Best Practices

- **Never commit `.env` or real API keys.** All secrets are loaded through environment variables or Android `BuildConfig`.
- Tokens stored in the database use masked previews in the UI and isolated storage.
- Keystores and local properties are strictly excluded in `.gitignore`.

---

## 📄 Documentation Index

- [API Specifications](docs/API.md)
- [Production Deployment Architecture](docs/DEPLOYMENT.md)
- [Error Handling & Circuit Breaking](docs/ERROR_HANDLING.md)
- [12-Point Quality Gate Specification](docs/QUALITY_GATE.md)
- [Canonical Database Schema](database/schema.sql)
