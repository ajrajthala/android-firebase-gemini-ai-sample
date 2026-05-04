package com.aj.geminiproj.features.chat.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aj.geminiproj.core.model.ChatMessage
import com.aj.geminiproj.core.model.MessageRole
import com.aj.geminiproj.core.model.MessageStatus
import com.aj.geminiproj.features.chat.presentation.components.ChatInput
import com.aj.geminiproj.features.chat.presentation.components.MessageItem
import com.aj.geminiproj.ui.util.isTablet
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String = "new",
    onToggleDrawer: () -> Unit,
    onConversationDeleted: () -> Unit,
    onConversationStarted: (String) -> Unit,
    viewModel: ChatViewModel = koinViewModel(
        key = conversationId,
        parameters = { parametersOf(conversationId) }),
    windowSizeClass: WindowSizeClass,
) {
    val uiState by viewModel.uiState.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    //Responsive values
    val isTablet = remember(windowSizeClass) { windowSizeClass.isTablet() }
    Log.d("ChatScreen", "isTablet: $isTablet")
    val horizontalPadding: Dp = remember(isTablet) { if (isTablet) 32.dp else 8.dp }

    // Scroll when messages list grows
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // scroll during streaming as text grows
    LaunchedEffect(uiState.streamingText) {
        if (uiState.isStreaming && uiState.streamingText.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Handle UI effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                ChatUiEffect.ClearInput -> {
                }

                ChatUiEffect.ScrollToBottom -> {
                }

                is ChatUiEffect.ShowError -> {}
                ChatUiEffect.ChatDeleted -> {
                    onConversationDeleted()
                }

                is ChatUiEffect.ConversationStarted -> {
                    onConversationStarted(effect.conversationId)
                }
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(uiState.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                IconButton(onClick = onToggleDrawer) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            },
            actions = {
                if (uiState.conversationId.isNotEmpty() && uiState.conversationId != "new") {
                    IconButton(onClick = {
                        showDeleteDialog = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Conversation")
                    }
                }
            })
    }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter

        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .then(
                        if (isTablet) Modifier.widthIn(max = 840.dp)
                        else Modifier.fillMaxWidth()
                    )
            ) {
                if (uiState.error != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 8.dp),
                        colors = cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.error ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )

                            TextButton(onClick = { viewModel.onEvent(ChatUiEvent.OnRetry) }) {
                                Text(text = "Retry")
                            }
                        }
                    }
                }

                //Messages
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = horizontalPadding,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->

                        MessageItem(
                            message = message,
                            isTablet = isTablet
                        )
                    }

                    if (uiState.isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }

                    // Streaming message
                    if (uiState.isStreaming && uiState.streamingText.isNotEmpty()) {
                        item {
                            MessageItem(
                                message = ChatMessage(
                                    id = "streaming",
                                    content = uiState.streamingText,
                                    role = MessageRole.ASSISTANT,
                                    status = MessageStatus.STREAMING,
                                    timeStamp = System.currentTimeMillis()
                                ),
                                isTablet = isTablet
                            )
                        }
                    }
                }
                //Input
                ChatInput(
                    text = inputText,
                    onTextChange = { viewModel.onEvent(ChatUiEvent.OnMessageTextChanged(it)) },
                    onSendClick = { viewModel.onEvent(ChatUiEvent.OnSendMessage) },
                    enabled = uiState.canSendMessage,
                    isTablet = isTablet
                )
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Conversation") },
                text = { Text("Are you sure you want to delete this conversation?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onEvent(ChatUiEvent.OnDeleteChat)
                        showDeleteDialog = false
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}