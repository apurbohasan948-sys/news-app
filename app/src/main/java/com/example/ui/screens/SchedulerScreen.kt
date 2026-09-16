package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SystemSettingsEntity
import com.example.ui.NewsUiState
import com.example.ui.components.StatusBadge
import com.example.ui.theme.NewsroomCyanPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulerScreen(
    state: NewsUiState,
    onSaveSettings: (SystemSettingsEntity) -> Unit,
    onTriggerManualRun: () -> Unit = {}
) {
    var autoMode by remember(state.settings.autoMode) { mutableStateOf(state.settings.autoMode) }
    var intervalMinutes by remember(state.settings.publishIntervalMinutes) { mutableStateOf(state.settings.publishIntervalMinutes.toString()) }
    var dailyLimit by remember(state.settings.dailyArticleLimit) { mutableStateOf(state.settings.dailyArticleLimit.toString()) }
    var allowedHoursStart by remember(state.settings.allowedHoursStart) { mutableStateOf(state.settings.allowedHoursStart.toString()) }
    var allowedHoursEnd by remember(state.settings.allowedHoursEnd) { mutableStateOf(state.settings.allowedHoursEnd.toString()) }
    var categories by remember(state.settings.enabledCategories) { mutableStateOf(state.settings.enabledCategories) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = NewsroomCyanPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Background News Scheduler",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        StatusBadge(status = if (autoMode) "ACTIVE" else "PAUSED")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Autonomous Publishing Loop", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Periodically discovers, cross-checks, writes, and publishes without manual intervention.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoMode,
                            onCheckedChange = { isChecked ->
                                autoMode = isChecked
                                onSaveSettings(state.settings.copy(autoMode = isChecked))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = dailyLimit,
                            onValueChange = { dailyLimit = it },
                            label = { Text("Daily Limit") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = intervalMinutes,
                            onValueChange = { intervalMinutes = it },
                            label = { Text("Interval (Minutes)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = allowedHoursStart,
                            onValueChange = { allowedHoursStart = it },
                            label = { Text("Allowed Hours Start (0-23)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = allowedHoursEnd,
                            onValueChange = { allowedHoursEnd = it },
                            label = { Text("Allowed Hours End (0-23)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = categories,
                        onValueChange = { categories = it },
                        label = { Text("Enabled Topics / Categories") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onTriggerManualRun,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trigger Run Now")
                        }

                        Button(
                            onClick = {
                                val limit = dailyLimit.toIntOrNull() ?: 8
                                val interval = intervalMinutes.toIntOrNull() ?: 120
                                val hStart = allowedHoursStart.toIntOrNull() ?: 9
                                val hEnd = allowedHoursEnd.toIntOrNull() ?: 21
                                onSaveSettings(
                                    state.settings.copy(
                                        autoMode = autoMode,
                                        dailyArticleLimit = limit,
                                        publishIntervalMinutes = interval,
                                        allowedHoursStart = hStart,
                                        allowedHoursEnd = hEnd,
                                        enabledCategories = categories
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Schedule")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Execution Log History (${state.scheduledJobs.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (state.scheduledJobs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No scheduled jobs run yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(state.scheduledJobs) { job ->
                val timeStr = dateFormat.format(Date(job.scheduledTime))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = job.jobName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "$timeStr • Category: ${job.category}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (job.resultSummary.isNotBlank()) {
                                Text(text = job.resultSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        StatusBadge(status = job.status)
                    }
                }
            }
        }
    }
}
