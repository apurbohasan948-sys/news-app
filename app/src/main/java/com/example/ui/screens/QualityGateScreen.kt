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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SystemSettingsEntity
import com.example.ui.NewsUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun QualityGateScreen(
    state: NewsUiState,
    onSaveSettings: (SystemSettingsEntity) -> Unit = {},
    onArticleClick: (Long) -> Unit = {}
) {
    val qualityChecks = listOf(
        "1. Sufficient Source Evidence" to "Minimum 2-3 verified external news sources with distinct domains",
        "2. Supported Claims" to "Every factual assertion matches Tavily verified data points",
        "3. Uncertainty Preservation" to "Conflicting reports and unresolved questions are clearly stated",
        "4. Original Synthesis" to "Substantive editorial analysis with zero plagiarism or verbatim blocks",
        "5. Fingerprint Deduplication" to "Sha-256 semantic deduplication against previously published stories",
        "6. Non-Sensational Headline" to "Factual, objective headline strictly avoiding clickbait or hyperbole",
        "7. Readability & Structure" to "Balanced paragraphs, executive summary, context, and implications",
        "8. Complete SEO Package" to "Slug, meta description (<160 chars), primary keywords, JSON-LD Schema",
        "9. Source Attribution" to "Citations and direct links to origin reporting",
        "10. Source Diversity" to "Multiple independent institutional wire and publication inputs",
        "11. Image Relevance & Rights" to "Licensed editorial visuals with verified alt-text and caption",
        "12. Speculation Boundary" to "Clear demarcation between confirmed facts and analyst projections"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quality Gate Summary Banner
        item {
            ClayCard(
                elevation = 6.dp,
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .claySurface(
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = ClayMintContainer,
                                    elevation = 2.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ClayMint)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "12-Point Quality Gate Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Strict journalistic verification firewall",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    ClayBadge(text = "ACTIVE", statusType = "success")
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Every synthesized article must pass 12 automated checks and achieve a score ≥ ${state.settings.qualityGateThreshold}/100. Failing articles trigger auto-rewriting up to ${state.settings.maxCorrectionAttempts} times before human editorial review.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Metrics Grid (4 Clay Stat Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClayStatCard(
                    title = "Pass Threshold",
                    value = "${state.settings.qualityGateThreshold}/100",
                    icon = Icons.Default.Gavel,
                    accentColor = ClayBlue,
                    containerColor = ClayBlueContainer,
                    modifier = Modifier.weight(1f)
                )
                ClayStatCard(
                    title = "Avg Quality Score",
                    value = if (state.averageQualityScore > 0) "%.1f".format(state.averageQualityScore) else "N/A",
                    icon = Icons.Default.Score,
                    accentColor = if (state.averageQualityScore >= 80) ClayMint else ClayPeach,
                    containerColor = if (state.averageQualityScore >= 80) ClayMintContainer else ClayPeachContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClayStatCard(
                    title = "Failed Gate",
                    value = "${state.qualityGateFailedCount}",
                    icon = Icons.Default.Cancel,
                    accentColor = ClayCoral,
                    containerColor = ClayCoralContainer,
                    modifier = Modifier.weight(1f)
                )
                ClayStatCard(
                    title = "Max Retries",
                    value = "${state.settings.maxCorrectionAttempts}",
                    icon = Icons.Default.Autorenew,
                    accentColor = ClayPeach,
                    containerColor = ClayPeachContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 12 Mandatory Quality Rules
        item {
            Text(
                text = "12 Mandatory Quality Checks",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        items(qualityChecks) { (title, description) ->
            ClayCard(elevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ClayMintContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = ClayMint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Section: Failed Articles Requiring Attention
        val failedArticles = state.articles.filter { it.status == "QUALITY_GATE_FAILED" }
        if (failedArticles.isNotEmpty()) {
            item {
                Text(
                    text = "Articles Needing Review (${failedArticles.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ClayCoral,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            items(failedArticles) { article ->
                ClayCard(elevation = 4.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(article.seoTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Score: ${article.qualityScore}/100 • Rewrites: ${article.rewriteAttempts}", style = MaterialTheme.typography.labelSmall, color = ClayCoral)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        ClayButton(
                            onClick = { onArticleClick(article.id) },
                            text = "Inspect",
                            containerColor = ClayCoral
                        )
                    }
                }
            }
        }
    }
}
