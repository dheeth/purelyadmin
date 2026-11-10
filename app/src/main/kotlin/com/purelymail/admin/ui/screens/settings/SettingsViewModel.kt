package com.purelymail.admin.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purelymail.admin.data.repository.PurelymailRepository
import com.purelymail.admin.data.repository.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiToken: String = "",
    val serverUrl: String = TokenManager.DEFAULT_SERVER_URL,
    val isDemoMode: Boolean = false,
    val isTestingConnection: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.Idle,
    val successMessage: String? = null
)

sealed interface ConnectionStatus {
    object Idle : ConnectionStatus
    object Success : ConnectionStatus
    data class Error(val message: String) : ConnectionStatus
}

class SettingsViewModel(
    private val repository: PurelymailRepository
) : ViewModel() {

    private val tokenManager = repository.tokenManager

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            apiToken = tokenManager.apiToken.value,
            serverUrl = tokenManager.serverUrl.value,
            isDemoMode = tokenManager.isDemoMode.value
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateToken(token: String) {
        tokenManager.saveToken(token)
        _uiState.value = _uiState.value.copy(apiToken = token, connectionStatus = ConnectionStatus.Idle)
    }

    fun updateServerUrl(url: String) {
        tokenManager.saveServerUrl(url)
        _uiState.value = _uiState.value.copy(serverUrl = url, connectionStatus = ConnectionStatus.Idle)
    }

    fun toggleDemoMode(enabled: Boolean) {
        tokenManager.setDemoMode(enabled)
        _uiState.value = _uiState.value.copy(isDemoMode = enabled, connectionStatus = ConnectionStatus.Idle)
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingConnection = true, connectionStatus = ConnectionStatus.Idle)
            val result = repository.checkCredit()
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionStatus = ConnectionStatus.Success
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionStatus = ConnectionStatus.Error(err.message ?: "Authentication failed")
                )
            }
        }
    }

    fun clearAllData() {
        tokenManager.clearAll()
        _uiState.value = SettingsUiState()
    }

    fun resetOnboarding() {
        tokenManager.resetOnboarding()
    }
}
