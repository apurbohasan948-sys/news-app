package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    // Articles
    @Query("SELECT * FROM articles ORDER BY createdAt DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: Long): ArticleEntity?

    @Query("SELECT * FROM articles WHERE status = :status ORDER BY createdAt DESC")
    fun getArticlesByStatus(status: String): Flow<List<ArticleEntity>>

    @Query("SELECT COUNT(*) FROM articles")
    fun getTotalArticlesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM articles WHERE status = 'PUBLISHED'")
    fun getPublishedArticlesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM articles WHERE status = 'DRAFT'")
    fun getDraftArticlesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM articles WHERE status = 'FAILED'")
    fun getFailedArticlesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM articles WHERE status = 'QUALITY_GATE_FAILED'")
    fun getQualityGateFailedArticlesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM articles WHERE status = 'QUALITY_CHECK'")
    fun getPendingQualityGateCount(): Flow<Int>

    @Query("SELECT AVG(qualityScore) FROM articles WHERE qualityScore > 0")
    fun getAverageQualityScore(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM articles WHERE publishedAt >= :startOfDayTimestamp")
    fun getTodayPublishedCount(startOfDayTimestamp: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity): Long

    @Update
    suspend fun updateArticle(article: ArticleEntity)

    @Query("UPDATE articles SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateArticleStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM articles WHERE id = :id")
    suspend fun deleteArticleById(id: Long)

    // Article Versions
    @Query("SELECT * FROM article_versions WHERE articleId = :articleId ORDER BY versionNumber DESC")
    fun getVersionsForArticle(articleId: Long): Flow<List<ArticleVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticleVersion(version: ArticleVersionEntity): Long

    // Topics
    @Query("SELECT * FROM topics ORDER BY discoveredAt DESC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE topicFingerprint = :fingerprint LIMIT 1")
    suspend fun findTopicByFingerprint(fingerprint: String): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    // Research Packages
    @Query("SELECT * FROM research_packages ORDER BY researchedAt DESC")
    fun getAllResearchPackages(): Flow<List<ResearchPackageEntity>>

    @Query("SELECT * FROM research_packages WHERE topicId = :topicId LIMIT 1")
    suspend fun getResearchPackageForTopic(topicId: Long): ResearchPackageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResearchPackage(pkg: ResearchPackageEntity): Long

    // Sources
    @Query("SELECT * FROM sources WHERE topicId = :topicId")
    fun getSourcesForTopic(topicId: Long): Flow<List<SourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<SourceEntity>)

    // LLM Providers
    @Query("SELECT * FROM llm_providers ORDER BY priority ASC")
    fun getAllLlmProviders(): Flow<List<LlmProviderEntity>>

    @Query("SELECT * FROM llm_providers WHERE isEnabled = 1 ORDER BY priority ASC")
    suspend fun getEnabledLlmProviders(): List<LlmProviderEntity>

    @Query("SELECT * FROM llm_providers WHERE id = :id")
    suspend fun getLlmProviderById(id: Long): LlmProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLlmProvider(provider: LlmProviderEntity): Long

    @Update
    suspend fun updateLlmProvider(provider: LlmProviderEntity)

    @Query("DELETE FROM llm_providers WHERE id = :id")
    suspend fun deleteLlmProvider(id: Long)

    // Quality Checks
    @Query("SELECT * FROM quality_checks WHERE articleId = :articleId ORDER BY checkedAt DESC")
    fun getQualityChecksForArticle(articleId: Long): Flow<List<QualityCheckEntity>>

    @Query("SELECT * FROM quality_checks ORDER BY checkedAt DESC LIMIT 20")
    fun getRecentQualityChecks(): Flow<List<QualityCheckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQualityCheck(check: QualityCheckEntity): Long

    // Article Images
    @Query("SELECT * FROM article_images WHERE articleId = :articleId LIMIT 1")
    suspend fun getImageForArticle(articleId: Long): ArticleImageEntity?

    @Query("SELECT * FROM article_images ORDER BY createdAt DESC")
    fun getAllImages(): Flow<List<ArticleImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ArticleImageEntity): Long

    // Blogger Posts
    @Query("SELECT * FROM blogger_posts ORDER BY publishedAt DESC")
    fun getAllBloggerPosts(): Flow<List<BloggerPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloggerPost(post: BloggerPostEntity): Long

    // Facebook Posts
    @Query("SELECT * FROM facebook_posts ORDER BY publishTime DESC")
    fun getAllFacebookPosts(): Flow<List<FacebookPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacebookPost(post: FacebookPostEntity): Long

    // Scheduled Jobs
    @Query("SELECT * FROM scheduled_jobs ORDER BY scheduledTime DESC")
    fun getAllScheduledJobs(): Flow<List<ScheduledJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledJob(job: ScheduledJobEntity): Long

    @Update
    suspend fun updateScheduledJob(job: ScheduledJobEntity)

    // API Logs
    @Query("SELECT * FROM api_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentApiLogs(): Flow<List<ApiLogEntity>>

    @Query("SELECT COUNT(*) FROM api_logs WHERE serviceName = 'TAVILY'")
    fun getTavilyUsageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM api_logs WHERE serviceName LIKE '%LLM%' OR serviceName LIKE '%OPENAI%'")
    fun getLlmUsageCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiLog(log: ApiLogEntity): Long

    // Error Logs
    @Query("SELECT * FROM error_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentErrorLogs(): Flow<List<ErrorLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertErrorLog(log: ErrorLogEntity): Long

    // Settings
    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    fun getSystemSettings(): Flow<SystemSettingsEntity?>

    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    suspend fun getSystemSettingsOnce(): SystemSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSystemSettings(settings: SystemSettingsEntity)
}
