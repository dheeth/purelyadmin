package com.purelymail.admin.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purelymail.admin.data.model.ApiDomainInfo
import com.purelymail.admin.data.model.CheckCreditResponse
import com.purelymail.admin.data.repository.DashboardSummary
import com.purelymail.admin.data.repository.PurelymailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val summary: DashboardSummary? = null,
    val error: String? = null,
    val generatedQuickAlias: String? = null
)

class DashboardViewModel(
    private val repository: PurelymailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            repository.refreshSignal.collect {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getDashboardSummary()
            result.onSuccess { summary ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    summary = summary,
                    error = null
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to load dashboard data"
                )
            }
        }
    }

    fun clearGeneratedAlias() {
        _uiState.value = _uiState.value.copy(generatedQuickAlias = null)
    }
}
