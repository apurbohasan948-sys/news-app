package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NewsUiState
import com.example.ui.components.*
import com.example.ui.theme.*
import org.json.JSONArray

@Composable
fun ArticleEditorScreen(
    state: NewsUiState,
    onSaveDraft: (Long, String, String) -> Unit,
    onPublishNow: (Long) -> Unit,
    onBackToList: () -> Unit
) {
    val article = state.selectedArticle

    if (article == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            ClayCard(elevation = 6.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Article,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No dispatch selected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    ClayButton(
                        onClick = onBackToList,
                        text = "Return to Articles Archive",
                        icon = Icons.Default.ArrowBack
                    )
                }
            }
        }
        return
    }

    var titleInput by remember(article.id) { mutableStateOf(article.seoTitle) }
    var contentInput by remember(article.id) { mutableStateOf(article.mainArticle) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Editor", "Quality & Facts", "Versions", "Sources")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Clay Action Bar
        ClayCard(
            elevation = 5.dp,
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackToList) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    ClayBadge(
                        text = article.status.replace("_", " "),
                        statusType = when (article.status.uppercase()) {
                            "PUBLISHED" -> "success"
                            "DRAFT" -> "draft"
                            "QUALITY_GATE_FAILED", "FAILED" -> "failed"
                            else -> "warning"
                        }
                    )
                    if (article.qualityScore > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        ClayBadge(
                            text = "${article.qualityScore}/100",
                            statusType = if (article.qualityScore >= 80) "success" else "warning"
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClayButton(
                        onClick = { onSaveDraft(article.id, titleInput, contentInput) },
                        text = "Save",
                        icon = Icons.Default.Save,
                        containerColor = ClayBlueContainer,
                        contentColor = ClayBlue
                    )

                    ClayButton(
                        onClick = { onPublishNow(article.id) },
                        text = "Publish",
                        icon = Icons.Default.Publish,
                        containerColor = ClayMint
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clay Tab Row
        ClayTabs(
            selectedTabIndex = selectedTab,
            tabs = tabs,
            onTabSelected = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // Content Editor Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        ClayInput(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = "Article SEO Title (H1)",
                            placeholder = "Enter compelling, journalistic headline..."
                        )
                    }

                    item {
                        ClayCard(elevation = 3.dp) {
                            Text(
                                text = "Executive Lead Summary",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = article.newsSummary.ifBlank { "Summary not generated yet." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    item {
                        ClayInput(
                            value = contentInput,
                            onValueChange = { contentInput = it },
                            label = "Main Article Body (Markdown supported)",
                            placeholder = "Full journalistic report synthesis...",
                            singleLine = false,
                            maxLines = 100
                        )
                    }

                    if (article.publishedUrl.isNotBlank()) {
                        item {
                            ClayCard(elevation = 3.dp) {
                                Text("Live Published URL:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ClayMint)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = article.publishedUrl,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // Quality & Facts Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        ClayCard(elevation = 4.dp) {
                            Text(
                                text = "Quality Gate Scoring Breakdown",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val latestCheck = state.selectedArticleQualityChecks.firstOrNull()
                            if (latestCheck != null) {
                                ScoreRow("Overall Verification Score", "${latestCheck.totalScore}/100", latestCheck.totalScore >= 80)
                                ScoreRow("Factual Accuracy", "${latestCheck.factAccuracyScore}/25", true)
                                ScoreRow("Research Depth", "${latestCheck.researchScore}/20", true)
                                ScoreRow("Original Synthesis", "${latestCheck.originalityScore}/15", true)
                                ScoreRow("Writing & Structure", "${latestCheck.writingQualityScore}/15", true)
                                ScoreRow("SEO & Schema Package", "${latestCheck.seoScore}/10", true)
                                ScoreRow("Source Attribution", "${latestCheck.sourceQualityScore}/10", true)
                                ScoreRow("Visual Asset", "${latestCheck.imageScore}/5", true)
                            } else {
                                ScoreRow("Overall Verification Score", "${article.qualityScore}/100", article.qualityScore >= 80)
                                ScoreRow("Fact Check Score", "${article.factCheckScore}/100", article.factCheckScore >= 80)
                                ScoreRow("Originality Score", "${article.originalityScore}/100", true)
                                ScoreRow("SEO Score", "${article.seoScore}/100", true)
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Extracted Verified Claims & Facts",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    val factsList = try {
                        val arr = JSONArray(article.importantFacts)
                        (0 until arr.length()).map { arr.getString(it) }
                    } catch (e: Exception) {
                        emptyList()
                    }

                    if (factsList.isEmpty()) {
                        item {
                            ClayCard(elevation = 2.dp) {
                                Text(
                                    "No verified facts indexed for this dispatch yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(factsList) { fact ->
                            ClayCard(elevation = 2.dp) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(ClayMintContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ClayMint,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(fact, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Version History Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.selectedArticleVersions.isEmpty()) {
                        item {
                            ClayCard(elevation = 2.dp) {
                                Text(
                                    "Initial canonical version. No auto-rewrites or manual revisions logged.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(state.selectedArticleVersions) { v ->
                            ClayCard(elevation = 3.dp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Version #${v.versionNumber}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    if (v.qualityScore > 0) {
                                        ClayBadge(
                                            text = "Score: ${v.qualityScore}/100",
                                            statusType = if (v.qualityScore >= 80) "success" else "warning"
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Revision Trigger: ${v.changeReason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = v.contentSnippet,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            3 -> {
                // Sources Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.selectedArticleSources.isEmpty()) {
                        item {
                            ClayCard(elevation = 2.dp) {
                                Text(
                                    "No external sources recorded for this topic.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(state.selectedArticleSources) { source ->
                            ClayCard(elevation = 3.dp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ClayBadge(text = source.domain, statusType = "info")
                                    if (source.publicationDate.isNotBlank()) {
                                        Text(source.publicationDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(source.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(source.snippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(source.url, fontSize = 11.sp, color = ClayBlue, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, score: String, isPassed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        ClayBadge(
            text = score,
            statusType = if (isPassed) "success" else "warning"
        )
    }
}
