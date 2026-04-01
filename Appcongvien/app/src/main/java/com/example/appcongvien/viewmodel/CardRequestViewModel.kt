package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.CardRequestDTO
import com.example.appcongvien.data.model.CreateCardRequestRequest
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.repository.CardRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CardRequestUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val requests: List<CardRequestDTO> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class CardRequestViewModel(
    private val repository: CardRequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardRequestUiState())
    val uiState: StateFlow<CardRequestUiState> = _uiState

    init {
        loadMyRequests()
    }

    fun loadMyRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.getMyCardRequests()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, requests = result.data
                )
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, errorMessage = result.message
                )
                is Resource.Loading -> {}
            }
        }
    }

    fun submitRequest(note: String?, depositAmount: Long?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, errorMessage = null)
            val request = CreateCardRequestRequest(
                depositPaidOnline = false,
                depositAmount = depositAmount?.toString() ?: "0",
                note = note?.ifBlank { null }
            )
            when (val result = repository.createCardRequest(request)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        successMessage = "Yêu cầu đã được gửi! Vui lòng đến quầy để nhận thẻ."
                    )
                    loadMyRequests()
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isSending = false, errorMessage = result.message
                )
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }

    class Factory(private val repository: CardRequestRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CardRequestViewModel(repository) as T
    }
}
