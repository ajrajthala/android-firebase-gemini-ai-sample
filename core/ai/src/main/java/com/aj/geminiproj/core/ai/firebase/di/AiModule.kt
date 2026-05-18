package com.aj.geminiproj.core.ai.firebase.di

import com.aj.geminiproj.core.ai.firebase.FirebaseAiClient
import com.aj.geminiproj.core.ai.firebase.agent.AgentContextBuilder
import com.aj.geminiproj.core.ai.firebase.agent.ConversationStateManager
import com.aj.geminiproj.core.ai.firebase.agent.SemanticDomainRegistry
import com.aj.geminiproj.core.ai.firebase.agent.SemanticDomainResolver
import com.aj.geminiproj.core.ai.firebase.data.AiRepositoryImpl
import com.aj.geminiproj.core.ai.firebase.dispatcher.ToolDispatcher
import com.aj.geminiproj.core.ai.firebase.domain.AiRepository
import com.aj.geminiproj.core.ai.firebase.mapper.FirebaseToolMapper
import com.aj.geminiproj.core.ai.firebase.orchestration.GeminiOrchestrator
import org.koin.dsl.module

val aiModule = module {
    single { FirebaseAiClient() }
    single<AiRepository> { AiRepositoryImpl(get(), get()) }

    single { FirebaseToolMapper() }
    single { SemanticDomainRegistry() }
    single { ToolDispatcher(registry = get()) }

    single { AgentContextBuilder() }
    single { SemanticDomainResolver(registry = get()) }
    single { ConversationStateManager(contextBuilder = get()) }

    single {
        GeminiOrchestrator(
            domainRegistry = get(),
            dispatcher = get(),
            mapper = get()
        )
    }
}