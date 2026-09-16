# AI Newsroom Automation - Error Handling & Recovery Protocols

## 1. Resilience & Reliability Philosophy

The platform is designed to operate autonomously without fatal interruptions. It implements multi-layered fault tolerance:
1. **Fallback Provider Switching**: If the primary LLM provider fails (HTTP 429, 500, or timeout), execution switches immediately to the next configured fallback provider in priority order.
2. **Quality Gate Auto-Correction**: If a generated article scores below the Quality Gate threshold (<80/100), the system formulates feedback and automatically re-prompts the LLM up to `maxCorrectionAttempts` (default: 3) times.
3. **Graceful Degradation**: If an external syndication channel (e.g., Facebook Graph API) experiences network or auth failure, the article remains saved in `DRAFT` status and an error log is generated without crashing the app.

---

## 2. Error Matrix & Recovery Procedures

| Component | Error Condition | Action Taken | Recovery Policy |
| :--- | :--- | :--- | :--- |
| **Tavily Research** | HTTP 401 / Invalid Key | Log critical error in `error_logs` | Notify user in UI; skip current cycle |
| **Tavily Research** | Rate Limit / HTTP 429 | Wait with exponential backoff | Retry 3 times; fallback to cached topics |
| **LLM Provider** | Timeout (>45s) | Mark provider degraded in memory | Route request to next priority LLM |
| **Quality Gate** | Score < 80 | Save version with change reason | Auto-rewrite focusing on failed checks |
| **Blogger API** | Token Expired (401) | Record failed syndication | Keep article as `APPROVED_PENDING_PUBLISH` |
| **Facebook API** | Graph API Error | Log error message with error code | Isolate social syndication; keep post in DB |

---

## 3. Observability & Auditing

Every external API transaction is audited in the `api_logs` table:
- Service Name, Endpoint, HTTP Status Code, Duration (ms).
- Summary of request and response payloads.
- Detailed error trace in `error_logs` table accessible via the **Logs & Audit** screen.
