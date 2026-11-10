package com.purelymail.admin.ui.screens.domains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purelymail.admin.data.model.ApiDomainInfo
import com.purelymail.admin.data.model.UpdateDomainSettingsRequest
import com.purelymail.admin.data.repository.PurelymailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DomainsUiState(
    val isLoading: Boolean = false,
    val domains: List<ApiDomainInfo> = emptyList(),
    val ownershipCode: String? = null,
    val selectedDomain: ApiDomainInfo? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val includeShared: Boolean = false
)

class DomainsViewModel(
    private val repository: PurelymailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DomainsUiState())
    val uiState: StateFlow<DomainsUiState> = _uiState.asStateFlow()

    init {
        loadDomains()
        loadOwnershipCode()
    }

    fun loadDomains() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.listDomains(includeShared = _uiState.value.includeShared)
            result.onSuccess { domains ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    domains = domains
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to load domains"
                )
            }
        }
    }

    fun toggleIncludeShared(include: Boolean) {
        _uiState.value = _uiState.value.copy(includeShared = include)
        loadDomains()
    }

    fun loadOwnershipCode() {
        viewModelScope.launch {
            val result = repository.getOwnershipCode()
            result.onSuccess { code ->
                _uiState.value = _uiState.value.copy(ownershipCode = code)
            }
        }
    }

    fun selectDomain(domain: ApiDomainInfo) {
        _uiState.value = _uiState.value.copy(selectedDomain = domain)
    }

    fun clearSelectedDomain() {
        _uiState.value = _uiState.value.copy(selectedDomain = null)
    }

    fun addDomain(domainName: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.addDomain(domainName)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Domain $domainName added successfully!"
                )
                loadDomains()
                onDone()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to add domain. Check DNS ownership record first."
                )
            }
        }
    }

    fun updateDomainSettings(request: UpdateDomainSettingsRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.updateDomainSettings(request)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = if (request.recheckDns) "DNS recheck triggered!" else "Domain settings updated!"
                )
                loadDomains()
                // Update selected domain view
                _uiState.value.domains.find { it.name == request.name }?.let {
                    _uiState.value = _uiState.value.copy(selectedDomain = it)
                }
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to update domain settings"
                )
            }
        }
    }

    fun deleteDomain(domainName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deleteDomain(domainName)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedDomain = null,
                    successMessage = "Domain $domainName deleted"
                )
                loadDomains()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to delete domain"
                )
            }
        }
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
