package com.aj.geminiproj.features.chat.presentation

import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aj.geminiproj.core.common.AndroidPermissionManager
import com.aj.geminiproj.features.chat.presentation.components.ChatInput
import com.aj.geminiproj.features.chat.presentation.components.MessageItem
import com.aj.geminiproj.features.chat.presentation.components.StreamingMessageBubble
import com.aj.geminiproj.features.chat.presentation.components.ToolStatusView
import com.aj.geminiproj.ui.util.isTablet
import org.koin.compose.koinInject
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
    androidPermissionManager: AndroidPermissionManager = koinInject(),
    windowSizeClass: WindowSizeClass,
) {
    val uiState by viewModel.uiState.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    //Responsive values
    val isTablet = remember(windowSizeClass) { windowSizeClass.isTablet() }
    Log.d("ChatScreen", "isTablet: $isTablet")
    val horizontalPadding: Dp = remember(isTablet) { if (isTablet) 32.dp else 8.dp }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            androidPermissionManager.onPermissionResult(isGranted)
        }

    //register permission launcher with the manager
    LaunchedEffect(Unit) {
        androidPermissionManager.registerLauncher(
            launcher = { permission ->
                permissionLauncher.launch(permission)
            },
            context = context
        )
    }
    // Scroll when messages list grows or streaming text updates
    LaunchedEffect(uiState.messages.size, uiState.streamingText) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size)
        }
    }

//    // scroll during streaming as text grows
//    LaunchedEffect(uiState.streamingText) {
//        if (uiState.isStreaming && uiState.streamingText.isNotEmpty()) {
//            listState.animateScrollToItem(uiState.messages.size - 1)
//        }
//    }

    //Permission rationale snackbar
    LaunchedEffect(uiState.showPermissionRationale) {
        if (uiState.showPermissionRationale) {
            val result = snackBarHostState.showSnackbar(
                message = "This permission is required to complete the requested action.",
                actionLabel = "Settings",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
            viewModel.clearError()
        }
    }

    // Error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackBarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
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

                is ChatUiEffect.ToolExecuting -> {
                    // Handle tool execution started (e.g., show loading indicator)
                }

                is ChatUiEffect.ToolCompleted -> {
                    // Handle tool execution completed (e.g., hide loading indicator)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        topBar = {
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

                    // Live-streaming bubble - shown during active response
                    if (uiState.isStreaming && uiState.streamingText.isNotEmpty()) {
                        item {
                            StreamingMessageBubble(
                                text = uiState.streamingText,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                ToolStatusView(
                    toolDisplayName = uiState.activeToolDisplay,
                    modifier = Modifier.fillMaxWidth()
                )

//

                // Streaming message
//                    if (uiState.isStreaming && uiState.streamingText.isNotEmpty()) {
//                        item {
//                            MessageItem(
//                                message = ChatMessage(
//                                    id = "streaming",
//                                    content = uiState.streamingText,
//                                    role = MessageRole.ASSISTANT,
//                                    status = MessageStatus.STREAMING,
//                                    timeStamp = System.currentTimeMillis()
//                                ),
//                                isTablet = isTablet
//                            )
//                        }
//                    }
//                }
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