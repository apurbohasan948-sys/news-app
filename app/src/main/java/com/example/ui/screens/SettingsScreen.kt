package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SystemSettingsEntity
import com.example.ui.NewsUiState
import com.example.ui.theme.NewsStatusGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: NewsUiState,
    onSaveSettings: (SystemSettingsEntity) -> Unit,
    onToggleAutoMode: (Boolean) -> Unit
) {
    var tavilyKey by remember(state.settings.tavilyApiKeyEncrypted) { mutableStateOf(state.settings.tavilyApiKeyEncrypted) }
    var selectedLanguage by remember(state.settings.defaultLanguage) { mutableStateOf(state.settings.defaultLanguage) }
    var maxAttempts by remember(state.settings.maxCorrectionAttempts) { mutableStateOf(state.settings.maxCorrectionAttempts.toString()) }
    var timezone by remember(state.settings.timezone) { mutableStateOf(state.settings.timezone) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Global Auto Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Global Automatic Publishing Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = if (state.settings.autoMode) "ENABLED: System runs full pipeline and publishes automatically." else "DISABLED: System saves generated articles as drafts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = state.settings.autoMode, onCheckedChange = onToggleAutoMode)
                }
            }
        }

        // Research & AI Credentials
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Research & Editorial Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tavilyKey,
                        onValueChange = { tavilyKey = it },
                        label = { Text("Tavily Search API Key (tvly-...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Default Publishing Language", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = selectedLanguage == "en",
                            onClick = { selectedLanguage = "en" },
                            label = { Text("English (US/UK)") }
                        )
                        FilterChip(
                            selected = selectedLanguage == "bn",
                            onClick = { selectedLanguage = "bn" },
                            label = { Text("বাংলা (Bangla)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = maxAttempts,
                            onValueChange = { maxAttempts = it },
                            label = { Text("Max Correction Loops") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = timezone,
                            onValueChange = { timezone = it },
                            label = { Text("Timezone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val attempts = maxAttempts.toIntOrNull() ?: 3
                            onSaveSettings(
                                state.settings.copy(
                                    tavilyApiKeyEncrypted = tavilyKey.trim(),
                                    defaultLanguage = selectedLanguage,
                                    maxCorrectionAttempts = attempts,
                                    timezone = timezone.trim()
                                )
                            )
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Configuration")
                    }
                }
            }
        }

        // Security & Server-Side Storage Audit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NewsStatusGreen, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Server-Side Credential Isolation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "All API keys (Tavily, LLMs, Blogger, Facebook) are stored in secure local-encrypted databases and injected directly via backend workers. No credentials are leaked to public client bundles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
