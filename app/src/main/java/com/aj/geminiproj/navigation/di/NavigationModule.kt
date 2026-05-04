package com.aj.geminiproj.navigation.di

import com.aj.geminiproj.navigation.data.datasource.ConversationDataSource
import com.aj.geminiproj.navigation.data.datasource.ConversationDataSourceImpl
import com.aj.geminiproj.navigation.data.repository.NavigationRepositoryImpl
import com.aj.geminiproj.navigation.domain.repository.NavigationRepository
import com.aj.geminiproj.navigation.domain.usecase.CreateNewConversationUseCase
import com.aj.geminiproj.navigation.domain.usecase.GetNavigationItemsUseCase
import com.aj.geminiproj.navigation.presentation.AppNavigationViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val navigationModule = module {
    single<ConversationDataSource> { ConversationDataSourceImpl(get()) }
    single<NavigationRepository> { NavigationRepositoryImpl(get()) }

    factoryOf(::CreateNewConversationUseCase)
    factoryOf(::GetNavigationItemsUseCase)
    viewModelOf(::AppNavigationViewModel)
}