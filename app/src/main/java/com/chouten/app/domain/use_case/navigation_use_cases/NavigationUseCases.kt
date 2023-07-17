package com.chouten.app.domain.use_case.navigation_use_cases

data class NavigationUseCases(
    val getNavigationItems: GetNavigationItemsUseCase,
    val getActiveDestination: GetActiveDestinationUseCase,
    val setActiveNavigationItem: SetActiveNavigationItemUseCase,
)
