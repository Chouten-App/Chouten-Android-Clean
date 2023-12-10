package com.chouten.app.presentation.ui.screens.more.screens.module_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.domain.use_case.module_use_cases.ModuleUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModuleViewModel @Inject constructor(
    private val moduleUseCases: ModuleUseCases
) : ViewModel() {
    private val _moduleTypes: MutableStateFlow<Set<String>> = MutableStateFlow(setOf())
    val moduleTypes: StateFlow<Set<String>> = _moduleTypes

    init {
        viewModelScope.launch {
            val types = (moduleTypes.firstOrNull() ?: return@launch).toMutableSet()
            moduleUseCases.getModuleUris().forEach {
                types += it.subtypes
            }
            _moduleTypes.emit(types)
        }
    }
}