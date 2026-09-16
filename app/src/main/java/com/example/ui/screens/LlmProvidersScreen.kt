package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.LlmProviderEntity
import com.example.ui.NewsUiState
import com.example.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmProvidersScreen(
    state: NewsUiState,
    onSaveProvider: (LlmProviderEntity) -> Unit,
    onDeleteProvider: (Long) -> Unit,
    onTestProvider: (LlmProviderEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProvider by remember { mutableStateOf<LlmProviderEntity?>(null) }

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
                    Text(
                        text = "Multi-LLM Provider Chain",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Priority-ordered fallback routing with automatic role assignment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            editingProvider = null
                            showAddDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Custom LLM Provider")
                    }
                }
            }
        }

        item {
            Text(
                text = "Configured Providers (${state.providers.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(state.providers) { provider ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = provider.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = "Priority ${provider.priority}")
                            }
                            Text(
                                text = "Model: ${provider.modelName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Switch(
                            checked = provider.isEnabled,
                            onCheckedChange = { isChecked ->
                                onSaveProvider(provider.copy(isEnabled = isChecked))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Base URL: ${provider.baseUrl}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Role: ${provider.assignedRole} • Key: ${provider.apiKeyMasked.ifBlank { "Unset" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (provider.lastStatusMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = provider.lastStatusMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (provider.lastStatusSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onTestProvider(provider) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test API", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                editingProvider = provider
                                showAddDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Key", fontSize = 12.sp)
                        }

                        IconButton(onClick = { onDeleteProvider(provider.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf(editingProvider?.name ?: "") }
        var baseUrl by remember { mutableStateOf(editingProvider?.baseUrl ?: "https://api.openai.com/v1") }
        var modelName by remember { mutableStateOf(editingProvider?.modelName ?: "gpt-4o") }
        var apiKey by remember { mutableStateOf(editingProvider?.apiKeyEncrypted ?: "") }
        var priority by remember { mutableStateOf(editingProvider?.priority?.toString() ?: "1") }
        var selectedRole by remember { mutableStateOf(editingProvider?.assignedRole ?: "ALL") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (editingProvider == null) "Add LLM Provider" else "Edit LLM Provider") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Provider Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        label = { Text("Model Identifier") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key (Stored Server-Side)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priority (1 = Highest)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val masked = if (apiKey.length > 8) apiKey.take(4) + "••••••••" + apiKey.takeLast(4) else "••••••••"
                        val providerToSave = (editingProvider ?: LlmProviderEntity(
                            name = name,
                            baseUrl = baseUrl,
                            modelName = modelName,
                            apiKeyMasked = masked,
                            apiKeyEncrypted = apiKey
                        )).copy(
                            name = name.ifBlank { "Custom Provider" },
                            baseUrl = baseUrl,
                            modelName = modelName,
                            apiKeyEncrypted = apiKey,
                            apiKeyMasked = masked,
                            priority = priority.toIntOrNull() ?: 1,
                            assignedRole = selectedRole,
                            isEnabled = true
                        )
                        onSaveProvider(providerToSave)
                        showAddDialog = false
                    }
                ) {
                    Text("Save Provider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
