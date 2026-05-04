package com.aj.geminiproj.navigation.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.aj.geminiproj.navigation.presentation.NavigationItem
import com.aj.geminiproj.navigation.presentation.NavigationUiEvent

@Composable
fun NavigationItemView(
    item: NavigationItem,
    onEvent: (NavigationUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = true,
) {
    val clickEvent = remember(item.id) {
        when (item) {
            is NavigationItem.ConversationItem -> {
                { onEvent(NavigationUiEvent.OnConversationSelected(item.id)) }
            }

            is NavigationItem.NewChat -> {
                { onEvent(NavigationUiEvent.StartNewChatClicked) }
            }
        }
    }

    when (item) {
        is NavigationItem.ConversationItem -> {
            ConversationCard(
                item = item,
                onClick = clickEvent,
                isSelected = isSelected
            )
        }

        is NavigationItem.NewChat -> {
            NavigationItemCard(
                icon = item.icon,
                title = item.title,
                onClick = clickEvent,
                isSelected = isSelected
            )

        }
    }
}