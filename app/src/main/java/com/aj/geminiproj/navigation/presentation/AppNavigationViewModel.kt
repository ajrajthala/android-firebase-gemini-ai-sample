package com.aj.geminiproj.navigation.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aj.geminiproj.navigation.domain.usecase.CreateNewConversationUseCase
import com.aj.geminiproj.navigation.domain.usecase.GetNavigationItemsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppNavigationViewModel(
    private val getNavigationItemsUseCase: GetNavigationItemsUseCase,
    private val createNewConversationUseCase: CreateNewConversationUseCase,
) : ViewModel() {
    private val _navigationUiState = MutableStateFlow(NavigationUiState())
    val navigationUiState: StateFlow<NavigationUiState> = _navigationUiState.asStateFlow()

    private val _navigationUiEffect = Channel<NavigationUiEffect>(Channel.BUFFERED)
    val navigationUiEffect = _navigationUiEffect.receiveAsFlow()

    init {
        loadNavigationItems()
    }

    fun onEvent(event: NavigationUiEvent) {
        when (event) {
            is NavigationUiEvent.OnConversationSelected -> navigateToConversation(event.conversationId)
            NavigationUiEvent.StartNewChatClicked -> startNewChat()
            NavigationUiEvent.ToggleDrawer -> toggleDrawer()
            NavigationUiEvent.CloseDrawer -> viewModelScope.launch { closeDrawer() }
            is NavigationUiEvent.OnConversationStarted -> onConversationStarted(event.conversationId)
        }
    }

    private fun onConversationStarted(conversationId: String) {
        val newNavigationItemId = "conversation_$conversationId"
        _navigationUiState.update {
            it.copy(
                currentNavigationItemId = newNavigationItemId
            )
        }
        Log.d(
            "AppNavigationViewModel",
            "onConversationStarted: Updated drawer selection to $conversationId (no navigation)"
        )
    }

    private fun toggleDrawer() {
        val isDrawerOpen = _navigationUiState.value.isDrawerOpen
        _navigationUiState.update { it.copy(isDrawerOpen = !isDrawerOpen) }
    }

    private fun closeDrawer() {
        Log.d(
            "AppNavigationViewModel",
            "closeDrawer: called, DRAWER OPEN: " + _navigationUiState.value.isDrawerOpen
        )
        _navigationUiState.update { it.copy(isDrawerOpen = false) }
    }

    private fun loadNavigationItems() {
        viewModelScope.launch {
            _navigationUiState.update { it.copy(isLoading = true) }
            getNavigationItemsUseCase().collect { items ->
                _navigationUiState.update {
                    it.copy(
                        items = items,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun startNewChat() {
        viewModelScope.launch {
            val currentConversationId = _navigationUiState.value
                .currentNavigationItemId?.removePrefix("conversation_")
            val conversationId = createNewConversationUseCase(currentConversationId)
            val newNavigationItemId = "conversation_$conversationId"

            val isDifferentConversation =
                _navigationUiState.value.currentNavigationItemId != newNavigationItemId
            _navigationUiState.update {
                it.copy(
                    currentNavigationItemId = newNavigationItemId
                )
            }
            closeDrawer()
            Log.d(
                "AppNavigationViewModel",
                "startNewChat: $conversationId" + ", IS DIFFERENT: " + isDifferentConversation
            )
            if (isDifferentConversation) {
                _navigationUiEffect.send(NavigationUiEffect.NavigateToConversation(conversationId))
            }

        }
    }

    private fun navigateToConversation(navigationItemId: String) {
        viewModelScope.launch {
            val isDifferentConversation =
                _navigationUiState.value.currentNavigationItemId != navigationItemId
            _navigationUiState.update {
                it.copy(
                    currentNavigationItemId = navigationItemId
                )
            }
            Log.d(
                "AppNavigationViewModel",
                "navigateToConversation: $navigationItemId" + ", DRAWER OPEN: " + _navigationUiState.value.isDrawerOpen
            )
            closeDrawer()
            if (isDifferentConversation) {
                val conversationId = navigationItemId.removePrefix("conversation_")
                _navigationUiEffect.send(NavigationUiEffect.NavigateToConversation(conversationId))
            }
        }
    }
}