package com.chouten.app.presentation.ui.components.navigation

import androidx.lifecycle.ViewModel
import com.chouten.app.domain.use_case.navigation_use_cases.NavigationUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val navUseCases: NavigationUseCases,
) : ViewModel() {
    val bottomDestinations = navUseCases.getNavigationItems()
    private val activeDestination = navUseCases.getActiveDestination()

    fun getActiveDestination() = activeDestination

    suspend fun setActiveDestination(destination: String) {
        navUseCases.setActiveNavigationItem(destination)
    }
}