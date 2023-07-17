package com.chouten.app.domain.repository

import androidx.compose.runtime.State
import com.chouten.app.common.Navigation

interface NavigationRepository {

    /**
     * Returns a list of all navigation items from
     * of type [Navigation.Destination].
     */
    fun getNavigationItems(): List<Navigation.Destination>

    /**
     * Returns the current active destination as immutable state.
     */
    fun getActiveDestination(): State<String>

    /**
     * Sets the current active destination.
     */
    suspend fun setActiveDestination(destination: String)

    companion object {
        /** Route used on application start. */
        const val defaultDestination = Navigation.HomeRoute
    }
}