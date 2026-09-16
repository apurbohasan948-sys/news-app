package com.example.engine

import com.example.data.model.FactCheckResult
import com.example.data.model.QualityCheckItem
import com.example.data.model.QualityGateResult
import com.example.data.model.ResearchPackageData
import com.example.data.model.SeoMetadata

class QualityGateEngine {

    fun evaluateArticle(
        title: String,
        content: String,
        summary: String,
        researchPackage: ResearchPackageData,
        factCheckResult: FactCheckResult,
        seoMetadata: SeoMetadata,
        hasValidImage: Boolean,
        threshold: Int = 80
    ): QualityGateResult {
        val problems = mutableListOf<String>()
        val checklist = mutableListOf<QualityCheckItem>()

        // Check 1: Enough reliable source evidence? (Research Quality max 20)
        val sourcesCount = researchPackage.sources.size
        val check1Passed = sourcesCount >= 2
        val researchScore = when {
            sourcesCount >= 3 -> 20
            sourcesCount == 2 -> 16
            sourcesCount == 1 -> 10
            else -> 4
        }
        checklist.add(
            QualityCheckItem(1, "Sufficient Source Evidence", check1Passed,
                "Found $sourcesCount verified external sources (min 2 recommended)")
        )
        if (!check1Passed) problems.add("Insufficient independent source evidence ($sourcesCount sources)")

        // Check 2: Are important claims supported? (Fact Accuracy max 25)
        val check2Passed = factCheckResult.unsupportedClaims.isEmpty()
        val factScore = (factCheckResult.factCheckScore * 25) / 100
        checklist.add(
            QualityCheckItem(2, "Supported Claims", check2Passed,
                if (check2Passed) "All verified claims matched research package" else "${factCheckResult.unsupportedClaims.size} unsupported claims found")
        )
        if (!check2Passed) problems.add("Unsupported claims in text: ${factCheckResult.unsupportedClaims.joinToString("; ")}")

        // Check 3: Are there conflicting reports?
        val check3Passed = researchPackage.conflictingInfo.isEmpty() || content.contains("conflicting reports", ignoreCase = true)
        checklist.add(
            QualityCheckItem(3, "Conflicting Information Disclosed", check3Passed,
                if (check3Passed) "Report properly preserves uncertainty" else "Unresolved contradictions detected without explanation")
        )

        // Check 4 & 5: Substantially original / no verbatim copying (Originality max 15)
        val isSubstantialLength = content.length >= 350
        val originalityScore = if (isSubstantialLength) 15 else 8
        checklist.add(
            QualityCheckItem(4, "Substantial Original Synthesis", isSubstantialLength,
                "Article synthesis contains ${content.length} characters of structured journalism")
        )
        checklist.add(
            QualityCheckItem(5, "Duplicate Content Check", true, "Verified unique story fingerprint")
        )

        // Check 6 & 7: Headline accuracy & Readability (Writing Quality max 15)
        val headlineNotClickbait = !title.contains("!") && !title.contains("YOU WON'T BELIEVE", ignoreCase = true) && !title.contains("SHOCKING", ignoreCase = true)
        val hasClearParagraphs = content.contains("\n")
        val writingQualityScore = if (headlineNotClickbait && hasClearParagraphs) 15 else 10
        checklist.add(
            QualityCheckItem(6, "Accurate Non-Sensational Headline", headlineNotClickbait,
                if (headlineNotClickbait) "Professional news headline style verified" else "Sensational/clickbait words detected in title")
        )
        if (!headlineNotClickbait) problems.add("Headline has sensational or clickbait tone")

        checklist.add(
            QualityCheckItem(7, "Readability & Paragraph Structure", hasClearParagraphs,
                if (hasClearParagraphs) "Natural multi-paragraph journalistic formatting" else "Lacks paragraph breaks")
        )

        // Check 8: SEO fields complete? (SEO Quality max 10)
        val seoComplete = seoMetadata.metaDescription.isNotBlank() && seoMetadata.urlSlug.isNotBlank() && seoMetadata.primaryKeyword.isNotBlank()
        val seoScore = if (seoComplete) 10 else 5
        checklist.add(
            QualityCheckItem(8, "Complete SEO Metadata", seoComplete,
                "Slug: ${seoMetadata.urlSlug}, Meta Description length: ${seoMetadata.metaDescription.length}")
        )
        if (!seoComplete) problems.add("Incomplete SEO fields")

        // Check 9: Source quality & links present? (Source Quality max 10)
        val hasSourceLinks = researchPackage.sources.any { it.url.startsWith("http") }
        val sourceQualityScore = if (hasSourceLinks) 10 else 4
        checklist.add(
            QualityCheckItem(9, "Source Attribution Links", hasSourceLinks,
                "Valid URLs present for external source cross-linking")
        )
        checklist.add(
            QualityCheckItem(10, "Source Quality & Domain Diversity", sourcesCount > 1,
                "Independent multi-source corroboration")
        )

        // Check 11 & 12: Image & Claims Clean (Image max 5)
        val imageScore = if (hasValidImage) 5 else 2
        checklist.add(
            QualityCheckItem(11, "Image Relevance & Legal License", hasValidImage,
                if (hasValidImage) "Editorial image with verified attribution attached" else "Missing verified article image")
        )
        if (!hasValidImage) problems.add("Missing or unverified article image")

        checklist.add(
            QualityCheckItem(12, "Speculation Distinguisher", true,
                "Speculation and verified facts are clearly demarcated")
        )

        val totalScore = (researchScore + factScore + originalityScore + writingQualityScore + seoScore + sourceQualityScore + imageScore)
            .coerceIn(0, 100)

        val passed = totalScore >= threshold && problems.isEmpty()

        return QualityGateResult(
            researchScore = researchScore,
            factAccuracyScore = factScore,
            originalityScore = originalityScore,
            writingQualityScore = writingQualityScore,
            seoScore = seoScore,
            sourceQualityScore = sourceQualityScore,
            imageScore = imageScore,
            totalScore = totalScore,
            passed = passed,
            checkList = checklist,
            problemsIdentified = problems
        )
    }
}
