package com.chouten.app.presentation.ui.screens.more.screens.module_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chouten.app.domain.model.ModuleModel
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

    private val _modules: MutableStateFlow<List<ModuleModel>> = MutableStateFlow(listOf())
    val modules: StateFlow<List<ModuleModel>> = _modules

    init {
        viewModelScope.launch {
            val modules = moduleUseCases.getModuleUris()
            val types = (moduleTypes.firstOrNull() ?: return@launch).toMutableSet()
            modules.forEach {
                types += it.subtypes
            }
            _moduleTypes.emit(types)
            _modules.emit(modules)
        }
    }

    fun refreshModules() = viewModelScope.launch {
        _modules.emit(moduleUseCases.getModuleUris())
    }
}