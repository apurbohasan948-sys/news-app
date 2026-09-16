package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SystemSettingsEntity
import com.example.ui.NewsUiState
import com.example.ui.components.StatusBadge
import com.example.ui.theme.NewsroomCyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacebookScreen(
    state: NewsUiState,
    onSaveSettings: (SystemSettingsEntity) -> Unit,
    onTestConnection: () -> Unit
) {
    var pageIdInput by remember(state.settings.facebookPageId) { mutableStateOf(state.settings.facebookPageId) }
    var tokenInput by remember(state.settings.facebookTokenEncrypted) { mutableStateOf(state.settings.facebookTokenEncrypted) }

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
                        Icon(Icons.Default.Share, contentDescription = null, tint = NewsroomCyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Facebook Graph API Auto-Publisher",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pageIdInput,
                        onValueChange = { pageIdInput = it },
                        label = { Text("Facebook Page ID") },
                        placeholder = { Text("e.g., 108291048291048") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Page Access Token (EAAG...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTestConnection,
                            modifier = Modifier.weight(1f),
                            enabled = !state.isTestingApi
                        ) {
                            Text(if (state.isTestingApi) "Testing..." else "Test Page")
                        }

                        Button(
                            onClick = {
                                onSaveSettings(
                                    state.settings.copy(
                                        facebookPageId = pageIdInput.trim(),
                                        facebookTokenEncrypted = tokenInput.trim()
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Credentials")
                        }
                    }

                    if (state.apiTestResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = state.apiTestResult,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Published Facebook Posts (${state.facebookPosts.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (state.facebookPosts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No Facebook dispatches published yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(state.facebookPosts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = post.headline, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            StatusBadge(status = post.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Post ID: ${post.facebookPostId}", style = MaterialTheme.typography.labelSmall)
                        if (post.postUrl.isNotBlank()) {
                            Text(text = post.postUrl, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
