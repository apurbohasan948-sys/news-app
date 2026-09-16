package com.example.engine

import com.example.data.model.SeoMetadata
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SeoEngine {

    fun generateSeoMetadata(
        title: String,
        summary: String,
        content: String,
        category: String,
        language: String
    ): SeoMetadata {
        val cleanSlug = title.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .replace(Regex("\\s+"), "-")
            .take(60)

        val words = title.split(" ")
        val primaryKeyword = words.filter { it.length > 4 }.take(2).joinToString(" ")
            .ifEmpty { category }

        val secondaryKeywords = listOf(
            "$category news",
            "$primaryKeyword update",
            "verified analysis",
            "latest developments"
        )

        val tags = listOf(
            category,
            primaryKeyword,
            "News",
            "Verified Reporting"
        )

        val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        val structuredData = JSONObject().apply {
            put("@context", "https://schema.org")
            put("@type", "NewsArticle")
            put("headline", title)
            put("description", summary.take(200))
            put("datePublished", isoDate)
            put("dateModified", isoDate)
            put("articleSection", category)
            put("inLanguage", language)
            put("author", JSONObject().apply {
                put("@type", "Organization")
                put("name", "AI Newsroom Editorial Board")
            })
            put("publisher", JSONObject().apply {
                put("@type", "Organization")
                put("name", "News Publisher")
                put("logo", JSONObject().apply {
                    put("@type", "ImageObject")
                    put("url", "https://example.com/logo.png")
                })
            })
            put("mainEntityOfPage", JSONObject().apply {
                put("@type", "WebPage")
                put("@id", "https://news.example.com/$cleanSlug")
            })
        }.toString(2)

        return SeoMetadata(
            title = "$title | Verified News Analysis",
            metaDescription = summary.take(155),
            urlSlug = cleanSlug,
            primaryKeyword = primaryKeyword,
            secondaryKeywords = secondaryKeywords,
            tags = tags,
            ogTitle = title,
            ogDescription = summary.take(180),
            imageAltText = "Journalistic photo covering $title in $category",
            structuredDataJson = structuredData
        )
    }

    fun generateSitemapXml(articleSlugs: List<String>, baseUrl: String = "https://news.example.com"): String {
        val sb = StringBuilder()
        val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n")
        sb.append("  <url>\n")
        sb.append("    <loc>$baseUrl/</loc>\n")
        sb.append("    <lastmod>$isoDate</lastmod>\n")
        sb.append("    <changefreq>hourly</changefreq>\n")
        sb.append("    <priority>1.0</priority>\n")
        sb.append("  </url>\n")
        for (slug in articleSlugs) {
            sb.append("  <url>\n")
            sb.append("    <loc>$baseUrl/$slug</loc>\n")
            sb.append("    <lastmod>$isoDate</lastmod>\n")
            sb.append("    <changefreq>daily</changefreq>\n")
            sb.append("    <priority>0.8</priority>\n")
            sb.append("  </url>\n")
        }
        sb.append("</urlset>")
        return sb.toString()
    }

    fun generateRobotsTxt(baseUrl: String = "https://news.example.com"): String {
        return """
            User-agent: *
            Allow: /
            Disallow: /admin/
            Disallow: /api/
            
            Sitemap: $baseUrl/sitemap.xml
        """.trimIndent()
    }
}
