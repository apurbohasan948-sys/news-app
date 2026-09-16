package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NewsUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ArticlesScreen(
    state: NewsUiState,
    onArticleClick: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filterOptions = listOf("ALL", "PUBLISHED", "DRAFT", "QUALITY_GATE_FAILED", "FAILED")

    val filteredArticles = state.articles.filter { art ->
        val matchesFilter = when (selectedFilter) {
            "ALL" -> true
            else -> art.status.equals(selectedFilter, ignoreCase = true)
        }
        val matchesSearch = searchQuery.isBlank() ||
                art.seoTitle.contains(searchQuery, ignoreCase = true) ||
                art.newsSummary.contains(searchQuery, ignoreCase = true) ||
                art.category.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Clay Search Box
        item {
            ClayInput(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Dispatch Archive",
                placeholder = "Filter by headline, topic keywords, or category...",
                leadingIcon = Icons.Default.Search,
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else null
            )
        }

        // Clay Filter Pills
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterOptions) { filter ->
                    val isSelected = selectedFilter == filter
                    val badgeType = when (filter) {
                        "PUBLISHED" -> "success"
                        "DRAFT" -> "draft"
                        "QUALITY_GATE_FAILED", "FAILED" -> "failed"
                        else -> "info"
                    }

                    Box(
                        modifier = Modifier
                            .clickable { selectedFilter = filter }
                    ) {
                        ClayBadge(
                            text = filter.replace("_", " "),
                            statusType = if (isSelected) badgeType else "default"
                        )
                    }
                }
            }
        }

        // Count Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredArticles.size} dispatches",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (filteredArticles.isEmpty()) {
            item {
                ClayCard(elevation = 3.dp) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No matching dispatches found", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Try adjusting your search terms or filter selection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredArticles) { article ->
                val badgeStatus = when (article.status.uppercase()) {
                    "PUBLISHED" -> "success"
                    "DRAFT" -> "draft"
                    "QUALITY_GATE_FAILED", "FAILED" -> "failed"
                    else -> "warning"
                }

                ClayCard(
                    elevation = 4.dp,
                    onClick = { onArticleClick(article.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ClayBadge(
                                text = article.status.replace("_", " "),
                                statusType = badgeStatus
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = article.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (article.qualityScore > 0) {
                            ClayBadge(
                                text = "Score: ${article.qualityScore}/100",
                                statusType = if (article.qualityScore >= 80) "success" else "warning"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = article.seoTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = article.newsSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Model: ${article.llmProviderUsed.take(28)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Inspect & Edit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
