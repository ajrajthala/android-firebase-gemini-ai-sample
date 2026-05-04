package com.aj.geminiproj.features.chat.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aj.geminiproj.features.chat.presentation.ChatScreen
import kotlinx.serialization.Serializable

@Serializable
data class ChatRoute
    (val conversationId: String = "new")

fun NavGraphBuilder.chatScreen(
    onToggleDrawer: () -> Unit,
    onConversationDeleted: () -> Unit,
    onConversationStarted: (String) -> Unit,
    windowSizeClass: WindowSizeClass,
) {
    composable<ChatRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ChatRoute>()
        ChatScreen(
            conversationId = route.conversationId,
            onConversationDeleted = onConversationDeleted,
            onConversationStarted = onConversationStarted,
            onToggleDrawer = onToggleDrawer,
            windowSizeClass = windowSizeClass
        )
    }
}