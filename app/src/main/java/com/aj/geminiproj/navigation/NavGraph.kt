package com.aj.geminiproj.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.aj.geminiproj.features.chat.navigation.ChatRoute
import com.aj.geminiproj.features.chat.navigation.chatScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    onToggleDrawer: () -> Unit,
    onConversationDeleted: ()-> Unit,
    onConversationStarted: (String) -> Unit,
) {
    NavHost(navController = navController, startDestination = ChatRoute()) {
        chatScreen(
            onToggleDrawer = onToggleDrawer,
            onConversationDeleted = onConversationDeleted,
            onConversationStarted = onConversationStarted,
            windowSizeClass = windowSizeClass
        )
    }
}