package com.aj.geminiproj.core.ai.firebase.di

import com.aj.geminiproj.core.ai.firebase.FirebaseAiClient
import com.aj.geminiproj.core.ai.firebase.FirebaseGenerativeModelFactory
import com.aj.geminiproj.core.ai.firebase.GenerativeModelFactory
import com.aj.geminiproj.core.ai.firebase.agent.ConversationAgent
import com.aj.geminiproj.core.ai.firebase.common.AndroidLogger
import com.aj.geminiproj.core.ai.firebase.common.Logger
import com.aj.geminiproj.core.ai.firebase.context.AgentContextBuilder
import com.aj.geminiproj.core.ai.firebase.domain.AiRepository
import com.aj.geminiproj.core.ai.firebase.data.AiRepositoryImpl
import com.aj.geminiproj.core.ai.firebase.dispatcher.ToolDispatcher
import com.aj.geminiproj.core.ai.firebase.dispatcher.ToolValidator
import com.aj.geminiproj.core.ai.firebase.ingelligence.ParallelToolExecutor
import com.aj.geminiproj.core.ai.firebase.ingelligence.ToolResultCache
import com.aj.geminiproj.core.ai.firebase.mapper.FirebaseToolMapper
import com.aj.geminiproj.core.ai.firebase.observability.AgentTracer
import com.aj.geminiproj.core.ai.firebase.orchestration.GeminiOrchestrator
import com.aj.geminiproj.core.ai.firebase.reliability.RetryHandler
import com.aj.geminiproj.core.ai.firebase.resolver.DomainResolverFallback
import com.aj.geminiproj.core.ai.firebase.resolver.SemanticDomainRegistry
import com.aj.geminiproj.core.ai.firebase.resolver.SemanticDomainResolver
import com.aj.geminiproj.core.ai.firebase.safety.InputGuardrail
import com.aj.geminiproj.core.ai.firebase.session.ConversationStateManager
import com.aj.geminiproj.core.ai.firebase.session.HistorySummarizer
import com.aj.geminiproj.core.model.tool.Tool
import org.koin.dsl.module

const val GEMINI_MODEL = "gemini-3-flash-preview"
val aiModule = module {
    // ---- Android bound --------------
    single<GenerativeModelFactory> { FirebaseGenerativeModelFactory() }
    single<Logger> { AndroidLogger() }

    // ---- Infrastructure ----------------
    single { RetryHandler(logger = get()) }
    single { FirebaseToolMapper() }
    single { FirebaseAiClient() }
    single<AiRepository> { AiRepositoryImpl(get(), get()) }

    // ---- Domain -----------
    single { AgentContextBuilder() }
    single { SemanticDomainRegistry(allTools = getKoin().getAll<Tool>(), logger = get()) }
    single { SemanticDomainResolver(registry = get(), logger = get(), modelFactory = get()) }
    single { DomainResolverFallback(registry = get(), logger = get()) }
    single { ConversationStateManager(contextBuilder = get(), logger = get()) }
    single { HistorySummarizer(modelFactory = get(), logger = get()) }
    single { AgentTracer(logger = get()) }
    single { InputGuardrail(logger = get()) }
    single { ToolValidator(logger = get()) }
    single { ToolDispatcher(registry = get(), validator = get()) }
    single { ToolResultCache(logger = get()) }
    single { ParallelToolExecutor(dispatcher = get(), cache = get(), logger = get()) }

    // ---- Orchestration -------------
    single {
        GeminiOrchestrator(
            modelFactory = get(),
            domainRegistry = get(),
            dispatcher = get(),
            mapper = get(),
            tracer = get(),
            parallelToolExecutor = get(),
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
            retryHandler = get(),
            logger = get()
        )
    }
}