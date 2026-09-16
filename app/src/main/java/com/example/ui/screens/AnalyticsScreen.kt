package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NewsUiState
import com.example.ui.components.MetricCard
import com.example.ui.theme.NewsStatusGreen
import com.example.ui.theme.NewsStatusRed
import com.example.ui.theme.NewsStatusYellow
import com.example.ui.theme.NewsroomCyanPrimary

@Composable
fun AnalyticsScreen(
    state: NewsUiState
) {
    val totalArticles = state.articles.size
    val publishedCount = state.articles.count { it.status == "PUBLISHED" }
    val failedQualityCount = state.articles.count { it.status == "QUALITY_GATE_FAILED" }
    val draftCount = state.articles.count { it.status == "DRAFT" }

    val categoryDistribution = state.articles.groupBy { it.category }

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
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = NewsroomCyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Newsroom Publishing Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Real-time pipeline performance, model efficiency, quality benchmarks, and automated syndication telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Published",
                    value = "$publishedCount",
                    icon = Icons.Default.CheckCircle,
                    accentColor = NewsStatusGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Draft / Pending",
                    value = "$draftCount",
                    icon = Icons.Default.EditNote,
                    accentColor = NewsStatusYellow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Quality Gate Rejections",
                    value = "$failedQualityCount",
                    icon = Icons.Default.Block,
                    accentColor = NewsStatusRed,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Avg Quality Score",
                    value = if (state.averageQualityScore > 0) "%.1f".format(state.averageQualityScore) else "N/A",
                    icon = Icons.Default.Verified,
                    accentColor = NewsroomCyanPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Editorial Coverage by Category", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (categoryDistribution.isEmpty()) {
                        Text("No articles recorded yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        categoryDistribution.forEach { (cat, list) ->
                            val percentage = if (totalArticles > 0) (list.size.toFloat() / totalArticles * 100).toInt() else 0
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${list.size} articles ($percentage%)", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { percentage / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = NewsroomCyanPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // LLM Provider Usage Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LLM Models Utilized in Production", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(10.dp))

                    val providerUsage = state.articles.groupBy { it.llmProviderUsed }
                    if (providerUsage.isEmpty()) {
                        Text("No synthesis runs logged yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        providerUsage.forEach { (provider, list) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(provider.take(30), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text("${list.size} calls", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NewsroomCyanPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
