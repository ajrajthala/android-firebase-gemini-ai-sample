package com.aj.geminiproj.core.ai.firebase.di

import com.aj.geminiproj.core.ai.firebase.FirebaseAiClient
import com.aj.geminiproj.core.ai.firebase.agent.AgentContextBuilder
import com.aj.geminiproj.core.ai.firebase.agent.AgentTracer
import com.aj.geminiproj.core.ai.firebase.agent.ConversationAgent
import com.aj.geminiproj.core.ai.firebase.agent.ConversationStateManager
import com.aj.geminiproj.core.ai.firebase.agent.DomainResolverFallback
import com.aj.geminiproj.core.ai.firebase.agent.HistorySummarizer
import com.aj.geminiproj.core.ai.firebase.agent.InputGuardrail
import com.aj.geminiproj.core.ai.firebase.agent.SemanticDomainRegistry
import com.aj.geminiproj.core.ai.firebase.agent.SemanticDomainResolver
import com.aj.geminiproj.core.ai.firebase.data.AiRepositoryImpl
import com.aj.geminiproj.core.ai.firebase.dispatcher.ToolDispatcher
import com.aj.geminiproj.core.ai.firebase.dispatcher.ToolValidator
import com.aj.geminiproj.core.ai.firebase.domain.AiRepository
import com.aj.geminiproj.core.ai.firebase.mapper.FirebaseToolMapper
import com.aj.geminiproj.core.ai.firebase.orchestration.GeminiOrchestrator
import org.koin.dsl.module

val aiModule = module {
    single { FirebaseAiClient() }
    single<AiRepository> { AiRepositoryImpl(get(), get()) }

    single { FirebaseToolMapper() }
    single { SemanticDomainRegistry() }
    single { ToolValidator() }
    single { ToolDispatcher(registry = get(), validator = get()) }

    single { AgentContextBuilder() }
    single { SemanticDomainResolver(registry = get()) }
    single { DomainResolverFallback(registry = get()) }
    single { ConversationStateManager(contextBuilder = get()) }
    single { HistorySummarizer() }
    single { AgentTracer() }
    single { InputGuardrail() }

    single {
        GeminiOrchestrator(
            domainRegistry = get(),
            dispatcher = get(),
            mapper = get(),
            tracer = get(),
        )
    }

    single {
        ConversationAgent(
            stateManager = get(),
            domainResolver = get(),
            domainResolverFallback = get(),
            domainRegistry = get(),
            tracer = get(),
            orchestrator = get(),
            guardrail = get(),
            historySummarizer = get(),
        )
    }
}