package com.aj.geminiproj.features.chat.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aj.geminiproj.core.model.ChatConversation
import com.aj.geminiproj.core.model.ChatMessage
import com.aj.geminiproj.core.model.MessageRole
import com.aj.geminiproj.core.model.MessageStatus
import com.aj.geminiproj.core.model.StreamState
import com.aj.geminiproj.features.chat.domain.usecase.DeleteConversationUseCase
import com.aj.geminiproj.features.chat.domain.usecase.GenerateConversationTitleUseCase
import com.aj.geminiproj.features.chat.domain.usecase.GetConversationUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SaveConversationUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SaveMessageUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SendMessageStreamUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val conversationId: String,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendMessageStreamUseCase: SendMessageStreamUseCase,
    private val saveConversationUseCase: SaveConversationUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val clearConversationUseCase: DeleteConversationUseCase,
    private val generateConversationTitleUseCase: GenerateConversationTitleUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState(conversationId = conversationId))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _uiEffect = Channel<ChatUiEffect>()
    val uiEffect: Flow<ChatUiEffect> = _uiEffect.receiveAsFlow()

    init {
        Log.d("ChatViewModel", "Init with conversationId: $conversationId")
        loadConversation()
    }

    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.OnMessageTextChanged -> {
                _inputText.update { event.text }
            }

            ChatUiEvent.OnSendMessage -> {
                sendMessage()
            }

            ChatUiEvent.OnRetry -> {
                retry()
            }

            ChatUiEvent.OnDeleteChat -> {
                deleteChat()
            }

            ChatUiEvent.OnDismissError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    fun loadConversation() {
        viewModelScope.launch {
            if (conversationId == "new") {
                _uiState.update { ChatUiState(conversationId = conversationId) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            // load exisitng conversation
            try {
                val conversation = getConversationUseCase(conversationId)
                _uiState.update {
                    it.copy(
                        title = conversation.title,
                        conversationId = conversationId,
                        messages = conversation.messages,
                        createdAt = conversation.createdAt,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load conversation"
                    )
                }
            }
        }

    }

    private fun sendMessage(addMessage: Boolean = true) {
        val messageText = _inputText.value.trim()
        if (messageText.isBlank()) return

        // Resolve a real UUID if this is a new conversation
        val currentConversationId = _uiState.value.conversationId.let { id ->
            if (id == "new" || id.isBlank()) {
                val newId = UUID.randomUUID().toString()
                _uiState.update { it.copy(conversationId = newId) }
                viewModelScope.launch {
                    _uiEffect.send(ChatUiEffect.ConversationStarted(newId))
                }
                newId
            } else {
                id
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isStreaming = false, isLoading = true) }
            _uiEffect.send(ChatUiEffect.ClearInput)

            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = messageText,
                role = MessageRole.USER,
                status = MessageStatus.SENT,
                timeStamp = System.currentTimeMillis()
            )

            _inputText.update { "" }

            _uiState.update { state ->
                state.copy(messages = state.messages + userMessage)
            }
            _uiEffect.send(ChatUiEffect.ScrollToBottom)

            // save conversation after adding user message; for retries we do not add a new message,
            if (addMessage) {
                saveConversationAfterMessage(currentConversationId, userMessage)
            }
            // Send message to AI
            sendMessageStreamUseCase(messageText, currentConversationId, _uiState.value.messages)
                .collect { streamState ->
                    handleStreamState(streamState, currentConversationId)
                }
        }
    }


    private suspend fun handleStreamState(
        streamState: StreamState<String>,
        conversationId: String,
    ) {
        when (streamState) {

            StreamState.Idle -> {
            }

            StreamState.Loading -> _uiState.update {
                it.copy(
                    isLoading = true, isStreaming = false
                )
            }

            is StreamState.Streaming -> {
                if (streamState.isComplete) {
                    val aiMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = streamState.partialData,
                        role = MessageRole.ASSISTANT,
                        status = MessageStatus.SENT,
                        timeStamp = System.currentTimeMillis()
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + aiMessage,
                            isStreaming = false,
                            isLoading = false,
                            streamingText = ""
                        )
                    }

                    // save conversation response
                    saveConversationAfterMessage(conversationId, aiMessage)
                    _uiEffect.send(ChatUiEffect.ScrollToBottom)
                } else {
                    _uiState.update {
                        it.copy(
                            streamingText = streamState.partialData,
                            isStreaming = true,
                            isLoading = false
                        )
                    }
                }
            }

            is StreamState.Success -> {
                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    content = streamState.data,
                    role = MessageRole.ASSISTANT,
                    status = MessageStatus.SENT,
                    timeStamp = System.currentTimeMillis()
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + aiMessage,
                        isStreaming = false,
                        isLoading = false,
                        streamingText = ""
                    )
                }
                saveConversationAfterMessage(conversationId, aiMessage)
                _uiEffect.send(ChatUiEffect.ScrollToBottom)
            }

            is StreamState.Error -> {
                _uiState.update {
                    it.copy(
                        isStreaming = false,
                        isLoading = false,
                        streamingText = "",
                        error = streamState.message
                    )
                }
                _uiEffect.send(
                    ChatUiEffect.ShowError(
                        streamState.message ?: "Unknown error"
                    )
                )
            }
        }
    }

    private suspend fun saveConversationAfterMessage(conversationId: String, message: ChatMessage) {
        try {
            val currentState = _uiState.value
            val updatedMessages = currentState.messages

            val needsTitle = currentState.title.isEmpty() || currentState.title == "New Chat"
            val title = if (needsTitle) {
                generateConversationTitleUseCase(updatedMessages).also { generatedTitle ->
                    _uiState.update { it.copy(title = generatedTitle) }
                }
            } else {
                currentState.title
            }

            val conversation = ChatConversation(
                id = conversationId,
                title = title,
                messages = updatedMessages,
                createdAt = currentState.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            saveConversationUseCase(conversation)
            saveMessageUseCase(message, conversationId)
        } catch (e: Exception) {
            // Handle save error if needed
        }
    }

    private fun retry() {
        val lastUserMessage = uiState.value.messages.lastOrNull { it.role == MessageRole.USER }
        lastUserMessage?.let { message ->
            _uiState.update { state -> state.copy(error = null) }
            sendMessage(addMessage = false)
        }
    }

    private fun deleteChat() {
        val currentConversationId = _uiState.value.conversationId
        if (currentConversationId.isEmpty() || currentConversationId == "new") return
        viewModelScope.launch {
            try {
                clearConversationUseCase(currentConversationId)
                _uiState.update {
                    it.copy(
                        title = "New Chat",
                        conversationId = "new",
                        messages = emptyList(),
                        error = null,
                        streamingText = "",
                    )
                }
                _uiEffect.send(ChatUiEffect.ChatDeleted)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to clear conversation"
                    )
                }
            }
        }
    }

}