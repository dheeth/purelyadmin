package com.purelymail.admin.ui.screens.routing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purelymail.admin.data.model.CreateRoutingRequest
import com.purelymail.admin.data.model.RoutingRule
import com.purelymail.admin.data.repository.PurelymailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RoutingUiState(
    val isLoading: Boolean = false,
    val rules: List<RoutingRule> = emptyList(),
    val domains: List<String> = emptyList(),
    val users: List<String> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val newlyCreatedAlias: String? = null
)

class RoutingViewModel(
    private val repository: PurelymailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutingUiState())
    val uiState: StateFlow<RoutingUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val rulesRes = repository.listRoutingRules()
            val domainsRes = repository.listDomains(includeShared = false)
            val usersRes = repository.listUsers()

            val rules = rulesRes.getOrNull() ?: emptyList()
            val domains = domainsRes.getOrNull()?.map { it.name } ?: emptyList()
            val users = usersRes.getOrNull() ?: emptyList()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                rules = rules,
                domains = domains,
                users = users
            )
        }
    }

    fun createRule(request: CreateRoutingRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.createRoutingRule(request)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Routing rule created!"
                )
                loadData()
                onDone()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to create routing rule"
                )
            }
        }
    }

    fun generateQuickAlias(
        serviceName: String,
        domain: String,
        targetEmail: String,
        onDone: (String) -> Unit
    ) {
        val cleanService = serviceName.trim().lowercase().replace("[^a-z0-9]".toRegex(), "")
        val randomSuffix = (1..4).map { "0123456789abcdef".random() }.joinToString("")
        val aliasUser = if (cleanService.isNotBlank()) "$cleanService.$randomSuffix" else "alias.$randomSuffix"
        val fullAlias = "$aliasUser@$domain"

        val request = CreateRoutingRequest(
            domainName = domain,
            prefix = false,
            matchUser = aliasUser,
            targetAddresses = listOf(targetEmail),
            catchall = false
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.createRoutingRule(request)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    newlyCreatedAlias = fullAlias,
                    successMessage = "Generated alias: $fullAlias"
                )
                loadData()
                onDone(fullAlias)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to create quick alias"
                )
            }
        }
    }

    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deleteRoutingRule(ruleId)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Rule removed"
                )
                loadData()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to delete routing rule"
                )
            }
        }
    }

    fun clearNewAlias() {
        _uiState.value = _uiState.value.copy(newlyCreatedAlias = null)
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
