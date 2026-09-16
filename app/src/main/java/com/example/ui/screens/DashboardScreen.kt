package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsCategory
import com.example.ui.NewsUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    state: NewsUiState,
    onTriggerPipeline: (NewsCategory, String?) -> Unit,
    onToggleAutoMode: (Boolean) -> Unit,
    onArticleClick: (Long) -> Unit,
    onNavigateTo: (String) -> Unit
) {
    val todaysPostsCount = remember(state.articles) {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        state.articles.count { it.publishedAt != null && (now - it.publishedAt) < oneDayMs }
    }

    val apiHealthText = remember(state.errorLogs, state.apiLogs) {
        if (state.errorLogs.isEmpty()) "100% Operational"
        else {
            val total = (state.apiLogs.size + state.errorLogs.size).coerceAtLeast(1)
            val rate = ((1.0 - (state.errorLogs.size.toDouble() / total)) * 100).toInt().coerceIn(0, 100)
            "$rate% Health"
        }
    }

    val schedulerText = if (state.settings.autoMode) "Every ${state.settings.publishIntervalMinutes}m" else "Paused"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Clay Banner: Auto Mode & Quick Trigger
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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Autonomous Editorial Newsroom",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ClayBadge(
                                text = if (state.settings.autoMode) "ACTIVE" else "STANDBY",
                                statusType = if (state.settings.autoMode) "success" else "warning"
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.settings.autoMode)
                                "Auto pipeline active: publishing every ${state.settings.publishIntervalMinutes}m (${state.settings.allowedHoursStart}:00 - ${state.settings.allowedHoursEnd}:00)"
                            else
                                "Auto mode paused. Dispatches will be saved as editorial review drafts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    ClayToggle(
                        checked = state.settings.autoMode,
                        onCheckedChange = onToggleAutoMode
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ClayButton(
                        onClick = { onTriggerPipeline(NewsCategory.TECHNOLOGY, null) },
                        modifier = Modifier.weight(1f),
                        text = "Run Pipeline Now",
                        icon = Icons.Default.PlayArrow,
                        containerColor = ClayBlue
                    )

                    ClayButton(
                        onClick = { onNavigateTo("Research") },
                        modifier = Modifier.weight(1f),
                        text = "Explore Research",
                        icon = Icons.Default.TravelExplore,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section: 8 Requested Clay-Style Stat Cards
        item {
            Text(
                text = "Operational Metrics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Row 1: Total Articles & Published
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClayStatCard(
                    title = "Total Articles",
                    value = "${state.totalArticles}",
                    icon = Icons.Default.Article,
                    accentColor = ClayBlue,
                    containerColor = ClayBlueContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("Articles") }
                )
                ClayStatCard(
                    title = "Published",
                    value = "${state.publishedArticles}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = ClayMint,
                    containerColor = ClayMintContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("Articles") }
                )
            }
        }

        // Row 2: Drafts & Quality Gate Passed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClayStatCard(
                    title = "Drafts",
                    value = "${state.draftArticles}",
                    icon = Icons.Default.EditNote,
                    accentColor = ClayLavender,
                    containerColor = ClayLavenderContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("Articles") }
                )
                ClayStatCard(
                    title = "Quality Gate Passed",
                    value = "${state.qualityGatePassedCount}",
                    icon = Icons.Default.Verified,
                    accentColor = ClayMint,
                    containerColor = ClayMintContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("Quality Gate") }
                )
            }
        }

        // Row 3: Quality Gate Failed & Today's Posts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClayStatCard(
                    title = "Quality Gate Failed",
                    value = "${state.qualityGateFailedCount + state.failedArticles}",
                    icon = Icons.Default.ErrorOutline,
                    accentColor = ClayCoral,
                    containerColor = ClayCoralContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("Quality Gate") }
                )
                ClayStatCard(
                    title = "Today's Posts",
                    value = "$todaysPostsCount",
                    icon = Icons.Default.Today,
                    accentColor = ClayCyan,
                    containerColor = ClayCyanContainer,
                    modifier = Modifier.weight(1f),
                    subtitle = "24h window"
                )
            }
        }

        // Row 4: API Health & Scheduler
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClayStatCard(
                    title = "API Health",
                    value = apiHealthText,
                    icon = Icons.Default.HealthAndSafety,
                    accentColor = if (state.errorLogs.isEmpty()) ClayMint else ClayPeach,
                    containerColor = if (state.errorLogs.isEmpty()) ClayMintContainer else ClayPeachContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("Logs") }
                )
                ClayStatCard(
                    title = "Scheduler",
                    value = schedulerText,
                    icon = Icons.Default.Schedule,
                    accentColor = if (state.settings.autoMode) ClayMint else ClayPeach,
                    containerColor = if (state.settings.autoMode) ClayMintContainer else ClayPeachContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("Scheduler") }
                )
            }
        }

        // Requirement 31: Visual Pipeline Card (Research ↓ Writing ↓ Fact Check ↓ SEO ↓ Image ↓ Quality Gate ↓ Blogger ↓ Facebook)
        item {
            VisualPipelineClayCard(
                progress = state.pipelineProgress,
                onTrigger = { onTriggerPipeline(NewsCategory.TECHNOLOGY, null) }
            )
        }

        // Section: Recent Articles
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Editorial Dispatches",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "View All (${state.articles.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateTo("Articles") }
                )
            }
        }

        if (state.articles.isEmpty()) {
            item {
                ClayCard(elevation = 3.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Feed,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No articles generated yet.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Click 'Run Pipeline Now' above to start automated multi-source research.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(state.articles.take(5)) { article ->
                val timeFormatted = remember(article.createdAt) {
                    val diffMin = (System.currentTimeMillis() - article.createdAt) / (1000 * 60)
                    if (diffMin < 60) "${diffMin}m ago" else "${diffMin / 60}h ago"
                }

                ClayTableRow(
                    title = article.seoTitle,
                    subtitle = article.newsSummary.ifBlank { "Journalistic synthesis for ${article.category}" },
                    badgeText = article.status.replace("_", " "),
                    badgeStatus = when (article.status.uppercase()) {
                        "PUBLISHED" -> "success"
                        "DRAFT" -> "draft"
                        "QUALITY_GATE_FAILED", "FAILED" -> "failed"
                        else -> "running"
                    },
                    timeText = timeFormatted,
                    onClick = { onArticleClick(article.id) }
                )
            }
        }
    }
}

/**
 * Visual Pipeline Card displaying the 8 sequential stages:
 * Research ↓ Writing ↓ Fact Check ↓ SEO ↓ Image ↓ Quality Gate ↓ Blogger ↓ Facebook
 * with animated status indicators for each stage.
 */
@Composable
private fun VisualPipelineClayCard(
    progress: com.example.engine.PipelineProgress,
    onTrigger: () -> Unit
) {
    val stages = listOf(
        PipelineStageItem("Research", "Multi-source Tavily query & web extraction", Icons.Default.TravelExplore),
        PipelineStageItem("Writing", "Structured journalistic dispatch synthesis", Icons.Default.Edit),
        PipelineStageItem("Fact Check", "Cross-domain claim corroboration", Icons.Default.FactCheck),
        PipelineStageItem("SEO", "Schema.org, OpenGraph & meta tags", Icons.Default.Search),
        PipelineStageItem("Image", "Editorial hero resolution & metadata", Icons.Default.Image),
        PipelineStageItem("Quality Gate", "12-point journalistic audit engine", Icons.Default.VerifiedUser),
        PipelineStageItem("Blogger", "Google Blogger REST V3 syndication", Icons.Default.Public),
        PipelineStageItem("Facebook", "Facebook Graph API page broadcast", Icons.Default.Share)
    )

    val currentStepLower = progress.currentStep.lowercase()
    val activeStageIndex = when {
        !progress.isRunning -> -1
        currentStepLower.contains("tavily") || currentStepLower.contains("research") -> 0
        currentStepLower.contains("write") || currentStepLower.contains("draft") || currentStepLower.contains("synthesis") -> 1
        currentStepLower.contains("fact") || currentStepLower.contains("claim") -> 2
        currentStepLower.contains("seo") || currentStepLower.contains("slug") || currentStepLower.contains("schema") -> 3
        currentStepLower.contains("image") || currentStepLower.contains("asset") -> 4
        currentStepLower.contains("quality") || currentStepLower.contains("gate") -> 5
        currentStepLower.contains("blogger") -> 6
        currentStepLower.contains("facebook") || currentStepLower.contains("social") -> 7
        else -> 1
    }

    ClayCard(
        elevation = 6.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = ClayBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Automated Pipeline Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "End-to-end 8-stage news synthesis & syndication architecture",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ClayStatusIndicator(
                label = if (progress.isRunning) "RUNNING" else "READY",
                isActive = progress.isRunning,
                activeColor = if (progress.isRunning) ClayPeach else ClayMint
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stages List with Clay aesthetics and downward flow indicators (↓)
        stages.forEachIndexed { index, stage ->
            val stageStatus = when {
                !progress.isRunning -> StageStatus.IDLE
                index < activeStageIndex -> StageStatus.COMPLETED
                index == activeStageIndex -> StageStatus.RUNNING
                else -> StageStatus.PENDING
            }

            StageRow(
                stage = stage,
                status = stageStatus,
                stepNumber = index + 1
            )

            if (index < stages.size - 1) {
                // Downward connection indicator
                Box(
                    modifier = Modifier
                        .padding(start = 22.dp, top = 2.dp, bottom = 2.dp)
                        .size(width = 2.dp, height = 12.dp)
                        .background(
                            if (stageStatus == StageStatus.COMPLETED) ClayMint else Color(0xFFE2E8F0)
                        )
                )
            }
        }

        if (progress.isRunning) {
            Spacer(modifier = Modifier.height(14.dp))
            ClayProgress(
                progress = (progress.stepIndex.toFloat() / progress.totalSteps.toFloat()).coerceIn(0.05f, 1f),
                color = ClayBlue
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Step ${progress.stepIndex}/${progress.totalSteps}: ${progress.currentStep}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClayBlue
            )
            if (progress.logs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .claySurface(
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color(0xFF1E293B),
                            elevation = 2.dp
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = progress.logs.takeLast(3).joinToString("\n"),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF86EFAC)
                    )
                }
            }
        }
    }
}

private enum class StageStatus {
    IDLE,
    RUNNING,
    COMPLETED,
    PENDING
}

private data class PipelineStageItem(
    val name: String,
    val description: String,
    val icon: ImageVector
)

@Composable
private fun StageRow(
    stage: PipelineStageItem,
    status: StageStatus,
    stepNumber: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_active_stage")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stage_pulse"
    )

    val (badgeText, badgeColor, badgeBg) = when (status) {
        StageStatus.RUNNING -> Triple("IN PROGRESS", ClayPeach, ClayPeachContainer)
        StageStatus.COMPLETED -> Triple("VERIFIED", ClayMint, ClayMintContainer)
        StageStatus.PENDING -> Triple("QUEUED", Color(0xFF94A3B8), Color(0xFFF1F5F9))
        StageStatus.IDLE -> Triple("READY", ClayBlue, ClayBlueContainer)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .claySurface(
                shape = RoundedCornerShape(14.dp),
                backgroundColor = if (status == StageStatus.RUNNING) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
                elevation = if (status == StageStatus.RUNNING) 4.dp else 2.dp
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (status) {
                            StageStatus.RUNNING -> ClayPeach.copy(alpha = pulseAlpha)
                            StageStatus.COMPLETED -> ClayMint
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (status == StageStatus.COMPLETED) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "$stepNumber",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (status == StageStatus.RUNNING) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = stage.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stage.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg)
                .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = badgeText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
    }
}
