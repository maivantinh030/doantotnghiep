package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthData>?>(null)
    val loginState: StateFlow<Resource<AuthData>?> = _loginState

    private val _registerState = MutableStateFlow<Resource<AuthData>?>(null)
    val registerState: StateFlow<Resource<AuthData>?> = _registerState

    private val _changePasswordState = MutableStateFlow<Resource<Unit>?>(null)
    val changePasswordState: StateFlow<Resource<Unit>?> = _changePasswordState

    private val _profileState = MutableStateFlow<Resource<UserDTO>?>(null)
    val profileState: StateFlow<Resource<UserDTO>?> = _profileState

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn()
    val currentUserName: String? get() = authRepository.getCurrentUserName()
    val currentBalance: String? get() = authRepository.getCurrentBalance()

    fun login(phoneNumber: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            _loginState.value = authRepository.login(LoginRequest(phoneNumber, password))
        }
    }

    fun register(
        phoneNumber: String,
        password: String,
        fullName: String,
        email: String? = null,
        dateOfBirth: String? = null,
        gender: String? = null,
        referralCode: String? = null
    ) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading
            _registerState.value = authRepository.register(
                RegisterRequest(phoneNumber, password, fullName, email, dateOfBirth, gender, referralCode)
            )
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = Resource.Loading
            _profileState.value = authRepository.getUserProfile()
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = Resource.Loading
            _changePasswordState.value = authRepository.changePassword(
                ChangePasswordRequest(currentPassword, newPassword)
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun resetLoginState() { _loginState.value = null }
    fun resetRegisterState() { _registerState.value = null }
    fun resetChangePasswordState() { _changePasswordState.value = null }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
