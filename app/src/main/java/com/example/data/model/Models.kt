package com.example.data.model

enum class ArticleStatus(val displayName: String) {
    QUEUED("Queued"),
    RESEARCHING("Researching"),
    WRITING("Writing"),
    FACT_CHECKING("Fact Checking"),
    OPTIMIZING("Optimizing SEO"),
    IMAGE_PROCESSING("Image Processing"),
    QUALITY_CHECK("Quality Gate Check"),
    APPROVED("Approved"),
    PUBLISHING("Publishing"),
    PUBLISHED("Published"),
    FAILED("Failed"),
    QUALITY_GATE_FAILED("Quality Gate Failed"),
    DRAFT("Draft")
}

enum class LlmJobRole(val displayName: String) {
    RESEARCH_ANALYSIS("Research Analysis"),
    ARTICLE_WRITING("Article Writing"),
    FACT_CHECKING("Fact Checking"),
    SEO_OPTIMIZATION("SEO Optimization"),
    SOCIAL_MEDIA("Social Media"),
    REWRITING("Rewriting / Correction")
}

enum class NewsCategory(val displayName: String) {
    TECHNOLOGY("Technology"),
    AI("Artificial Intelligence"),
    BUSINESS("Business"),
    SCIENCE("Science"),
    WORLD("World News"),
    ENTERTAINMENT("Entertainment"),
    SPORTS("Sports"),
    GAMING("Gaming"),
    OTHER("Other")
}

enum class ContentLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "Bangla (বাংলা)")
}

data class SourceItem(
    val title: String,
    val url: String,
    val snippet: String,
    val publicationDate: String = "",
    val relevanceScore: Double = 0.9,
    val sourceDomain: String = ""
)

data class ResearchPackageData(
    val topic: String,
    val searchQueries: List<String> = emptyList(),
    val sources: List<SourceItem> = emptyList(),
    val importantFacts: List<String> = emptyList(),
    val publicationDates: List<String> = emptyList(),
    val conflictingInfo: List<String> = emptyList(),
    val verifiedInfo: List<String> = emptyList(),
    val unverifiedInfo: List<String> = emptyList()
)

data class FactCheckResult(
    val factCheckScore: Int,
    val verifiedClaims: List<String> = emptyList(),
    val questionableClaims: List<String> = emptyList(),
    val unsupportedClaims: List<String> = emptyList(),
    val contradictions: List<String> = emptyList(),
    val recommendedCorrections: List<String> = emptyList(),
    val isPassed: Boolean = factCheckScore >= 80
)

data class QualityGateResult(
    val researchScore: Int, // max 20
    val factAccuracyScore: Int, // max 25
    val originalityScore: Int, // max 15
    val writingQualityScore: Int, // max 15
    val seoScore: Int, // max 10
    val sourceQualityScore: Int, // max 10
    val imageScore: Int, // max 5
    val totalScore: Int, // max 100
    val passed: Boolean,
    val checkList: List<QualityCheckItem> = emptyList(),
    val problemsIdentified: List<String> = emptyList()
)

data class QualityCheckItem(
    val id: Int,
    val title: String,
    val passed: Boolean,
    val detail: String
)

data class SeoMetadata(
    val title: String,
    val metaDescription: String,
    val urlSlug: String,
    val primaryKeyword: String,
    val secondaryKeywords: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val ogTitle: String = "",
    val ogDescription: String = "",
    val imageAltText: String = "",
    val structuredDataJson: String = ""
)

data class ImageMetadata(
    val imageUrl: String,
    val concept: String,
    val altText: String,
    val caption: String,
    val licenseInfo: String = "Editorial / CC Attribution",
    val provider: String = "Tavily Web / Editorial Stock",
    val width: Int = 1200,
    val height: Int = 630
)
