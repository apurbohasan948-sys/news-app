package com.example.data.remote

import com.example.data.local.ApiLogEntity
import com.example.data.local.BloggerPostEntity
import com.example.data.local.NewsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class BloggerService(private val newsDao: NewsDao) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(blogId: String, apiKeyOrToken: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (blogId.isBlank() || apiKeyOrToken.isBlank()) {
            return@withContext Pair(false, "Blog ID and API Key/Token must not be empty.")
        }

        val startTime = System.currentTimeMillis()
        val isBearer = apiKeyOrToken.startsWith("ya29.") || apiKeyOrToken.length > 50

        val url = if (isBearer) {
            "https://www.googleapis.com/blogger/v3/blogs/$blogId"
        } else {
            "https://www.googleapis.com/blogger/v3/blogs/$blogId?key=$apiKeyOrToken"
        }

        val requestBuilder = Request.Builder().url(url).get()
        if (isBearer) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKeyOrToken")
        }

        try {
            val response = NetworkClient.okHttpClient.newCall(requestBuilder.build()).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            newsDao.insertApiLog(
                ApiLogEntity(
                    serviceName = "BLOGGER",
                    endpoint = "https://www.googleapis.com/blogger/v3/blogs/$blogId",
                    responseStatus = response.code,
                    latencyMs = latency,
                    success = response.isSuccessful,
                    requestSummary = "Test connection for Blog ID: $blogId",
                    responseSummary = if (response.isSuccessful) "Connected successfully" else body.take(100)
                )
            )

            if (response.isSuccessful) {
                val json = JSONObject(body)
                val blogName = json.optString("name", "Blogger Site")
                val blogUrl = json.optString("url", "")
                Pair(true, "Connected to: $blogName ($blogUrl)")
            } else {
                Pair(false, "Blogger Error (${response.code}): ${body.take(120)}")
            }
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Connection failed")
        }
    }

    suspend fun publishPost(
        articleId: Long,
        blogId: String,
        apiKeyOrToken: String,
        title: String,
        htmlContent: String,
        labels: List<String>,
        isDraft: Boolean = false
    ): Result<BloggerPostEntity> = withContext(Dispatchers.IO) {
        if (blogId.isBlank() || apiKeyOrToken.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Blogger Blog ID or Auth credential not configured in Settings.")
            )
        }

        val startTime = System.currentTimeMillis()
        val isBearer = apiKeyOrToken.startsWith("ya29.") || apiKeyOrToken.length > 50

        val url = if (isBearer) {
            "https://www.googleapis.com/blogger/v3/blogs/$blogId/posts${if (isDraft) "?isDraft=true" else ""}"
        } else {
            "https://www.googleapis.com/blogger/v3/blogs/$blogId/posts?key=$apiKeyOrToken${if (isDraft) "&isDraft=true" else ""}"
        }

        val postJson = JSONObject().apply {
            put("kind", "blogger#post")
            put("title", title)
            put("content", htmlContent)
            val labelsArray = JSONArray()
            for (label in labels) {
                labelsArray.put(label)
            }
            put("labels", labelsArray)
        }

        val requestBuilder = Request.Builder()
            .url(url)
            .post(postJson.toString().toRequestBody(jsonMediaType))

        if (isBearer) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKeyOrToken")
        }

        try {
            val response = NetworkClient.okHttpClient.newCall(requestBuilder.build()).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            newsDao.insertApiLog(
                ApiLogEntity(
                    serviceName = "BLOGGER",
                    endpoint = "https://www.googleapis.com/blogger/v3/blogs/$blogId/posts",
                    responseStatus = response.code,
                    latencyMs = latency,
                    success = response.isSuccessful,
                    requestSummary = "Publish Article #$articleId ($title)",
                    responseSummary = if (response.isSuccessful) "Post created successfully" else body.take(150)
                )
            )

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    RuntimeException("Blogger publish failed (${response.code}): ${body.take(150)}")
                )
            }

            val json = JSONObject(body)
            val postId = json.optString("id", "")
            val postUrl = json.optString("url", "")

            val postEntity = BloggerPostEntity(
                articleId = articleId,
                bloggerPostId = postId,
                blogId = blogId,
                postUrl = postUrl,
                title = title,
                status = "SUCCESS",
                errorMessage = ""
            )
            newsDao.insertBloggerPost(postEntity)

            Result.success(postEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
