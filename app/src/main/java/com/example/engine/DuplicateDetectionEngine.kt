package com.example.engine

import java.security.MessageDigest

class DuplicateDetectionEngine {

    fun generateTopicFingerprint(title: String, category: String): String {
        val normalized = title.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .take(64) + category.lowercase()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(normalized.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }.take(16)
    }

    fun calculateSimilarity(textA: String, textB: String): Double {
        val tokensA = tokenize(textA)
        val tokensB = tokenize(textB)
        if (tokensA.isEmpty() || tokensB.isEmpty()) return 0.0

        val intersection = tokensA.intersect(tokensB).size
        val union = tokensA.union(tokensB).size
        if (union == 0) return 0.0
        return intersection.toDouble() / union
    }

    private fun tokenize(text: String): Set<String> {
        val stopWords = setOf("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were")
        return text.lowercase()
            .split(Regex("\\W+"))
            .filter { it.length > 3 && it !in stopWords }
            .toSet()
    }
}
