package com.chouten.app.domain.use_case.navigation_use_cases

import com.chouten.app.domain.repository.NavigationRepository
import javax.inject.Inject

class GetActiveDestinationUseCase @Inject constructor(
    private val navigationRepository: NavigationRepository,
) {
    operator fun invoke() = navigationRepository.getActiveDestination()
}