package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.NewsCategory
import com.example.ui.components.ClayBadge
import com.example.ui.components.claySurface
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsApp(
    viewModel: NewsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusNotification) {
        state.statusNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header with Claymorphism accent
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .claySurface(
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = ClayBlueContainer,
                                elevation = 2.dp
                            )
                            .padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClayBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Feed, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Newsroom AI",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = ClayTextPrimary
                            )
                            Text(
                                text = "Autonomous Pipeline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CORE OPERATIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                    )

                    val coreScreens = listOf(
                        AppScreen.DASHBOARD,
                        AppScreen.RESEARCH,
                        AppScreen.ARTICLES,
                        AppScreen.ARTICLE_EDITOR,
                        AppScreen.QUALITY_GATE
                    )

                    coreScreens.forEach { screen ->
                        NavigationDrawerItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title, fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal) },
                            selected = currentScreen == screen,
                            onClick = {
                                currentScreen = screen
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 2.dp),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "PUBLISHING CHANNELS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                    )

                    val pubScreens = listOf(
                        AppScreen.SCHEDULER,
                        AppScreen.BLOGGER,
                        AppScreen.FACEBOOK,
                        AppScreen.IMAGES
                    )

                    pubScreens.forEach { screen ->
                        NavigationDrawerItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title, fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal) },
                            selected = currentScreen == screen,
                            onClick = {
                                currentScreen = screen
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 2.dp),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "ENGINES & SYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                    )

                    val sysScreens = listOf(
                        AppScreen.LLM_PROVIDERS,
                        AppScreen.SEO,
                        AppScreen.ANALYTICS,
                        AppScreen.LOGS,
                        AppScreen.SETTINGS
                    )

                    sysScreens.forEach { screen ->
                        NavigationDrawerItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title, fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal) },
                            selected = currentScreen == screen,
                            onClick = {
                                currentScreen = screen
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 2.dp),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentScreen.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ClayBadge(
                                text = if (state.settings.autoMode) "AUTO ON" else "MANUAL",
                                statusType = if (state.settings.autoMode) "success" else "warning"
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.triggerPipeline(NewsCategory.TECHNOLOGY, null) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ClayBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = "Run Pipeline",
                                    tint = ClayBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                // Mobile-first Claymorphism Bottom Navigation Bar
                val mobileNavItems = listOf(
                    Triple(AppScreen.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
                    Triple(AppScreen.RESEARCH, "Research", Icons.Default.TravelExplore),
                    Triple(AppScreen.ARTICLES, "Articles", Icons.Default.Article),
                    Triple(AppScreen.QUALITY_GATE, "Q-Gate", Icons.Default.VerifiedUser),
                    Triple(AppScreen.SETTINGS, "Settings", Icons.Default.Settings)
                )

                NavigationBar(
                    modifier = Modifier.claySurface(
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        elevation = 8.dp
                    ),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    mobileNavItems.forEach { (screen, label, icon) ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) ClayBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ClayBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ClayBlueContainer
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        state = state,
                        onTriggerPipeline = { cat, topic -> viewModel.triggerPipeline(cat, topic) },
                        onToggleAutoMode = { enabled -> viewModel.toggleAutoMode(enabled) },
                        onArticleClick = { id ->
                            viewModel.selectArticle(id)
                            currentScreen = AppScreen.ARTICLE_EDITOR
                        },
                        onNavigateTo = { dest ->
                            currentScreen = when (dest) {
                                "Research" -> AppScreen.RESEARCH
                                "Articles" -> AppScreen.ARTICLES
                                "Quality Gate" -> AppScreen.QUALITY_GATE
                                "Logs" -> AppScreen.LOGS
                                "Scheduler" -> AppScreen.SCHEDULER
                                "Settings" -> AppScreen.SETTINGS
                                else -> AppScreen.DASHBOARD
                            }
                        }
                    )
                    AppScreen.RESEARCH -> ResearchScreen(
                        state = state,
                        onTriggerResearch = { cat, topic -> viewModel.triggerPipeline(cat, topic) }
                    )
                    AppScreen.ARTICLES -> ArticlesScreen(
                        state = state,
                        onArticleClick = { id ->
                            viewModel.selectArticle(id)
                            currentScreen = AppScreen.ARTICLE_EDITOR
                        }
                    )
                    AppScreen.ARTICLE_EDITOR -> ArticleEditorScreen(
                        state = state,
                        onSaveDraft = { id, title, content -> viewModel.saveDraft(id, title, content) },
                        onPublishNow = { id -> viewModel.publishArticleNow(id) },
                        onBackToList = { currentScreen = AppScreen.ARTICLES }
                    )
                    AppScreen.QUALITY_GATE -> QualityGateScreen(
                        state = state,
                        onArticleClick = { id ->
                            viewModel.selectArticle(id)
                            currentScreen = AppScreen.ARTICLE_EDITOR
                        }
                    )
                    AppScreen.IMAGES -> ImagesScreen(state = state)
                    AppScreen.SCHEDULER -> SchedulerScreen(
                        state = state,
                        onSaveSettings = { s -> viewModel.saveSettings(s) },
                        onTriggerManualRun = { viewModel.triggerPipeline(NewsCategory.TECHNOLOGY, null) }
                    )
                    AppScreen.BLOGGER -> BloggerScreen(
                        state = state,
                        onSaveSettings = { s -> viewModel.saveSettings(s) },
                        onTestConnection = { viewModel.testBlogger() }
                    )
                    AppScreen.FACEBOOK -> FacebookScreen(
                        state = state,
                        onSaveSettings = { s -> viewModel.saveSettings(s) },
                        onTestConnection = { viewModel.testFacebook() }
                    )
                    AppScreen.LLM_PROVIDERS -> LlmProvidersScreen(
                        state = state,
                        onSaveProvider = { p -> viewModel.addOrUpdateProvider(p) },
                        onDeleteProvider = { id -> viewModel.deleteProvider(id) },
                        onTestProvider = { p -> viewModel.testProvider(p) }
                    )
                    AppScreen.SEO -> SeoScreen(
                        state = state,
                        onArticleClick = { id ->
                            viewModel.selectArticle(id)
                            currentScreen = AppScreen.ARTICLE_EDITOR
                        }
                    )
                    AppScreen.ANALYTICS -> AnalyticsScreen(state = state)
                    AppScreen.LOGS -> LogsScreen(state = state)
                    AppScreen.SETTINGS -> SettingsScreen(
                        state = state,
                        onSaveSettings = { s -> viewModel.saveSettings(s) },
                        onToggleAutoMode = { enabled -> viewModel.toggleAutoMode(enabled) }
                    )
                }
            }
        }
    }
}
