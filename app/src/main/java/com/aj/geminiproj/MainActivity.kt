package com.aj.geminiproj

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.aj.geminiproj.features.chat.navigation.ChatRoute
import com.aj.geminiproj.navigation.presentation.AppNavigationViewModel
import com.aj.geminiproj.navigation.NavGraph
import com.aj.geminiproj.navigation.presentation.NavigationUiEffect
import com.aj.geminiproj.navigation.presentation.NavigationUiEvent
import com.aj.geminiproj.navigation.presentation.components.AppDrawer
import com.aj.geminiproj.ui.theme.GeminiProjTheme
import com.aj.geminiproj.ui.util.isTablet
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeminiProjTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                AppContent(windowSizeClass = windowSizeClass)
            }
        }
    }
}

@Composable
fun AppContent(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val navigationViewModel: AppNavigationViewModel = koinViewModel()
    val navigationState by navigationViewModel.navigationUiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val isTablet = remember(windowSizeClass) { windowSizeClass.isTablet() }

    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
    val density = LocalDensity.current
    val strokeWidthPx = remember(density) { with(density) { 1.dp.toPx() } }

    val drawerBorderModifier = remember(borderColor, strokeWidthPx) {
        Modifier.drawWithContent() {
            drawContent()
            // Draw a shadow line on the right edge
            drawLine(
                color = borderColor,
                start = Offset(size.width - strokeWidthPx / 2, 0f),
                end = Offset(size.width - strokeWidthPx / 2, size.height),
                strokeWidth = strokeWidthPx
            )
        }
    }

    LaunchedEffect(navigationState.isDrawerOpen) {
        if (!isTablet) {
            if (navigationState.isDrawerOpen) {
                drawerState.open()
            } else {
                drawerState.close()
            }
        }
    }

    LaunchedEffect(drawerState.currentValue) {
        if (!isTablet) {
            if (drawerState.currentValue == DrawerValue.Closed && navigationState.isDrawerOpen) {
                navigationViewModel.onEvent(NavigationUiEvent.CloseDrawer)
            }
        }
    }

    LaunchedEffect(navigationViewModel) {
        navigationViewModel.navigationUiEffect.collect { effect ->
            when (effect) {
                is NavigationUiEffect.NavigateToConversation -> {
                    Log.i(
                        "AppContent",
                        "NavigateToConversation effect received for conversationId: ${effect.itemId}"
                    )
                    navController.navigate(ChatRoute(conversationId = effect.itemId)) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            }
        }
    }

    if (isTablet) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(
                    drawerContainerColor = drawerContainerColor,
                    modifier = drawerBorderModifier,
                ) {
                    AppDrawer(
                        drawerItems = navigationState.items,
                        onEvent = navigationViewModel::onEvent,
                        isTablet = true,
                        currentNavigationItemId = navigationState.currentNavigationItemId
                    )
                }
            }
        ) {
            NavGraph(
                navController = navController,
                windowSizeClass = windowSizeClass,
                onToggleDrawer = { },
                onConversationDeleted = {
                    navigationViewModel.onEvent(NavigationUiEvent.StartNewChatClicked)
                },
                onConversationStarted = { conversationId ->
                    navigationViewModel.onEvent(
                        NavigationUiEvent.OnConversationStarted(conversationId)
                    )
                }
            )
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    AppDrawer(
                        drawerItems = navigationState.items,
                        onEvent = navigationViewModel::onEvent,
                        isTablet = false,
                        currentNavigationItemId = navigationState.currentNavigationItemId
                    )
                }
            }
        ) {
            NavGraph(
                navController = navController,
                windowSizeClass = windowSizeClass,
                onToggleDrawer = {
                    navigationViewModel.onEvent(NavigationUiEvent.ToggleDrawer)
                },
                onConversationDeleted = {
                    navigationViewModel.onEvent(NavigationUiEvent.StartNewChatClicked)
                },
                onConversationStarted = { conversationId ->
                    navigationViewModel.onEvent(
                        NavigationUiEvent.OnConversationStarted(conversationId)
                    )
                }
            )
        }
    }
}
