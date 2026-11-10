package com.purelymail.admin.ui.screens.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purelymail.admin.data.model.*
import com.purelymail.admin.data.repository.PurelymailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UsersUiState(
    val isLoading: Boolean = false,
    val users: List<String> = emptyList(),
    val filteredUsers: List<String> = emptyList(),
    val domains: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedUser: String? = null,
    val selectedUserDetails: GetUserResponse? = null,
    val isLoadingDetails: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val newlyGeneratedAppPassword: Pair<String, String>? = null // Label to Password
)

class UsersViewModel(
    private val repository: PurelymailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
        viewModelScope.launch {
            repository.refreshSignal.collect {
                loadUsers()
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val usersRes = repository.listUsers()
            val domainsRes = repository.listDomains(includeShared = true)

            val users = usersRes.getOrNull() ?: emptyList()
            val domains = domainsRes.getOrNull()?.map { it.name } ?: emptyList()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                users = users,
                domains = domains,
                filteredUsers = filterUsers(users, _uiState.value.searchQuery)
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredUsers = filterUsers(_uiState.value.users, query)
        )
    }

    private fun filterUsers(users: List<String>, query: String): List<String> {
        if (query.isBlank()) return users
        return users.filter { it.contains(query, ignoreCase = true) }
    }

    fun selectUser(userName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedUser = userName,
                selectedUserDetails = null,
                isLoadingDetails = true
            )
            val detailsRes = repository.getUser(userName)
            detailsRes.onSuccess { details ->
                _uiState.value = _uiState.value.copy(
                    selectedUserDetails = details,
                    isLoadingDetails = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoadingDetails = false,
                    error = err.message ?: "Failed to fetch user details"
                )
            }
        }
    }

    fun clearSelectedUser() {
        _uiState.value = _uiState.value.copy(selectedUser = null, selectedUserDetails = null)
    }

    fun createUser(request: CreateUserRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.createUser(request)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Mailbox ${request.userName}@${request.domainName} created!"
                )
                loadUsers()
                onDone()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to create user"
                )
            }
        }
    }

    fun modifyUser(request: ModifyUserRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.modifyUser(request)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "User settings updated!"
                )
                loadUsers()
                val targetUser = request.newUserName ?: request.userName
                selectUser(targetUser)
                onDone()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to modify user"
                )
            }
        }
    }

    fun deleteUser(userName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deleteUser(userName)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedUser = null,
                    selectedUserDetails = null,
                    successMessage = "User $userName deleted"
                )
                loadUsers()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to delete user"
                )
            }
        }
    }

    fun createAppPassword(userHandle: String, name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.createAppPassword(userHandle, name)
            result.onSuccess { pass ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    newlyGeneratedAppPassword = Pair(name.ifBlank { "Client Password" }, pass),
                    successMessage = "App password generated successfully!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to generate app password"
                )
            }
        }
    }

    fun clearNewAppPassword() {
        _uiState.value = _uiState.value.copy(newlyGeneratedAppPassword = null)
    }

    fun upsertPasswordReset(request: UpsertPasswordResetRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.upsertPasswordReset(request)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Recovery method saved"
                )
                selectUser(request.userName)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to update recovery method"
                )
            }
        }
    }

    fun deletePasswordReset(userName: String, target: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.deletePasswordReset(userName, target)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Recovery method removed"
                )
                selectUser(userName)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to delete recovery method"
                )
            }
        }
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
