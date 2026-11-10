package com.purelymail.admin.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purelymail.admin.data.repository.PurelymailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiToken: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

class SettingsViewModel(
    private val repository: PurelymailRepository
) : ViewModel() {

    private val tokenManager = repository.tokenManager

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            apiToken = tokenManager.apiToken.value
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun saveToken(token: String) {
        val trimmed = token.trim()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, savedSuccessfully = false, errorMessage = null)
            tokenManager.saveToken(trimmed)
            repository.triggerGlobalRefresh()
            _uiState.value = _uiState.value.copy(
                apiToken = trimmed,
                isSaving = false,
                savedSuccessfully = true,
                errorMessage = null
            )
        }
    }

    fun clearSavedNotice() {
        _uiState.value = _uiState.value.copy(savedSuccessfully = false, errorMessage = null)
    }

    fun clearAllData() {
        tokenManager.clearAll()
        repository.triggerGlobalRefresh()
        _uiState.value = SettingsUiState()
    }

    fun resetOnboarding() {
        tokenManager.resetOnboarding()
    }
}
