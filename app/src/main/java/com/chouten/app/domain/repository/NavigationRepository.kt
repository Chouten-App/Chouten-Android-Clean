package com.chouten.app.domain.repository

import com.chouten.app.common.Navigation

interface NavigationRepository {
    fun getNavigationItems(): List<Navigation.Destination>
}