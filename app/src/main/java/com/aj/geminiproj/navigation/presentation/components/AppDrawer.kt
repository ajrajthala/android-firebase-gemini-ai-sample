package com.aj.geminiproj.navigation.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aj.geminiproj.R
import com.aj.geminiproj.navigation.presentation.NavigationItem
import com.aj.geminiproj.navigation.presentation.NavigationUiEvent

@Composable
fun AppDrawer(
    drawerItems: List<NavigationItem>,
    onEvent: (NavigationUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false,
    currentNavigationItemId: String? = null,
) {
    val isConversationSelected =
        currentNavigationItemId != null && drawerItems.any { it.id == currentNavigationItemId }

    Column(
        modifier = modifier
            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        //Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            if (!isTablet) {
                IconButton(onClick = {
                    onEvent(NavigationUiEvent.ToggleDrawer)
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Close Drawer"
                    )
                }
            }
        }

        NavigationItemView(
            item = NavigationItem.NewChat,
            onEvent = onEvent,
            isSelected = currentNavigationItemId == null || !isConversationSelected
        )

        if (drawerItems.isNotEmpty()) {
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Recent chats",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // navigationItems
        LazyColumn {
            items(drawerItems, key = { it.id }) { drawerItem ->
                NavigationItemView(
                    item = drawerItem,
                    onEvent = onEvent,
                    isSelected = drawerItem.id == currentNavigationItemId
                )
            }
        }
    }
}