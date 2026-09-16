package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.NewsCategory
import com.example.data.remote.BloggerService
import com.example.data.remote.FacebookService
import com.example.data.remote.ImageService
import com.example.data.remote.LlmManager
import com.example.data.remote.TavilyService
import com.example.engine.PublishingPipeline
import com.example.engine.SchedulerManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class NewsRepository(
    private val database: AppDatabase
) {
    val newsDao = database.newsDao()
    val llmManager = LlmManager(newsDao)
    val tavilyService = TavilyService(newsDao)
    val bloggerService = BloggerService(newsDao)
    val facebookService = FacebookService(newsDao)
    val imageService = ImageService(newsDao)

    val pipeline = PublishingPipeline(
        newsDao = newsDao,
        llmManager = llmManager,
        tavilyService = tavilyService,
        bloggerService = bloggerService,
        facebookService = facebookService,
        imageService = imageService
    )

    val schedulerManager = SchedulerManager(newsDao, pipeline)

    // Flow streams
    val allArticles: Flow<List<ArticleEntity>> = newsDao.getAllArticles()
    val totalArticlesCount: Flow<Int> = newsDao.getTotalArticlesCount()
    val publishedArticlesCount: Flow<Int> = newsDao.getPublishedArticlesCount()
    val draftArticlesCount: Flow<Int> = newsDao.getDraftArticlesCount()
    val failedArticlesCount: Flow<Int> = newsDao.getFailedArticlesCount()
    val qualityGateFailedCount: Flow<Int> = newsDao.getQualityGateFailedArticlesCount()
    val pendingQualityGateCount: Flow<Int> = newsDao.getPendingQualityGateCount()
    val averageQualityScore: Flow<Double?> = newsDao.getAverageQualityScore()
    val llmProviders: Flow<List<LlmProviderEntity>> = newsDao.getAllLlmProviders()
    val researchPackages: Flow<List<ResearchPackageEntity>> = newsDao.getAllResearchPackages()
    val allImages: Flow<List<ArticleImageEntity>> = newsDao.getAllImages()
    val scheduledJobs: Flow<List<ScheduledJobEntity>> = newsDao.getAllScheduledJobs()
    val bloggerPosts: Flow<List<BloggerPostEntity>> = newsDao.getAllBloggerPosts()
    val facebookPosts: Flow<List<FacebookPostEntity>> = newsDao.getAllFacebookPosts()
    val recentApiLogs: Flow<List<ApiLogEntity>> = newsDao.getRecentApiLogs()
    val recentErrorLogs: Flow<List<ErrorLogEntity>> = newsDao.getRecentErrorLogs()
    val tavilyUsageCount: Flow<Int> = newsDao.getTavilyUsageCount()
    val llmUsageCount: Flow<Int> = newsDao.getLlmUsageCount()
    val systemSettings: Flow<SystemSettingsEntity?> = newsDao.getSystemSettings()
    val pipelineProgress = pipeline.progress

    suspend fun initializeDefaultsIfNeeded() {
        val currentSettings = newsDao.getSystemSettingsOnce()
        if (currentSettings == null) {
            newsDao.saveSystemSettings(
                SystemSettingsEntity(
                    id = 1,
                    autoMode = false,
                    tavilyApiKeyEncrypted = "",
                    bloggerBlogId = "",
                    bloggerApiKeyEncrypted = "",
                    facebookPageId = "",
                    facebookTokenEncrypted = "",
                    defaultLanguage = "en",
                    dailyArticleLimit = 8,
                    qualityGateThreshold = 80,
                    maxCorrectionAttempts = 3,
                    publishIntervalMinutes = 120,
                    allowedHoursStart = 9,
                    allowedHoursEnd = 21,
                    timezone = "UTC",
                    enabledCategories = "Technology,AI,Science,Business,World"
                )
            )
        }

        val providers = newsDao.getAllLlmProviders().firstOrNull() ?: emptyList()
        if (providers.isEmpty()) {
            // Seed modular third-party LLM providers
            newsDao.insertLlmProvider(
                LlmProviderEntity(
                    name = "Groq High-Speed Llama 3.3",
                    apiKeyMasked = "gsk-••••••••4a91",
                    apiKeyEncrypted = "",
                    baseUrl = "https://api.groq.com/openai/v1",
                    modelName = "llama-3.3-70b-versatile",
                    isEnabled = true,
                    priority = 1,
                    maxRetries = 3,
                    assignedRole = "ARTICLE_WRITING",
                    lastStatusMessage = "Configured (Awaiting Key)",
                    lastStatusSuccess = true
                )
            )
            newsDao.insertLlmProvider(
                LlmProviderEntity(
                    name = "OpenRouter Multi-Model",
                    apiKeyMasked = "sk-or-••••••••99c2",
                    apiKeyEncrypted = "",
                    baseUrl = "https://openrouter.ai/api/v1",
                    modelName = "anthropic/claude-3.5-sonnet",
                    isEnabled = true,
                    priority = 2,
                    maxRetries = 3,
                    assignedRole = "FACT_CHECKING",
                    lastStatusMessage = "Configured (Awaiting Key)",
                    lastStatusSuccess = true
                )
            )
            newsDao.insertLlmProvider(
                LlmProviderEntity(
                    name = "OpenAI Direct",
                    apiKeyMasked = "sk-proj-••••••••118e",
                    apiKeyEncrypted = "",
                    baseUrl = "https://api.openai.com/v1",
                    modelName = "gpt-4o",
                    isEnabled = true,
                    priority = 3,
                    maxRetries = 3,
                    assignedRole = "ALL",
                    lastStatusMessage = "Configured (Awaiting Key)",
                    lastStatusSuccess = true
                )
            )
            newsDao.insertLlmProvider(
                LlmProviderEntity(
                    name = "Mistral AI Large",
                    apiKeyMasked = "mis-••••••••7b22",
                    apiKeyEncrypted = "",
                    baseUrl = "https://api.mistral.ai/v1",
                    modelName = "mistral-large-latest",
                    isEnabled = true,
                    priority = 4,
                    maxRetries = 3,
                    assignedRole = "REWRITING",
                    lastStatusMessage = "Configured (Awaiting Key)",
                    lastStatusSuccess = true
                )
            )
        }
    }

    suspend fun triggerPipelineRun(category: NewsCategory, manualTopic: String? = null): Result<ArticleEntity> {
        return pipeline.runFullPipeline(category, manualTopic)
    }

    suspend fun saveSettings(settings: SystemSettingsEntity) {
        newsDao.saveSystemSettings(settings)
    }

    suspend fun toggleAutoMode(enabled: Boolean) {
        val current = newsDao.getSystemSettingsOnce() ?: SystemSettingsEntity()
        newsDao.saveSystemSettings(current.copy(autoMode = enabled))
        if (enabled) {
            schedulerManager.startScheduler()
        } else {
            schedulerManager.stopScheduler()
        }
    }

    suspend fun testProvider(provider: LlmProviderEntity): Pair<Boolean, String> {
        return llmManager.testProviderConnection(provider)
    }

    suspend fun addOrUpdateProvider(provider: LlmProviderEntity) {
        if (provider.id == 0L) {
            newsDao.insertLlmProvider(provider)
        } else {
            newsDao.updateLlmProvider(provider)
        }
    }

    suspend fun deleteProvider(id: Long) {
        newsDao.deleteLlmProvider(id)
    }

    suspend fun getArticleWithDetails(articleId: Long): ArticleEntity? {
        return newsDao.getArticleById(articleId)
    }

    suspend fun updateArticle(article: ArticleEntity) {
        newsDao.updateArticle(article)
    }

    suspend fun deleteArticle(articleId: Long) {
        newsDao.deleteArticleById(articleId)
    }

    suspend fun testBloggerConnection(): Pair<Boolean, String> {
        val s = newsDao.getSystemSettingsOnce() ?: return Pair(false, "No settings found")
        return bloggerService.testConnection(s.bloggerBlogId, s.bloggerApiKeyEncrypted)
    }

    suspend fun testFacebookConnection(): Pair<Boolean, String> {
        val s = newsDao.getSystemSettingsOnce() ?: return Pair(false, "No settings found")
        return facebookService.testPageConnection(s.facebookPageId, s.facebookTokenEncrypted)
    }
}
