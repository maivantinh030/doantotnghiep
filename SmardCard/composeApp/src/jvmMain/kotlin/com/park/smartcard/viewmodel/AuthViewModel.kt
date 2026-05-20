package com.park.smartcard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.park.smartcard.data.model.AdminInfo
import com.park.smartcard.data.repository.AuthRepository

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val adminInfo: AdminInfo? = null,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun login(phoneNumber: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            repository.login(phoneNumber, password).fold(
                onSuccess = { authData ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        adminInfo = authData.admin
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Đăng nhập thất bại"
                    )
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _state.value = AuthUiState()
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
