package com.aj.geminiproj.navigation.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem {
    abstract val id: String
    abstract val title: String
    abstract val icon: ImageVector

    data object NewChat : NavigationItem() {
        override val id: String = "new_chat"
        override val title: String = "New Chat"
        override val icon: ImageVector = Icons.Default.Add
    }

    data class ConversationItem(
        override val id: String,
        override val title: String,
        val timeStamp: Long,
        val lastMessage: String,
    ) : NavigationItem() {
        override val icon: ImageVector = ICON

        companion object {
            val ICON: ImageVector = Icons.AutoMirrored.Filled.Chat
        }
    }
}

data class NavigationUiState(
    val isDrawerOpen: Boolean = false,
    val currentNavigationItemId: String? = null,
    val currentRoute: String = "",
    val isLoading: Boolean = false,
    val items: List<NavigationItem> = emptyList(),
)

sealed interface NavigationUiEvent {
    data object StartNewChatClicked : NavigationUiEvent
    data class OnConversationSelected(val conversationId: String) : NavigationUiEvent

    data class OnConversationStarted(val conversationId: String) : NavigationUiEvent
    data object ToggleDrawer : NavigationUiEvent
    data object CloseDrawer : NavigationUiEvent
}

sealed interface NavigationUiEffect {
    //    data object OpenDrawer : NavigationUiEffect
//    data object CloseDrawer : NavigationUiEffect
    data class NavigateToConversation(val itemId: String) : NavigationUiEffect
}

data class ConversationSummary(
    val id: String,
    val title: String,
    val lastMessage: String?,
    val updatedAt: Long,
)