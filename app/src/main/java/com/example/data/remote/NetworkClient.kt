package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object NetworkClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS // Avoid logging sensitive bodies/keys
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request()
            // Add universal User-Agent identifying newsroom editorial agent
            val newRequest = request.newBuilder()
                .header("User-Agent", "NewsPublisher-Engine/1.0 (Automated Editorial Bot)")
                .build()
            chain.proceed(newRequest)
        }
        .build()
}
