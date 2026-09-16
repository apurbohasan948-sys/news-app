# AI Newsroom Automation - API Documentation

This document outlines the API specifications, payload schemas, authentication methods, and endpoint interactions utilized across the automated newsroom platform.

---

## 1. Tavily Research API

- **Endpoint**: `POST https://api.tavily.com/search`
- **Authentication**: Bearer token via `api_key` JSON parameter or HTTP header.
- **Request Payload**:
```json
{
  "api_key": "tvly-...",
  "query": "latest generative AI breakthrough",
  "search_depth": "advanced",
  "include_answer": true,
  "include_raw_content": false,
  "max_results": 7,
  "include_domains": []
}
```
- **Response Structure**:
```json
{
  "query": "latest generative AI breakthrough",
  "answer": "Summary of recent developments...",
  "results": [
    {
      "title": "Article Title",
      "url": "https://example.com/story",
      "content": "Extracted snippet...",
      "score": 0.94,
      "published_date": "2026-09-15"
    }
  ]
}
```

---

## 2. Multi-LLM Provider Interface

The platform routes synthesis and verification tasks across configured providers:

### Google Gemini API (v1beta)
- **Endpoint**: `POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={API_KEY}`
- **Models**: `gemini-2.5-flash`, `gemini-1.5-pro`
- **Capabilities**: Structured JSON extraction, journalistic synthesis, 12-point audit.

### OpenAI API (v1)
- **Endpoint**: `POST https://api.openai.com/v1/chat/completions`
- **Models**: `gpt-4o`, `gpt-4o-mini`

### Anthropic Claude API (v1)
- **Endpoint**: `POST https://api.anthropic.com/v1/messages`
- **Models**: `claude-3-5-sonnet-20241022`, `claude-3-haiku-20240307`

### Groq Cloud API
- **Endpoint**: `POST https://api.groq.com/openai/v1/chat/completions`
- **Models**: `llama-3.3-70b-versatile`, `mixtral-8x7b-32768`

---

## 3. Google Blogger REST API v3

- **Endpoint**: `POST https://www.googleapis.com/blogger/v3/blogs/{blogId}/posts/`
- **Authentication**: OAuth 2.0 Bearer token (`Authorization: Bearer <TOKEN>`)
- **Payload**:
```json
{
  "kind": "blogger#post",
  "title": "SEO Optimized Article Headline",
  "content": "<article>HTML formatted news content with citations and schema...</article>",
  "labels": ["Technology", "AI", "News"]
}
```

---

## 4. Facebook Graph API (v19.0+)

- **Endpoint**: `POST https://graph.facebook.com/v19.0/{pageId}/feed`
- **Authentication**: Page Access Token (`access_token` query parameter or Bearer header)
- **Payload**:
```json
{
  "message": "Breaking news dispatch summary with key takeaways #AI #Tech",
  "link": "https://published-article-url.com"
}
```
