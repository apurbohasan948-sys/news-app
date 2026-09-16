package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NewsUiState
import com.example.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogsScreen(state: NewsUiState) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("API Logs (${state.apiLogs.size})", "Error Logs (${state.errorLogs.size})")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 12.sp) }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // API Logs
            if (state.apiLogs.isEmpty()) {
                item {
                    Text("No API calls recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(state.apiLogs) { log ->
                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(status = if (log.success) "SUCCESS" else "FAILED")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = log.serviceName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(text = "${log.latencyMs}ms • $timeStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = log.endpoint, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                            if (log.requestSummary.isNotBlank()) {
                                Text(text = "Req: ${log.requestSummary}", style = MaterialTheme.typography.labelSmall)
                            }
                            if (log.responseSummary.isNotBlank()) {
                                Text(text = "Res: ${log.responseSummary}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        } else {
            // Error Logs
            if (state.errorLogs.isEmpty()) {
                item {
                    Text("No system errors recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(state.errorLogs) { err ->
                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(err.timestamp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = err.moduleName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = err.errorMessage, style = MaterialTheme.typography.bodySmall)
                            if (err.stackTraceSnippet.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = err.stackTraceSnippet, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
