package com.chouten.app.presentation.ui.components.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.chouten.app.common.Navigation
import com.chouten.app.domain.repository.NavigationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    navigationRepository: NavigationRepository,
) : ViewModel() {
    val bottomDestinations: List<Navigation.Destination> = navigationRepository.getNavigationItems()
    private lateinit var activeDestination: MutableState<String>

    fun getActiveDestination(): MutableState<String> {
        return activeDestination
    }

    fun setActiveDestination(destination: String) {
        if (::activeDestination.isInitialized) {
            activeDestination.value = destination
        } else {
            activeDestination = mutableStateOf(destination)
        }
    }
}