package com.chouten.app.domain.use_case.navigation_use_cases

import com.chouten.app.domain.repository.NavigationRepository
import javax.inject.Inject

class SetActiveNavigationItemUseCase @Inject constructor(
    private val navigationRepository: NavigationRepository,
) {
    suspend operator fun invoke(destination: String) {
        navigationRepository.setActiveDestination(destination)
    }
}