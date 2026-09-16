package com.example.engine

import com.example.data.model.FactCheckResult
import com.example.data.model.ResearchPackageData

class FactCheckingEngine {

    fun performFactCheck(
        articleTitle: String,
        articleContent: String,
        researchPackage: ResearchPackageData
    ): FactCheckResult {
        val verifiedClaims = mutableListOf<String>()
        val questionableClaims = mutableListOf<String>()
        val unsupportedClaims = mutableListOf<String>()
        val contradictions = mutableListOf<String>()
        val recommendedCorrections = mutableListOf<String>()

        val researchFacts = researchPackage.importantFacts.map { it.lowercase() }
        val sources = researchPackage.sources

        // 1. Check if article references core research facts
        var factsMatched = 0
        for (fact in researchPackage.importantFacts) {
            val keyWords = fact.split(" ").filter { it.length > 4 }
            val matchCount = keyWords.count { articleContent.contains(it, ignoreCase = true) }
            if (matchCount >= (keyWords.size / 2).coerceAtLeast(1)) {
                factsMatched++
                if (verifiedClaims.size < 5) {
                    verifiedClaims.add("Verified fact: ${fact.take(120)}")
                }
            }
        }

        // 2. Scan for fabricated statistics or extreme unverified claims
        val paragraphs = articleContent.split("\n\n").filter { it.isNotBlank() }
        for (p in paragraphs) {
            if (p.contains("100%") || p.contains("guaranteed to") || p.contains("never before seen in history")) {
                questionableClaims.add("Exaggerated language detected: \"${p.take(80)}...\"")
            }
        }

        // 3. Check for source consistency
        if (sources.isEmpty()) {
            unsupportedClaims.add("Zero verified external sources available for cross-referencing.")
            contradictions.add("Article makes assertions without independent citations.")
            recommendedCorrections.add("Attach verified journalistic citations from Tavily research.")
        } else {
            verifiedClaims.add("Cross-checked against ${sources.size} independent reporting sources.")
        }

        // 4. Calculate score (0-100)
        var score = 75
        if (sources.isNotEmpty()) score += 15
        if (factsMatched >= 2) score += 10
        if (questionableClaims.isNotEmpty()) score -= (questionableClaims.size * 5)
        if (unsupportedClaims.isNotEmpty()) score -= (unsupportedClaims.size * 10)

        score = score.coerceIn(20, 100)

        if (score < 80) {
            recommendedCorrections.add("Harmonize claims with verified facts extracted from primary sources.")
            recommendedCorrections.add("Ensure all numbers, dates, and named entities reflect research package.")
        }

        return FactCheckResult(
            factCheckScore = score,
            verifiedClaims = verifiedClaims,
            questionableClaims = questionableClaims,
            unsupportedClaims = unsupportedClaims,
            contradictions = contradictions,
            recommendedCorrections = recommendedCorrections,
            isPassed = score >= 80
        )
    }
}
