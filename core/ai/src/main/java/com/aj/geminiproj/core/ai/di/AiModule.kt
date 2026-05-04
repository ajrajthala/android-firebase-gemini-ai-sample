package com.aj.geminiproj.core.ai.di

import com.aj.geminiproj.core.ai.firebase.FirebaseAiClient
import com.aj.geminiproj.core.ai.firebase.data.AiRepositoryImpl
import com.aj.geminiproj.core.ai.firebase.domain.AiRepository
import org.koin.dsl.module

val aiModule = module {
    single { FirebaseAiClient() }
    single<AiRepository> { AiRepositoryImpl(get()) }
}