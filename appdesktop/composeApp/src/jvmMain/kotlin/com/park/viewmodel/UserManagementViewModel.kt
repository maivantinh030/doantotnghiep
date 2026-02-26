package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.UserDTO
import com.park.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserManagementUiState(
    val isLoading: Boolean = false,
    val users: List<UserDTO> = emptyList(),
    val totalUsers: Int = 0,
    val currentPage: Int = 1,
    val searchQuery: String = "",
    val selectedUser: UserDTO? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class UserManagementViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState

    init {
        loadUsers()
    }

    fun loadUsers(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getUsers(
                page = page,
                size = 20,
                search = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
            )
            result.fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        users = data.items,
                        totalUsers = data.total,
                        currentPage = page
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            )
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadUsers(1)
    }

    fun selectUser(user: UserDTO?) {
        _uiState.value = _uiState.value.copy(selectedUser = user)
    }

    fun lockUser(userId: String) {
        viewModelScope.launch {
            repository.lockUser(userId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Đã khóa tài khoản")
                    loadUsers(_uiState.value.currentPage)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )
        }
    }

    fun unlockUser(userId: String) {
        viewModelScope.launch {
            repository.unlockUser(userId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Đã mở khóa tài khoản")
                    loadUsers(_uiState.value.currentPage)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )
        }
    }

    fun adjustBalance(userId: String, amount: Double, reason: String) {
        viewModelScope.launch {
            repository.adjustBalance(userId, amount, reason).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Đã điều chỉnh số dư")
                    loadUsers(_uiState.value.currentPage)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )
        }
    }

    fun updateMembership(userId: String, level: String) {
        viewModelScope.launch {
            repository.updateMembership(userId, level).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Đã cập nhật hạng thành viên")
                    loadUsers(_uiState.value.currentPage)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
