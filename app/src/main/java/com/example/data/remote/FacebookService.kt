package com.example.data.remote

import com.example.data.local.ApiLogEntity
import com.example.data.local.FacebookPostEntity
import com.example.data.local.NewsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject

class FacebookService(private val newsDao: NewsDao) {

    suspend fun testPageConnection(pageId: String, pageAccessToken: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (pageId.isBlank() || pageAccessToken.isBlank()) {
            return@withContext Pair(false, "Facebook Page ID and Access Token must not be empty.")
        }

        val startTime = System.currentTimeMillis()
        val url = "https://graph.facebook.com/v19.0/$pageId?fields=name,about,link&access_token=$pageAccessToken"

        val request = Request.Builder().url(url).get().build()

        try {
            val response = NetworkClient.okHttpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            newsDao.insertApiLog(
                ApiLogEntity(
                    serviceName = "FACEBOOK",
                    endpoint = "https://graph.facebook.com/v19.0/$pageId",
                    responseStatus = response.code,
                    latencyMs = latency,
                    success = response.isSuccessful,
                    requestSummary = "Test connection for Page ID: $pageId",
                    responseSummary = if (response.isSuccessful) "Page verified" else body.take(120)
                )
            )

            if (response.isSuccessful) {
                val json = JSONObject(body)
                val pageName = json.optString("name", "Facebook Page")
                Pair(true, "Connected to: $pageName")
            } else {
                Pair(false, "Facebook API error (${response.code}): ${body.take(120)}")
            }
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Connection error")
        }
    }

    suspend fun publishPagePost(
        articleId: Long,
        pageId: String,
        pageAccessToken: String,
        headline: String,
        summary: String,
        articleUrl: String,
        hashtags: List<String>
    ): Result<FacebookPostEntity> = withContext(Dispatchers.IO) {
        if (pageId.isBlank() || pageAccessToken.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Facebook Page credentials not configured.")
            )
        }

        val startTime = System.currentTimeMillis()
        val tagString = hashtags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" }
        val message = "🚨 $headline\n\n$summary\n\nRead the full verified story: $articleUrl\n\n$tagString"

        val url = "https://graph.facebook.com/v19.0/$pageId/feed"

        val formBody = FormBody.Builder()
            .add("message", message)
            .add("link", articleUrl)
            .add("access_token", pageAccessToken)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        try {
            val response = NetworkClient.okHttpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            newsDao.insertApiLog(
                ApiLogEntity(
                    serviceName = "FACEBOOK",
                    endpoint = url,
                    responseStatus = response.code,
                    latencyMs = latency,
                    success = response.isSuccessful,
                    requestSummary = "Post to Page $pageId: $headline",
                    responseSummary = if (response.isSuccessful) "Published to feed" else body.take(120)
                )
            )

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    RuntimeException("Facebook publish failed (${response.code}): ${body.take(150)}")
                )
            }

            val json = JSONObject(body)
            val postId = json.optString("id", "")
            val postUrl = "https://facebook.com/$postId"

            val postEntity = FacebookPostEntity(
                articleId = articleId,
                facebookPostId = postId,
                pageId = pageId,
                postUrl = postUrl,
                headline = headline,
                status = "SUCCESS",
                errorMessage = ""
            )
            newsDao.insertFacebookPost(postEntity)

            Result.success(postEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
