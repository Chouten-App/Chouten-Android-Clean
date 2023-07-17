package com.chouten.app.domain.use_case.navigation_use_cases

import com.chouten.app.common.Navigation
import com.chouten.app.domain.repository.NavigationRepository
import javax.inject.Inject

class GetNavigationItemsUseCase @Inject constructor(
    private val navigationRepository: NavigationRepository,
) {
    operator fun invoke(): List<Navigation.Destination> {
        return navigationRepository.getNavigationItems()
    }
}