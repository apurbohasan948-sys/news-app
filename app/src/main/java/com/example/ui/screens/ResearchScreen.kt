package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsCategory
import com.example.ui.NewsUiState
import com.example.ui.components.StatusBadge
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(
    state: NewsUiState,
    onTriggerResearch: (NewsCategory, String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(NewsCategory.TECHNOLOGY) }
    var customTopicInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Research Trigger Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TravelExplore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tavily Web Research & Discovery Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Category Filter",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(NewsCategory.values()) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.displayName, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customTopicInput,
                        onValueChange = { customTopicInput = it },
                        label = { Text("Topic or Query (Leave blank to auto-discover trending)") },
                        placeholder = { Text("e.g., Breakthrough Quantum Processing Standards") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val topic = customTopicInput.trim().ifEmpty { null }
                            onTriggerResearch(selectedCategory, topic)
                            customTopicInput = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ManageSearch, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (customTopicInput.isBlank()) "Discover & Research Trending Topics" else "Research Specified Topic")
                    }
                }
            }
        }

        // Section: Research Packages in Database
        item {
            Text(
                text = "Compiled Research Packages (${state.researchPackages.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (state.researchPackages.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No research dossiers generated yet. Trigger a research job above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(state.researchPackages) { pkg ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pkg.topicTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(status = "${pkg.sourcesCount} Sources")
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val queries = try {
                            val arr = JSONArray(pkg.searchQueriesJson)
                            (0 until arr.length()).map { arr.getString(it) }
                        } catch (e: Exception) {
                            emptyList()
                        }

                        if (queries.isNotEmpty()) {
                            Text(
                                text = "Queries: ${queries.joinToString(" • ")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        val facts = try {
                            val arr = JSONArray(pkg.importantFactsJson)
                            (0 until arr.length()).map { arr.getString(it) }
                        } catch (e: Exception) {
                            emptyList()
                        }

                        if (facts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Verified Facts (${facts.size}):",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall
                            )
                            facts.take(if (expanded) facts.size else 2).forEach { fact ->
                                Text(
                                    text = "• $fact",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TextButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (expanded) "Show Less" else "Show Details & Sources")
                        }
                    }
                }
            }
        }
    }
}
