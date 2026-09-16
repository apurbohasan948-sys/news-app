package com.example.engine

import com.example.data.local.NewsDao
import com.example.data.local.ScheduledJobEntity
import com.example.data.model.NewsCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class SchedulerManager(
    private val newsDao: NewsDao,
    private val pipeline: PublishingPipeline,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var schedulerJob: Job? = null

    fun startScheduler() {
        if (schedulerJob?.isActive == true) return

        schedulerJob = scope.launch {
            while (isActive) {
                val settings = newsDao.getSystemSettingsOnce()
                if (settings != null && settings.autoMode) {
                    val calendar = Calendar.getInstance()
                    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

                    // Check allowed hours
                    if (currentHour in settings.allowedHoursStart..settings.allowedHoursEnd) {
                        // Check daily limit
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        val startOfDay = calendar.timeInMillis

                        val publishedCount = newsDao.getTodayPublishedCount(startOfDay)
                        // Collect first value
                        // Pick random category from enabled categories
                        val enabledList = settings.enabledCategories.split(",").map { it.trim() }
                        val categoryName = enabledList.randomOrNull() ?: "Technology"
                        val targetCategory = try {
                            NewsCategory.valueOf(categoryName.uppercase())
                        } catch (e: Exception) {
                            NewsCategory.TECHNOLOGY
                        }

                        val jobEntity = ScheduledJobEntity(
                            jobName = "Auto-Publish: ${targetCategory.displayName}",
                            scheduledTime = System.currentTimeMillis(),
                            status = "RUNNING",
                            category = targetCategory.name
                        )
                        val jobId = newsDao.insertScheduledJob(jobEntity)

                        val result = pipeline.runFullPipeline(targetCategory)
                        val completedStatus = if (result.isSuccess) "COMPLETED" else "FAILED"
                        val summary = if (result.isSuccess) {
                            "Article generated: ${result.getOrNull()?.seoTitle}"
                        } else {
                            "Pipeline error: ${result.exceptionOrNull()?.message}"
                        }

                        newsDao.updateScheduledJob(
                            jobEntity.copy(
                                id = jobId,
                                executedTime = System.currentTimeMillis(),
                                status = completedStatus,
                                resultSummary = summary
                            )
                        )
                    }
                }

                val intervalMinutes = settings?.publishIntervalMinutes ?: 120
                delay(intervalMinutes * 60 * 1000L)
            }
        }
    }

    fun stopScheduler() {
        schedulerJob?.cancel()
        schedulerJob = null
    }

    fun isRunning(): Boolean = schedulerJob?.isActive == true
}
