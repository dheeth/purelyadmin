package com.purelymail.admin.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purelymail.admin.data.repository.PurelymailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val tokenInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class OnboardingViewModel(
    private val repository: PurelymailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onTokenChanged(token: String) {
        _uiState.value = _uiState.value.copy(tokenInput = token, error = null)
    }

    fun connectWithToken(onSuccess: () -> Unit) {
        val token = _uiState.value.tokenInput.trim()
        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your Purelymail API token.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.tokenManager.saveToken(token)

            val result = repository.checkCredit()
            result.onSuccess {
                repository.tokenManager.completeOnboarding()
                repository.triggerGlobalRefresh()
                _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                onSuccess()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Authentication failed. Please verify your token."
                )
            }
        }
    }
}
