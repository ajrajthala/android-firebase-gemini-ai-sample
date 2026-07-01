package com.aj.geminiproj.features.chat.di

import com.aj.geminiproj.features.chat.data.repository.ChatRepositoryImpl
import com.aj.geminiproj.features.chat.domain.repository.ChatRepository
import com.aj.geminiproj.features.chat.domain.usecase.DeleteConversationUseCase
import com.aj.geminiproj.features.chat.domain.usecase.GenerateConversationTitleUseCase
import com.aj.geminiproj.features.chat.domain.usecase.GetConversationUseCase
import com.aj.geminiproj.features.chat.domain.usecase.ResetConversationUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SaveConversationUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SaveMessageUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SendMessageStreamUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SendMessageUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SendMessageWithImageStreamUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SendMessageWithImageUseCase
import com.aj.geminiproj.features.chat.domain.usecase.SendMessageWithAgentUseCase
import com.aj.geminiproj.features.chat.presentation.ChatViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    // Repository
    single<ChatRepository> { ChatRepositoryImpl(get(), get()) }

    // Use cases
    factory { SendMessageUseCase(get()) }
    factory { SendMessageStreamUseCase(get()) }
    factory { SendMessageWithImageUseCase(get()) }
    factory { SendMessageWithImageStreamUseCase(get()) }
    factory { GetConversationUseCase(get()) }
    factory { DeleteConversationUseCase(get()) }
    factory { SaveConversationUseCase(get()) }
    factory { GenerateConversationTitleUseCase(get()) }
    factory { SaveMessageUseCase(get()) }
    factory { SendMessageWithAgentUseCase(get()) }
    factory { ResetConversationUseCase(get()) }

    viewModel { params ->
        ChatViewModel(
            conversationId = params.get(),
            sendMessageStreamUseCase = get(),
            sendMessageWithImageStreamUseCase = get(),
            getConversationUseCase = get(),
            clearConversationUseCase = get(),
            saveConversationUseCase = get(),
            saveMessageUseCase = get(),
            generateConversationTitleUseCase = get(),
            sendMessageWithAgentUseCase = get(),
            resetConversationUseCase = get(),
        )
    }
}