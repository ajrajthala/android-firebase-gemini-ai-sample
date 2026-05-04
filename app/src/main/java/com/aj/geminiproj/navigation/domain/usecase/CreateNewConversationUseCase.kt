package com.aj.geminiproj.navigation.domain.usecase

import com.aj.geminiproj.navigation.domain.repository.NavigationRepository

class CreateNewConversationUseCase(private val navigationRepository: NavigationRepository) {
    suspend operator fun invoke(currentConversationId: String? = null): String {
        return navigationRepository.startNewConversation(currentConversationId)
    }
}