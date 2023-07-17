package com.chouten.app.data.repository

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.chouten.app.common.Navigation
import com.chouten.app.domain.repository.NavigationRepository
import com.chouten.app.domain.repository.NavigationRepository.Companion.defaultDestination

class NavigationRepositoryImpl : NavigationRepository {

    private val destinations: List<Navigation.Destination> =
        Navigation.Destination.values().toList()

    private var activeDestination = mutableStateOf(defaultDestination)

    override fun getNavigationItems() = destinations

    override fun getActiveDestination(): State<String> = activeDestination
    
    override suspend fun setActiveDestination(destination: String) {
        activeDestination.value = destination
    }
}