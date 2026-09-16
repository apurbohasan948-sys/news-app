package com.example.data.remote

import com.example.data.local.ApiLogEntity
import com.example.data.local.ErrorLogEntity
import com.example.data.local.LlmProviderEntity
import com.example.data.local.NewsDao
import com.example.data.model.LlmJobRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class LlmManager(private val newsDao: NewsDao) {

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        fun maskKey(key: String): String {
            if (key.isBlank()) return "Not Configured"
            if (key.length <= 6) return "••••••"
            return key.take(3) + "••••••••" + key.takeLast(3)
        }
    }

    suspend fun testProviderConnection(provider: LlmProviderEntity): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val responseText = executeSingleProviderRequest(
                provider = provider,
                systemPrompt = "You are a professional editorial API health test bot.",
                userPrompt = "Please respond with exactly: SYSTEM_OPERATIONAL"
            )
            val latency = System.currentTimeMillis() - startTime
            val success = responseText.isNotBlank()
            newsDao.updateLlmProvider(
                provider.copy(
                    lastResponseTimeMs = latency,
                    lastStatusMessage = if (success) "Connected ($latency ms)" else "Empty response",
                    lastStatusSuccess = success
                )
            )
            Pair(success, if (success) "Connection successful ($latency ms)" else "Empty response")
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            val errorMsg = e.localizedMessage ?: "Unknown connection error"
            newsDao.updateLlmProvider(
                provider.copy(
                    lastResponseTimeMs = latency,
                    lastStatusMessage = "Failed: ${errorMsg.take(50)}",
                    lastStatusSuccess = false
                )
            )
            Pair(false, errorMsg)
        }
    }

    suspend fun executeWithFallback(
        role: LlmJobRole,
        systemPrompt: String,
        userPrompt: String
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val allEnabled = newsDao.getEnabledLlmProviders()
        if (allEnabled.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("No LLM providers are configured and enabled. Please add a provider in Settings/LLM Providers.")
            )
        }

        // Prioritize providers assigned to this role or general ones
        val sortedProviders = allEnabled.sortedWith(
            compareBy(
                { it.assignedRole != role.name && it.assignedRole != "ALL" },
                { it.priority }
            )
        )

        var lastError: Exception? = null
        var fallbackChainLog = mutableListOf<String>()

        for ((index, provider) in sortedProviders.withIndex()) {
            fallbackChainLog.add(provider.name)
            val startTime = System.currentTimeMillis()
            var retriesLeft = provider.maxRetries.coerceAtLeast(1)

            while (retriesLeft > 0) {
                try {
                    val resultText = executeSingleProviderRequest(provider, systemPrompt, userPrompt)
                    val latency = System.currentTimeMillis() - startTime

                    // Log success
                    newsDao.insertApiLog(
                        ApiLogEntity(
                            serviceName = "LLM_${provider.name}",
                            endpoint = provider.baseUrl,
                            responseStatus = 200,
                            latencyMs = latency,
                            success = true,
                            requestSummary = "Role: ${role.name}, Model: ${provider.modelName}",
                            responseSummary = "Generated ${resultText.length} chars"
                        )
                    )

                    val usedChain = if (index > 0) {
                        "Fallback to ${provider.name} (Primary failed)"
                    } else {
                        provider.name
                    }
                    return@withContext Result.success(Pair(resultText, usedChain))
                } catch (e: Exception) {
                    retriesLeft--
                    lastError = e

                    newsDao.insertErrorLog(
                        ErrorLogEntity(
                            moduleName = "LLM_${provider.name}",
                            errorMessage = "${e.message} (Retries left: $retriesLeft)",
                            retryCount = provider.maxRetries - retriesLeft,
                            fallbackApplied = if (index < sortedProviders.size - 1) "Trying next provider" else "Chain exhausted",
                            stackTraceSnippet = e.stackTraceToString().take(300)
                        )
                    )

                    if (retriesLeft <= 0) break
                    kotlinx.coroutines.delay(1000L * (provider.maxRetries - retriesLeft)) // Exponential backoff
                }
            }
        }

        Result.failure(lastError ?: Exception("All LLM providers in fallback chain failed: ${fallbackChainLog.joinToString(" -> ")}"))
    }

    private suspend fun executeSingleProviderRequest(
        provider: LlmProviderEntity,
        systemPrompt: String,
        userPrompt: String
    ): String {
        val apiKey = provider.apiKeyEncrypted
        val cleanBaseUrl = provider.baseUrl.trim().trimEnd('/')

        // Determine if target is Gemini REST or OpenAI-compatible
        if (cleanBaseUrl.contains("generativelanguage.googleapis.com")) {
            return callGeminiRest(cleanBaseUrl, provider.modelName, apiKey, systemPrompt, userPrompt)
        } else if (cleanBaseUrl.contains("api.anthropic.com")) {
            return callAnthropicRest(cleanBaseUrl, provider.modelName, apiKey, systemPrompt, userPrompt)
        } else {
            return callOpenAiCompatibleRest(cleanBaseUrl, provider.modelName, apiKey, systemPrompt, userPrompt)
        }
    }

    private fun callOpenAiCompatibleRest(
        baseUrl: String,
        modelName: String,
        apiKey: String,
        systemPrompt: String,
        userPrompt: String
    ): String {
        val endpoint = if (baseUrl.endsWith("/chat/completions")) {
            baseUrl
        } else {
            "$baseUrl/chat/completions"
        }

        val requestJson = JSONObject().apply {
            put("model", modelName)
            val messages = JSONArray().apply {
                if (systemPrompt.isNotBlank()) {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                }
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            }
            put("messages", messages)
            put("temperature", 0.4)
        }

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val response = NetworkClient.okHttpClient.newCall(requestBuilder.build()).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errObj = JSONObject(responseBody).optJSONObject("error")
                errObj?.optString("message") ?: responseBody.take(150)
            } catch (e: Exception) {
                responseBody.take(150)
            }
            throw RuntimeException("HTTP ${response.code}: $errorMsg")
        }

        val jsonObj = JSONObject(responseBody)
        val choices = jsonObj.getJSONArray("choices")
        if (choices.length() == 0) throw RuntimeException("No response choices returned")
        val firstChoice = choices.getJSONObject(0)
        return firstChoice.getJSONObject("message").getString("content")
    }

    private fun callAnthropicRest(
        baseUrl: String,
        modelName: String,
        apiKey: String,
        systemPrompt: String,
        userPrompt: String
    ): String {
        val endpoint = if (baseUrl.endsWith("/messages")) baseUrl else "$baseUrl/v1/messages"

        val requestJson = JSONObject().apply {
            put("model", modelName)
            put("max_tokens", 4096)
            if (systemPrompt.isNotBlank()) {
                put("system", systemPrompt)
            }
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            }
            put("messages", messages)
        }

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = NetworkClient.okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw RuntimeException("Anthropic error ${response.code}: ${responseBody.take(150)}")
        }

        val jsonObj = JSONObject(responseBody)
        val content = jsonObj.getJSONArray("content")
        if (content.length() == 0) throw RuntimeException("No content returned from Anthropic")
        return content.getJSONObject(0).getString("text")
    }

    private fun callGeminiRest(
        baseUrl: String,
        modelName: String,
        apiKey: String,
        systemPrompt: String,
        userPrompt: String
    ): String {
        val endpoint = "$baseUrl/v1beta/models/$modelName:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            if (systemPrompt.isNotBlank()) {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
            }
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userPrompt) })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = NetworkClient.okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw RuntimeException("Gemini REST error ${response.code}: ${responseBody.take(150)}")
        }

        val jsonObj = JSONObject(responseBody)
        val candidates = jsonObj.optJSONArray("candidates")
            ?: throw RuntimeException("No candidates in Gemini response")
        val content = candidates.getJSONObject(0).getJSONObject("content")
        val parts = content.getJSONArray("parts")
        return parts.getJSONObject(0).getString("text")
    }
}
