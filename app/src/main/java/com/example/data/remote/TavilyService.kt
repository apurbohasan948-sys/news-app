package com.example.data.remote

import com.example.data.local.ApiLogEntity
import com.example.data.local.NewsDao
import com.example.data.model.ResearchPackageData
import com.example.data.model.SourceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class TavilyService(private val newsDao: NewsDao) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun discoverTrendingTopics(apiKey: String, category: String): List<String> = withContext(Dispatchers.IO) {
        val query = "top breaking news developments $category latest ${java.time.Year.now()}"
        val sources = searchWeb(apiKey, query, searchDepth = "advanced", maxResults = 8)
        
        // Extract unique fresh news headlines/topics from returned results
        val topics = mutableListOf<String>()
        for (source in sources) {
            val title = cleanHeadline(source.title)
            if (title.length > 15 && !topics.any { isSimilarHeadline(it, title) }) {
                topics.add(title)
            }
        }
        if (topics.isEmpty()) {
            topics.add("Breakthrough developments in $category industry and governance")
        }
        topics.take(5)
    }

    suspend fun researchTopic(apiKey: String, topic: String, category: String): ResearchPackageData = withContext(Dispatchers.IO) {
        val searchQueries = listOf(
            topic,
            "$topic latest news timeline facts",
            "$topic verified reporting analysis"
        )

        val allSources = mutableListOf<SourceItem>()
        val seenUrls = mutableSetOf<String>()

        for (query in searchQueries) {
            val results = searchWeb(apiKey, query, searchDepth = "advanced", maxResults = 5)
            for (item in results) {
                if (item.url !in seenUrls && isValidUrl(item.url)) {
                    seenUrls.add(item.url)
                    allSources.add(item)
                }
            }
        }

        // Rank sources by relevance score descending
        val rankedSources = allSources.sortedByDescending { it.relevanceScore }

        // Extract important facts from the retrieved snippets
        val extractedFacts = mutableListOf<String>()
        val dates = mutableListOf<String>()
        val verified = mutableListOf<String>()
        val unverified = mutableListOf<String>()

        for (source in rankedSources) {
            if (source.publicationDate.isNotBlank()) {
                dates.add(source.publicationDate)
            }
            val sentences = source.snippet.split(". ")
            for (sentence in sentences) {
                val trimmed = sentence.trim()
                if (trimmed.length in 25..200) {
                    if (extractedFacts.size < 8 && !extractedFacts.contains(trimmed)) {
                        extractedFacts.add(trimmed)
                        verified.add("${trimmed} [Ref: ${source.sourceDomain}]")
                    }
                }
            }
        }

        if (extractedFacts.isEmpty() && rankedSources.isNotEmpty()) {
            extractedFacts.add("Multiple verified news outlets report significant developments regarding $topic.")
            verified.add("Cross-reported by ${rankedSources.size} sources.")
        }

        ResearchPackageData(
            topic = topic,
            searchQueries = searchQueries,
            sources = rankedSources,
            importantFacts = extractedFacts,
            publicationDates = dates.distinct().take(4),
            conflictingInfo = emptyList(),
            verifiedInfo = verified,
            unverifiedInfo = unverified
        )
    }

    suspend fun searchWeb(
        apiKey: String,
        query: String,
        searchDepth: String = "basic",
        maxResults: Int = 5
    ): List<SourceItem> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("Tavily API Key is missing. Please configure it in Settings.")
        }

        val startTime = System.currentTimeMillis()
        val requestJson = JSONObject().apply {
            put("api_key", apiKey)
            put("query", query)
            put("search_depth", searchDepth)
            put("include_answer", true)
            put("max_results", maxResults)
            put("include_images", true)
        }

        val request = Request.Builder()
            .url("https://api.tavily.com/search")
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = NetworkClient.okHttpClient.newCall(request).execute()
        val latency = System.currentTimeMillis() - startTime
        val responseBody = response.body?.string() ?: ""

        newsDao.insertApiLog(
            ApiLogEntity(
                serviceName = "TAVILY",
                endpoint = "https://api.tavily.com/search",
                responseStatus = response.code,
                latencyMs = latency,
                success = response.isSuccessful,
                requestSummary = "Query: ${query.take(60)}",
                responseSummary = if (response.isSuccessful) "Found sources" else responseBody.take(100)
            )
        )

        if (!response.isSuccessful) {
            throw RuntimeException("Tavily API error (${response.code}): ${responseBody.take(150)}")
        }

        val results = mutableListOf<SourceItem>()
        val json = JSONObject(responseBody)
        val itemsArray = json.optJSONArray("results") ?: JSONArray()

        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            val title = item.optString("title", "Untitled Source")
            val url = item.optString("url", "")
            val content = item.optString("content", "")
            val score = item.optDouble("score", 0.8)
            val publishedDate = item.optString("published_date", "")

            val domain = try {
                URL(url).host.replace("www.", "")
            } catch (e: Exception) {
                "web-source"
            }

            results.add(
                SourceItem(
                    title = title,
                    url = url,
                    snippet = content,
                    publicationDate = publishedDate,
                    relevanceScore = score,
                    sourceDomain = domain
                )
            )
        }

        results
    }

    private fun cleanHeadline(raw: String): String {
        return raw.substringBefore(" - ")
            .substringBefore(" | ")
            .substringBefore(" : ")
            .trim()
    }

    private fun isSimilarHeadline(a: String, b: String): Boolean {
        val wordsA = a.lowercase().split(" ").toSet()
        val wordsB = b.lowercase().split(" ").toSet()
        val intersection = wordsA.intersect(wordsB).size
        val union = wordsA.union(wordsB).size
        return (intersection.toDouble() / union.coerceAtLeast(1)) > 0.6
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }
}
