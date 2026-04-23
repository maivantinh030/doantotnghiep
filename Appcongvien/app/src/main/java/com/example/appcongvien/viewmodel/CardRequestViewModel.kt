package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.CardDTO
import com.example.appcongvien.data.model.CardRequestDTO
import com.example.appcongvien.data.model.CreateCardRequestRequest
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.repository.CardRepository
import com.example.appcongvien.data.repository.CardRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CardRequestUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isCheckingCards: Boolean = false,
    val isBlockingCard: Boolean = false,
    val requests: List<CardRequestDTO> = emptyList(),
    val activeCard: CardDTO? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class CardRequestViewModel(
    private val repository: CardRequestRepository,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardRequestUiState())
    val uiState: StateFlow<CardRequestUiState> = _uiState

    init {
        loadMyRequests()
        loadMyCards()
    }

    fun loadMyRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.getMyCardRequests()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    requests = result.data
                )

                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )

                is Resource.Loading -> Unit
            }
        }
    }

    fun loadMyCards() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingCards = true)
            when (val result = cardRepository.getMyCards()) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(
                    isCheckingCards = false,
                    activeCard = result.data.firstOrNull { it.status == "ACTIVE" }
                )

                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isCheckingCards = false,
                    errorMessage = result.message
                )

                is Resource.Loading -> Unit
            }
        }
    }

    fun submitRequest(note: String?, depositAmount: Long?) {
        if (_uiState.value.activeCard != null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Ban dang co san the khong the tao them"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSending = true,
                errorMessage = null,
                successMessage = null
            )
            val request = CreateCardRequestRequest(
                depositPaidOnline = false,
                depositAmount = depositAmount?.toString() ?: "0",
                note = note?.ifBlank { null }
            )
            when (val result = repository.createCardRequest(request)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        successMessage = "Yeu cau da duoc gui. Vui long den quay de nhan the."
                    )
                    loadMyRequests()
                }

                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isSending = false,
                    errorMessage = result.message
                )

                is Resource.Loading -> Unit
            }
        }
    }

    fun blockActiveCard(reason: String = "Nguoi dung bao mat the") {
        val activeCard = _uiState.value.activeCard ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBlockingCard = true,
                errorMessage = null,
                successMessage = null
            )

            when (val result = cardRepository.blockCard(activeCard.cardId, reason)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isBlockingCard = false,
                        activeCard = null,
                        successMessage = "The cu da duoc khoa. Ban co the tao the moi."
                    )
                    loadMyCards()
                }

                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isBlockingCard = false,
                    errorMessage = result.message
                )

                is Resource.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }

    class Factory(
        private val repository: CardRequestRepository,
        private val cardRepository: CardRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CardRequestViewModel(repository, cardRepository) as T
    }
}
