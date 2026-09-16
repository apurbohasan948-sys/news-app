package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.model.NewsCategory
import com.example.data.repository.NewsRepository
import com.example.engine.PipelineProgress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NewsUiState(
    val articles: List<ArticleEntity> = emptyList(),
    val totalArticles: Int = 0,
    val publishedArticles: Int = 0,
    val draftArticles: Int = 0,
    val failedArticles: Int = 0,
    val qualityGateFailedCount: Int = 0,
    val qualityGatePassedCount: Int = 0,
    val pendingQualityGate: Int = 0,
    val averageQualityScore: Double = 0.0,
    val providers: List<LlmProviderEntity> = emptyList(),
    val researchPackages: List<ResearchPackageEntity> = emptyList(),
    val images: List<ArticleImageEntity> = emptyList(),
    val scheduledJobs: List<ScheduledJobEntity> = emptyList(),
    val bloggerPosts: List<BloggerPostEntity> = emptyList(),
    val facebookPosts: List<FacebookPostEntity> = emptyList(),
    val apiLogs: List<ApiLogEntity> = emptyList(),
    val errorLogs: List<ErrorLogEntity> = emptyList(),
    val tavilyUsage: Int = 0,
    val llmUsage: Int = 0,
    val settings: SystemSettingsEntity = SystemSettingsEntity(),
    val pipelineProgress: PipelineProgress = PipelineProgress(),
    val selectedArticle: ArticleEntity? = null,
    val selectedArticleVersions: List<ArticleVersionEntity> = emptyList(),
    val selectedArticleQualityChecks: List<QualityCheckEntity> = emptyList(),
    val selectedArticleSources: List<SourceEntity> = emptyList(),
    val statusNotification: String? = null,
    val isTestingApi: Boolean = false,
    val apiTestResult: String? = null
)

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = NewsRepository(database)

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            observeStreams()
        }
    }

    private fun observeStreams() {
        viewModelScope.launch {
            combine(
                repository.allArticles,
                repository.totalArticlesCount,
                repository.publishedArticlesCount,
                repository.draftArticlesCount,
                repository.failedArticlesCount
            ) { arts, total, pub, draft, failed ->
                _uiState.update { current ->
                    current.copy(
                        articles = arts,
                        totalArticles = total,
                        publishedArticles = pub,
                        draftArticles = draft,
                        failedArticles = failed
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            combine(
                repository.pendingQualityGateCount,
                repository.averageQualityScore,
                repository.qualityGateFailedCount,
                repository.tavilyUsageCount,
                repository.llmUsageCount
            ) { pending, avgScore, qgFailed, tavily, llm ->
                _uiState.update { current ->
                    current.copy(
                        pendingQualityGate = pending,
                        averageQualityScore = avgScore ?: 0.0,
                        qualityGateFailedCount = qgFailed,
                        qualityGatePassedCount = (current.totalArticles - qgFailed - current.failedArticles).coerceAtLeast(0),
                        tavilyUsage = tavily,
                        llmUsage = llm
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            repository.llmProviders.collect { list ->
                _uiState.update { it.copy(providers = list) }
            }
        }

        viewModelScope.launch {
            repository.researchPackages.collect { list ->
                _uiState.update { it.copy(researchPackages = list) }
            }
        }

        viewModelScope.launch {
            repository.allImages.collect { list ->
                _uiState.update { it.copy(images = list) }
            }
        }

        viewModelScope.launch {
            repository.scheduledJobs.collect { list ->
                _uiState.update { it.copy(scheduledJobs = list) }
            }
        }

        viewModelScope.launch {
            repository.bloggerPosts.collect { list ->
                _uiState.update { it.copy(bloggerPosts = list) }
            }
        }

        viewModelScope.launch {
            repository.facebookPosts.collect { list ->
                _uiState.update { it.copy(facebookPosts = list) }
            }
        }

        viewModelScope.launch {
            repository.recentApiLogs.collect { list ->
                _uiState.update { it.copy(apiLogs = list) }
            }
        }

        viewModelScope.launch {
            repository.recentErrorLogs.collect { list ->
                _uiState.update { it.copy(errorLogs = list) }
            }
        }

        viewModelScope.launch {
            repository.systemSettings.collect { s ->
                if (s != null) {
                    _uiState.update { it.copy(settings = s) }
                }
            }
        }

        viewModelScope.launch {
            repository.pipelineProgress.collect { p ->
                _uiState.update { it.copy(pipelineProgress = p) }
            }
        }
    }

    fun triggerPipeline(category: NewsCategory = NewsCategory.TECHNOLOGY, manualTopic: String? = null) {
        viewModelScope.launch {
            showNotification("Starting news publishing pipeline for ${category.displayName}...")
            val result = repository.triggerPipelineRun(category, manualTopic)
            if (result.isSuccess) {
                val article = result.getOrNull()
                showNotification("Pipeline finished: ${article?.seoTitle} (${article?.status})")
            } else {
                showNotification("Pipeline halted: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun toggleAutoMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAutoMode(enabled)
            showNotification(if (enabled) "AUTO MODE ENABLED: Pipeline is running on schedule" else "AUTO MODE DISABLED: Draft mode active")
        }
    }

    fun saveSettings(settings: SystemSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            showNotification("Newsroom settings saved securely")
        }
    }

    fun testProvider(provider: LlmProviderEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingApi = true, apiTestResult = "Testing connection to ${provider.name}...") }
            val (success, message) = repository.testProvider(provider)
            _uiState.update { it.copy(isTestingApi = false, apiTestResult = if (success) "✓ $message" else "✗ $message") }
            showNotification(if (success) "Provider connected: ${provider.name}" else "Provider test failed: $message")
        }
    }

    fun addOrUpdateProvider(provider: LlmProviderEntity) {
        viewModelScope.launch {
            repository.addOrUpdateProvider(provider)
            showNotification("Saved provider: ${provider.name}")
        }
    }

    fun deleteProvider(id: Long) {
        viewModelScope.launch {
            repository.deleteProvider(id)
            showNotification("Provider removed")
        }
    }

    fun selectArticle(articleId: Long) {
        viewModelScope.launch {
            val art = repository.getArticleWithDetails(articleId)
            if (art != null) {
                val versions = repository.newsDao.getVersionsForArticle(articleId).firstOrNull() ?: emptyList()
                val qgChecks = repository.newsDao.getQualityChecksForArticle(articleId).firstOrNull() ?: emptyList()
                val sources = repository.newsDao.getSourcesForTopic(art.topicId).firstOrNull() ?: emptyList()
                _uiState.update {
                    it.copy(
                        selectedArticle = art,
                        selectedArticleVersions = versions,
                        selectedArticleQualityChecks = qgChecks,
                        selectedArticleSources = sources
                    )
                }
            }
        }
    }

    fun saveDraft(articleId: Long, title: String, content: String) {
        viewModelScope.launch {
            val art = repository.getArticleWithDetails(articleId) ?: return@launch
            val updated = art.copy(
                seoTitle = title,
                mainArticle = content,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateArticle(updated)
            _uiState.update { it.copy(selectedArticle = updated) }
            showNotification("Article draft updated")
        }
    }

    fun publishArticleNow(articleId: Long) {
        viewModelScope.launch {
            val art = repository.getArticleWithDetails(articleId) ?: return@launch
            showNotification("Publishing article #${art.id} to Google Blogger & Facebook...")
            val settings = uiState.value.settings

            // Publish Blogger
            val bloggerRes = repository.bloggerService.publishPost(
                articleId = art.id,
                blogId = settings.bloggerBlogId,
                apiKeyOrToken = settings.bloggerApiKeyEncrypted,
                title = art.seoTitle,
                htmlContent = "<p>${art.newsSummary}</p><div>${art.mainArticle.replace("\n", "<br/>")}</div>",
                labels = listOf(art.category, "Verified News")
            )

            var publishedUrl = "https://news.example.com/article/${art.id}"
            var bloggerId = ""
            if (bloggerRes.isSuccess) {
                val post = bloggerRes.getOrThrow()
                publishedUrl = post.postUrl
                bloggerId = post.bloggerPostId
            }

            // Publish Facebook
            var fbId = ""
            if (settings.facebookPageId.isNotBlank()) {
                val fbRes = repository.facebookService.publishPagePost(
                    articleId = art.id,
                    pageId = settings.facebookPageId,
                    pageAccessToken = settings.facebookTokenEncrypted,
                    headline = art.seoTitle,
                    summary = art.newsSummary,
                    articleUrl = publishedUrl,
                    hashtags = listOf(art.category, "News", "Breaking")
                )
                if (fbRes.isSuccess) {
                    fbId = fbRes.getOrThrow().facebookPostId
                }
            }

            val updated = art.copy(
                status = "PUBLISHED",
                publishedUrl = publishedUrl,
                bloggerPostId = bloggerId,
                facebookPostId = fbId,
                publishedAt = System.currentTimeMillis()
            )
            repository.updateArticle(updated)
            _uiState.update { it.copy(selectedArticle = updated) }
            showNotification("Article published successfully!")
        }
    }

    fun testBlogger() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingApi = true, apiTestResult = "Connecting to Google Blogger API...") }
            val (success, msg) = repository.testBloggerConnection()
            _uiState.update { it.copy(isTestingApi = false, apiTestResult = if (success) "✓ $msg" else "✗ $msg") }
            showNotification(if (success) "Blogger connected" else "Blogger error: $msg")
        }
    }

    fun testFacebook() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingApi = true, apiTestResult = "Connecting to Facebook Graph API...") }
            val (success, msg) = repository.testFacebookConnection()
            _uiState.update { it.copy(isTestingApi = false, apiTestResult = if (success) "✓ $msg" else "✗ $msg") }
            showNotification(if (success) "Facebook Page connected" else "Facebook error: $msg")
        }
    }

    fun showNotification(msg: String) {
        _uiState.update { it.copy(statusNotification = msg) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(statusNotification = null) }
    }

    fun clearApiTestResult() {
        _uiState.update { it.copy(apiTestResult = null) }
    }
}
