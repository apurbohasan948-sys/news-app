package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "admin_users")
data class AdminUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val role: String = "NEWSROOM_ADMIN",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "llm_providers")
data class LlmProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val apiKeyMasked: String, // Masked representation for security
    val apiKeyEncrypted: String, // Stored encrypted/obfuscated credential
    val baseUrl: String,
    val modelName: String,
    val isEnabled: Boolean = true,
    val priority: Int = 1, // 1 is highest priority
    val maxRetries: Int = 3,
    val timeoutSeconds: Int = 45,
    val assignedRole: String = "ALL", // LlmJobRole name or ALL
    val lastResponseTimeMs: Long = 0,
    val lastStatusMessage: String = "Ready",
    val lastStatusSuccess: Boolean = true
)

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // NewsCategory
    val topicFingerprint: String, // Hash for deduplication
    val angle: String = "",
    val discoveredAt: Long = System.currentTimeMillis(),
    val status: String = "DISCOVERED", // DISCOVERED, RESEARCHED, PROCESSED, SKIPPED_DUPLICATE
    val similarityScore: Double = 0.0
)

@Entity(tableName = "research_packages")
data class ResearchPackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val topicTitle: String,
    val searchQueriesJson: String, // List<String> as JSON
    val sourcesJson: String, // List<SourceItem> as JSON
    val importantFactsJson: String, // List<String> as JSON
    val publicationDatesJson: String, // List<String> as JSON
    val conflictingInfoJson: String,
    val verifiedInfoJson: String,
    val unverifiedInfoJson: String,
    val researchedAt: Long = System.currentTimeMillis(),
    val sourcesCount: Int = 0
)

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val title: String,
    val url: String,
    val snippet: String,
    val publicationDate: String,
    val relevanceScore: Double,
    val domain: String,
    val fetchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: Long,
    val category: String,
    val language: String = "en",
    val seoTitle: String,
    val newsSummary: String,
    val mainArticle: String,
    val importantFacts: String, // JSON list or formatted bullets
    val backgroundContext: String,
    val whyItMatters: String,
    val latestDevelopments: String,
    val sourcesList: String, // JSON list of citations
    val status: String, // ArticleStatus name
    val qualityScore: Int = 0,
    val factCheckScore: Int = 0,
    val originalityScore: Int = 0,
    val seoScore: Int = 0,
    val rewriteAttempts: Int = 0,
    val publishedUrl: String = "",
    val bloggerPostId: String = "",
    val facebookPostId: String = "",
    val llmProviderUsed: String = "",
    val fallbackLlmUsed: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val publishedAt: Long = 0,
    val seoSlug: String = "",
    val seoMetaDescription: String = "",
    val seoKeywords: String = "",
    val schemaJsonLd: String = ""
)

@Entity(tableName = "article_versions")
data class ArticleVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val articleId: Long,
    val versionNumber: Int,
    val title: String,
    val contentSnippet: String,
    val changeReason: String, // e.g. "Initial generation", "Quality Gate Auto-Correction", "Fact Check Correction"
    val qualityScore: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quality_checks")
data class QualityCheckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val articleId: Long,
    val totalScore: Int,
    val researchScore: Int,
    val factAccuracyScore: Int,
    val originalityScore: Int,
    val writingQualityScore: Int,
    val seoScore: Int,
    val sourceQualityScore: Int,
    val imageScore: Int,
    val passed: Boolean,
    val problemsJson: String,
    val checkListJson: String,
    val checkedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "article_images")
data class ArticleImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val articleId: Long,
    val imageUrl: String,
    val concept: String,
    val altText: String,
    val caption: String,
    val licenseInfo: String,
    val provider: String,
    val width: Int,
    val height: Int,
    val seoFilename: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blogger_posts")
data class BloggerPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val articleId: Long,
    val bloggerPostId: String,
    val blogId: String,
    val postUrl: String,
    val title: String,
    val publishedAt: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS",
    val errorMessage: String = ""
)

@Entity(tableName = "facebook_posts")
data class FacebookPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val articleId: Long,
    val facebookPostId: String,
    val pageId: String,
    val postUrl: String,
    val headline: String,
    val publishTime: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS",
    val errorMessage: String = ""
)

@Entity(tableName = "scheduled_jobs")
data class ScheduledJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobName: String,
    val scheduledTime: Long,
    val executedTime: Long = 0,
    val status: String, // PENDING, RUNNING, COMPLETED, FAILED
    val category: String,
    val resultSummary: String = "",
    val errorDetails: String = ""
)

@Entity(tableName = "api_logs")
data class ApiLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceName: String, // TAVILY, OPENAI_COMPAT, BLOGGER, FACEBOOK
    val endpoint: String,
    val responseStatus: Int,
    val latencyMs: Long,
    val success: Boolean,
    val requestSummary: String,
    val responseSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "error_logs")
data class ErrorLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moduleName: String,
    val errorMessage: String,
    val retryCount: Int,
    val fallbackApplied: String = "",
    val stackTraceSnippet: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val autoMode: Boolean = false,
    val tavilyApiKeyEncrypted: String = "",
    val bloggerBlogId: String = "",
    val bloggerApiKeyEncrypted: String = "",
    val facebookPageId: String = "",
    val facebookTokenEncrypted: String = "",
    val defaultLanguage: String = "en",
    val dailyArticleLimit: Int = 10,
    val qualityGateThreshold: Int = 80,
    val maxCorrectionAttempts: Int = 3,
    val publishIntervalMinutes: Int = 120,
    val allowedHoursStart: Int = 8,
    val allowedHoursEnd: Int = 22,
    val timezone: String = "UTC",
    val enabledCategories: String = "Technology,AI,Science,Business,World",
    val bloggerAutoPublish: Boolean = true,
    val facebookAutoPublish: Boolean = true
) {
    val autoModeEnabled: Boolean get() = autoMode
    val tavilyApiKey: String get() = tavilyApiKeyEncrypted
    val bloggerApiKey: String get() = bloggerApiKeyEncrypted
    val facebookAccessToken: String get() = facebookTokenEncrypted
    val targetLanguage: String get() = defaultLanguage
    val schedulerIntervalMinutes: Int get() = publishIntervalMinutes
    val dailyPublishingLimit: Int get() = dailyArticleLimit
    val allowedPublishHoursStart: Int get() = allowedHoursStart
    val allowedPublishHoursEnd: Int get() = allowedHoursEnd
}
