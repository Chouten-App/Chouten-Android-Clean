package com.chouten.app.data.repository

import com.chouten.app.common.Navigation
import com.chouten.app.domain.repository.NavigationRepository

class NavigationRepositoryImpl() : NavigationRepository {

    private val destinations: List<Navigation.Destination> =
        Navigation.Destination.values().toList()

    override fun getNavigationItems(): List<Navigation.Destination> {
        return destinations
    }
}