package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    RESEARCH("Research", Icons.Default.TravelExplore),
    ARTICLES("Articles", Icons.Default.Article),
    ARTICLE_EDITOR("Article Editor", Icons.Default.EditNote),
    QUALITY_GATE("Quality Gate", Icons.Default.VerifiedUser),
    IMAGES("Images", Icons.Default.Image),
    SCHEDULER("Scheduler", Icons.Default.Schedule),
    BLOGGER("Blogger", Icons.Default.Public),
    FACEBOOK("Facebook", Icons.Default.Share),
    LLM_PROVIDERS("LLM Providers", Icons.Default.SmartToy),
    SEO("SEO & Indexing", Icons.Default.Search),
    ANALYTICS("Analytics", Icons.Default.BarChart),
    LOGS("Logs & Audit", Icons.Default.ReceiptLong),
    SETTINGS("Settings", Icons.Default.Settings)
}
