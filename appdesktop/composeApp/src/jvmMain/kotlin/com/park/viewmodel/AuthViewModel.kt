package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.AdminProfile
import com.park.data.network.ApiClient
import com.park.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val adminProfile: AdminProfile? = null,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(
        // Khôi phục phiên: nếu token đã được TokenStore load → vào thẳng dashboard.
        // Profile sẽ null, UI cần handle (vd: hiện "Admin" thay vì tên cụ thể).
        AuthUiState(isLoggedIn = ApiClient.getToken() != null)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(phoneNumber: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.login(phoneNumber, password)
            result.fold(
                onSuccess = { authData ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        adminProfile = authData.admin
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Đăng nhập thất bại"
                    )
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
