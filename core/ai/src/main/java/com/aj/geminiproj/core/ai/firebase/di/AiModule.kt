package com.aj.geminiproj.core.ai.firebase.di

import com.aj.geminiproj.core.ai.firebase.FirebaseAiClient
import com.aj.geminiproj.core.ai.firebase.data.AiRepositoryImpl
import com.aj.geminiproj.core.ai.firebase.dispatcher.ToolDispatcher
import com.aj.geminiproj.core.ai.firebase.domain.AiRepository
import com.aj.geminiproj.core.ai.firebase.mapper.FirebaseToolMapper
import com.aj.geminiproj.core.ai.firebase.orchestration.GeminiOrchestrator
import com.aj.geminiproj.core.ai.firebase.registry.ToolRegistry
import org.koin.dsl.module

val aiModule = module {
    single { FirebaseAiClient() }
    single<AiRepository> { AiRepositoryImpl(get()) }

    single { FirebaseToolMapper() }
    single { ToolRegistry() }
    single { ToolDispatcher(registry = get()) }

    single {
        GeminiOrchestrator(
            registry = get(),
            dispatcher = get(),
            mapper = get()
        )
    }
}