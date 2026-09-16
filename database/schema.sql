-- ==============================================================================
-- AI NEWSROOM AUTOMATION PLATFORM - CANONICAL DATABASE SCHEMA
-- Compatible with SQLite (Room) & PostgreSQL
-- ==============================================================================

-- 1. Admin Users & Permissions
CREATE TABLE IF NOT EXISTS admin_users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) DEFAULT 'NEWSROOM_ADMIN',
    created_at BIGINT NOT NULL
);

-- 2. Multi-LLM Provider Registry & Dynamic Fallback Routing
CREATE TABLE IF NOT EXISTS llm_providers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    api_key_masked VARCHAR(255) NOT NULL,
    api_key_encrypted TEXT NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    is_enabled BOOLEAN DEFAULT 1,
    priority INTEGER DEFAULT 1,
    max_retries INTEGER DEFAULT 3,
    timeout_seconds INTEGER DEFAULT 45,
    assigned_role VARCHAR(50) DEFAULT 'ALL',
    last_response_time_ms BIGINT DEFAULT 0,
    last_status_message TEXT DEFAULT 'Ready',
    last_status_success BOOLEAN DEFAULT 1
);

-- 3. Topic Discovery & Deduplication Fingerprints
CREATE TABLE IF NOT EXISTS topics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    topic_fingerprint VARCHAR(64) NOT NULL UNIQUE,
    angle TEXT DEFAULT '',
    discovered_at BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'DISCOVERED',
    similarity_score REAL DEFAULT 0.0
);

-- 4. Deep Research Packages
CREATE TABLE IF NOT EXISTS research_packages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    topic_id BIGINT NOT NULL,
    topic_title TEXT NOT NULL,
    search_queries_json TEXT NOT NULL,
    sources_json TEXT NOT NULL,
    important_facts_json TEXT NOT NULL,
    publication_dates_json TEXT NOT NULL,
    conflicting_info_json TEXT DEFAULT '',
    verified_info_json TEXT DEFAULT '',
    unverified_info_json TEXT DEFAULT '',
    researched_at BIGINT NOT NULL,
    sources_count INTEGER DEFAULT 0,
    FOREIGN KEY(topic_id) REFERENCES topics(id) ON DELETE CASCADE
);

-- 5. Verified Raw External Sources
CREATE TABLE IF NOT EXISTS sources (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    topic_id BIGINT NOT NULL,
    title TEXT NOT NULL,
    url TEXT NOT NULL,
    snippet TEXT NOT NULL,
    publication_date VARCHAR(50) DEFAULT '',
    relevance_score REAL DEFAULT 0.0,
    domain VARCHAR(100) NOT NULL,
    fetched_at BIGINT NOT NULL,
    FOREIGN KEY(topic_id) REFERENCES topics(id) ON DELETE CASCADE
);

-- 6. Synthesized Articles & Journalistic Dispatches
CREATE TABLE IF NOT EXISTS articles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    topic_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    language VARCHAR(10) DEFAULT 'en',
    seo_title TEXT NOT NULL,
    news_summary TEXT NOT NULL,
    main_article TEXT NOT NULL,
    important_facts TEXT NOT NULL,
    background_context TEXT DEFAULT '',
    why_it_matters TEXT DEFAULT '',
    latest_developments TEXT DEFAULT '',
    sources_list TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    quality_score INTEGER DEFAULT 0,
    fact_check_score INTEGER DEFAULT 0,
    originality_score INTEGER DEFAULT 0,
    seo_score INTEGER DEFAULT 0,
    rewrite_attempts INTEGER DEFAULT 0,
    published_url TEXT DEFAULT '',
    blogger_post_id TEXT DEFAULT '',
    facebook_post_id TEXT DEFAULT '',
    llm_provider_used VARCHAR(100) DEFAULT '',
    fallback_llm_used VARCHAR(100) DEFAULT '',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    published_at BIGINT DEFAULT 0,
    seo_slug TEXT DEFAULT '',
    seo_meta_description TEXT DEFAULT '',
    seo_keywords TEXT DEFAULT '',
    schema_json_ld TEXT DEFAULT ''
);

-- 7. Full Article Version History & Revisions
CREATE TABLE IF NOT EXISTS article_versions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    article_id BIGINT NOT NULL,
    version_number INTEGER NOT NULL,
    title TEXT NOT NULL,
    content_snippet TEXT NOT NULL,
    change_reason TEXT NOT NULL,
    quality_score INTEGER NOT NULL,
    created_at BIGINT NOT NULL,
    FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE
);

-- 8. 12-Point Quality Gate Audit Checks
CREATE TABLE IF NOT EXISTS quality_checks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    article_id BIGINT NOT NULL,
    check_type VARCHAR(100) NOT NULL,
    passed BOOLEAN NOT NULL,
    score INTEGER NOT NULL,
    max_score INTEGER NOT NULL,
    notes TEXT NOT NULL,
    checked_at BIGINT NOT NULL,
    FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE
);

-- 9. Licensed Editorial Visual Assets & Images
CREATE TABLE IF NOT EXISTS article_images (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    article_id BIGINT NOT NULL,
    image_url TEXT NOT NULL,
    alt_text TEXT NOT NULL,
    caption TEXT NOT NULL,
    aspect_ratio VARCHAR(20) DEFAULT '16:9',
    width INTEGER DEFAULT 1200,
    height INTEGER DEFAULT 675,
    source_attribution TEXT DEFAULT '',
    created_at BIGINT NOT NULL,
    FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE
);

-- 10. Automated Background Scheduler Jobs
CREATE TABLE IF NOT EXISTS scheduled_jobs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category VARCHAR(50) NOT NULL,
    interval_minutes INTEGER NOT NULL,
    last_run_at BIGINT DEFAULT 0,
    next_run_at BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT 1,
    failure_count INTEGER DEFAULT 0,
    last_error TEXT DEFAULT ''
);

-- 11. Syndication Records: Google Blogger REST V3
CREATE TABLE IF NOT EXISTS blogger_posts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    article_id BIGINT NOT NULL,
    blogger_post_id VARCHAR(100) NOT NULL,
    blogger_url TEXT NOT NULL,
    published_at BIGINT NOT NULL,
    title TEXT NOT NULL,
    FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE
);

-- 12. Syndication Records: Facebook Graph API
CREATE TABLE IF NOT EXISTS facebook_posts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    article_id BIGINT NOT NULL,
    facebook_post_id VARCHAR(100) NOT NULL,
    page_id VARCHAR(100) NOT NULL,
    post_text TEXT NOT NULL,
    article_link TEXT NOT NULL,
    published_at BIGINT NOT NULL,
    FOREIGN KEY(article_id) REFERENCES articles(id) ON DELETE CASCADE
);

-- 13. System Configuration & Operational Settings
CREATE TABLE IF NOT EXISTS system_settings (
    id INTEGER PRIMARY KEY DEFAULT 1,
    auto_mode BOOLEAN DEFAULT 0,
    publish_interval_minutes INTEGER DEFAULT 60,
    allowed_hours_start INTEGER DEFAULT 6,
    allowed_hours_end INTEGER DEFAULT 23,
    tavily_api_key TEXT DEFAULT '',
    gemini_api_key TEXT DEFAULT '',
    openai_api_key TEXT DEFAULT '',
    blogger_blog_id TEXT DEFAULT '',
    blogger_access_token TEXT DEFAULT '',
    facebook_page_id TEXT DEFAULT '',
    facebook_access_token TEXT DEFAULT '',
    quality_gate_threshold INTEGER DEFAULT 80,
    max_correction_attempts INTEGER DEFAULT 3,
    active_categories_json TEXT DEFAULT '["TECHNOLOGY","BUSINESS","SCIENCE"]'
);

-- 14. Observability: API Transaction Logs
CREATE TABLE IF NOT EXISTS api_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_name VARCHAR(100) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    status_code INTEGER NOT NULL,
    duration_ms BIGINT NOT NULL,
    request_summary TEXT DEFAULT '',
    response_summary TEXT DEFAULT '',
    is_success BOOLEAN NOT NULL,
    timestamp BIGINT NOT NULL
);

-- 15. Observability: System Error Audits
CREATE TABLE IF NOT EXISTS error_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    service_name VARCHAR(100) NOT NULL,
    error_type VARCHAR(100) NOT NULL,
    error_message TEXT NOT NULL,
    stack_trace TEXT DEFAULT '',
    context_data_json TEXT DEFAULT '',
    timestamp BIGINT NOT NULL
);

-- Indices for performance
CREATE INDEX IF NOT EXISTS idx_articles_status ON articles(status);
CREATE INDEX IF NOT EXISTS idx_articles_topic ON articles(topic_id);
CREATE INDEX IF NOT EXISTS idx_sources_topic ON sources(topic_id);
CREATE INDEX IF NOT EXISTS idx_quality_checks_article ON quality_checks(article_id);
CREATE INDEX IF NOT EXISTS idx_api_logs_timestamp ON api_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_error_logs_timestamp ON error_logs(timestamp);
