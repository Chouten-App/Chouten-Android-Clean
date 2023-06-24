package com.chouten.app.data.repository

import com.chouten.app.common.Navigation

interface NavigationRepository {
    fun getNavigationItems(): List<Navigation.Destination>
}