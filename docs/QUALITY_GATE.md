# 12-Point Journalistic Quality Gate Specification

Every article produced by the automated pipeline must pass a rigorous 12-point audit before publication.

---

## 1. Quality Check Matrix

| # | Check Rule | Target Criteria | Max Score |
| :-: | :--- | :--- | :-: |
| 1 | **Sufficient Evidence** | Minimum 2-3 verified independent external source citations | 10 |
| 2 | **Supported Claims** | Every core claim corroborated against Tavily research facts | 15 |
| 3 | **Uncertainty Preservation** | Conflicting reports or unverified details explicitly marked | 10 |
| 4 | **Original Synthesis** | Zero plagiarism; distinct journalistic structure and tone | 15 |
| 5 | **Deduplication Fingerprint** | SHA-256 semantic deduplication against past published stories | 5 |
| 6 | **Objective Headline** | Non-sensational, accurate, no clickbait or misleading hooks | 10 |
| 7 | **Readability & Flow** | Clear sections: summary, context, implications, takeaway | 10 |
| 8 | **SEO & Schema Package** | Valid slug, meta description (<160 chars), JSON-LD Article Schema | 10 |
| 9 | **Source Attribution** | Origin wire/journalistic sources linked and credited | 5 |
| 10 | **Source Diversity** | Cross-domain verification (no single-source dependency) | 5 |
| 11 | **Visual Asset & Rights** | Licensed imagery with verified editorial caption and alt-text | 5 |
| 12 | **Speculation Boundary** | Hard line between factual events and speculative opinion | 5 |
| **Total** | | **Comprehensive Journalistic Quality Index** | **100** |

---

## 2. Thresholds & Auto-Rewrite Rules

- **Pass Threshold**: Score ≥ 80 / 100.
- **Auto-Rewrite Loop**:
  - If score is between 50 and 79, the feedback generator identifies specifically failing checks.
  - The LLM is re-invoked with an explicit correction directive.
  - An updated version entry is saved in `article_versions`.
  - Max auto-rewrites: 3 attempts.
- **Rejection Policy**: If an article cannot achieve score ≥ 80 after 3 attempts, it is locked in `QUALITY_GATE_FAILED` status and routed to human editorial review.
