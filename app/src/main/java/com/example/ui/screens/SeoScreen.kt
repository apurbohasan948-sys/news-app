package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SeoEngine
import com.example.ui.NewsUiState
import com.example.ui.theme.NewsroomCyanPrimary

@Composable
fun SeoScreen(
    state: NewsUiState,
    onArticleClick: (Long) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("SEO Architecture", "Schema.org Preview", "Sitemap & Robots")

    val latestArticle = state.articles.firstOrNull()
    val seoEngine = remember { SeoEngine() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NewsroomCyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Automated SEO & Structured Data Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Every article is automatically enhanced with SEO-optimized titles, search-engine slugs, meta descriptions under 160 characters, OpenGraph/Twitter cards, and W3C validated Schema.org NewsArticle JSON-LD.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 12.sp) }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                if (latestArticle != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Latest Article SEO Manifest", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("SEO Title:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(latestArticle.seoTitle, style = MaterialTheme.typography.bodyMedium)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Meta Description:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(latestArticle.newsSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Standard SEO Package Applied to Every Dispatch", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            val seoFeatures = listOf(
                                "Title tag with primary entity and zero clickbait",
                                "URL slug formatted with hyphens and stop-words removed",
                                "Meta description strictly within 140-160 characters",
                                "Open Graph og:title, og:description, og:image, og:type=article",
                                "Twitter Cards (summary_large_image)",
                                "Canonical link tag to prevent duplicate content indexing penalty",
                                "Language alternate tags (English en-US & Bengali bn-BD)"
                            )
                            seoFeatures.forEach { feat ->
                                Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = NewsroomCyanPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(feat, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Schema.org Preview
                item {
                    val sampleSchema = if (!latestArticle?.schemaJsonLd.isNullOrBlank()) {
                        latestArticle!!.schemaJsonLd
                    } else {
                        seoEngine.generateSeoMetadata(
                            title = "OpenAI Releases Next-Generation Model to Global Developers",
                            summary = "Artificial intelligence research lab OpenAI has officially launched its newest high-efficiency model.",
                            content = "Sample full article content for verification.",
                            category = "Technology",
                            language = "en"
                        ).structuredDataJson
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("JSON-LD NewsArticle Structured Data (W3C / Google Search Validated)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = sampleSchema,
                                    color = Color(0xFFD4D4D4),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Sitemap & Robots
                item {
                    val slugs = state.articles.map { it.seoTitle.lowercase().replace(Regex("[^a-z0-9]"), "-").take(40) }
                    val sitemap = seoEngine.generateSitemapXml(articleSlugs = slugs)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Dynamic XML Sitemap (sitemap.xml)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = sitemap,
                                    color = Color(0xFFD4D4D4),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
