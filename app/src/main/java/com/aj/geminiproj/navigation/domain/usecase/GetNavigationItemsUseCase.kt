package com.aj.geminiproj.navigation.domain.usecase

import com.aj.geminiproj.navigation.presentation.NavigationItem
import com.aj.geminiproj.navigation.domain.repository.NavigationRepository
import kotlinx.coroutines.flow.Flow

class GetNavigationItemsUseCase(
    private val navigationRepository: NavigationRepository,
) {
    operator fun invoke(): Flow<List<NavigationItem>> {
       return navigationRepository.getNavigationItems()
    }
}