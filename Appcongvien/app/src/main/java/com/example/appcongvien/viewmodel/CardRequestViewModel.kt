package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.CardDTO
import com.example.appcongvien.data.model.CardRequestDTO
import com.example.appcongvien.data.model.CreateCardRequestRequest
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.model.WalletBalanceDTO
import com.example.appcongvien.data.repository.CardRepository
import com.example.appcongvien.data.repository.CardRequestRepository
import com.example.appcongvien.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CardRequestViewModel(
    private val repository: CardRequestRepository,
    private val cardRepository: CardRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    companion object {
        const val DEPOSIT_AMOUNT = 50_000L
    }

    private val _requestsState = MutableStateFlow<Resource<List<CardRequestDTO>>?>(null)
    val requestsState: StateFlow<Resource<List<CardRequestDTO>>?> = _requestsState

    private val _cardsState = MutableStateFlow<Resource<List<CardDTO>>?>(null)
    val cardsState: StateFlow<Resource<List<CardDTO>>?> = _cardsState

    private val _balanceState = MutableStateFlow<Resource<WalletBalanceDTO>?>(null)
    val balanceState: StateFlow<Resource<WalletBalanceDTO>?> = _balanceState

    private val _submitState = MutableStateFlow<Resource<CardRequestDTO>?>(null)
    val submitState: StateFlow<Resource<CardRequestDTO>?> = _submitState

    private val _blockCardState = MutableStateFlow<Resource<CardDTO>?>(null)
    val blockCardState: StateFlow<Resource<CardDTO>?> = _blockCardState

    private val _cancelRequestState = MutableStateFlow<Resource<CardRequestDTO>?>(null)
    val cancelRequestState: StateFlow<Resource<CardRequestDTO>?> = _cancelRequestState

    init {
        loadMyRequests()
        loadMyCards()
        loadBalance()
    }

    fun loadBalance() {
        viewModelScope.launch {
            _balanceState.value = Resource.Loading
            _balanceState.value = walletRepository.getBalance()
        }
    }

    fun loadMyRequests() {
        viewModelScope.launch {
            _requestsState.value = Resource.Loading
            _requestsState.value = repository.getMyCardRequests()
        }
    }

    fun loadMyCards() {
        viewModelScope.launch {
            _cardsState.value = Resource.Loading
            _cardsState.value = cardRepository.getMyCards()
        }
    }

    fun submitRequest(note: String?, depositPaidOnline: Boolean, depositAmount: Long?) {
        val activeCard = (_cardsState.value as? Resource.Success)
            ?.data?.firstOrNull { it.status == "ACTIVE" }
        if (activeCard != null) {
            _submitState.value = Resource.Error("Bạn đang có sẵn thẻ, không thể tạo thêm")
            return
        }

        viewModelScope.launch {
            _submitState.value = Resource.Loading
            val request = CreateCardRequestRequest(
                depositPaidOnline = depositPaidOnline,
                depositAmount = depositAmount?.toString() ?: "0",
                note = note?.ifBlank { null }
            )
            val result = repository.createCardRequest(request)
            _submitState.value = result
            if (result is Resource.Success) {
                loadMyRequests()
                if (depositPaidOnline) {
                    loadBalance()
                }
            }
        }
    }

    fun blockActiveCard(reason: String = "Nguoi dung bao mat the") {
        val activeCard = (_cardsState.value as? Resource.Success)
            ?.data?.firstOrNull { it.status == "ACTIVE" } ?: return

        viewModelScope.launch {
            _blockCardState.value = Resource.Loading
            val result = cardRepository.blockCard(activeCard.cardId, reason)
            _blockCardState.value = result
            if (result is Resource.Success) {
                loadMyCards()
            }
        }
    }

    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            _cancelRequestState.value = Resource.Loading
            val result = repository.cancelCardRequest(requestId)
            _cancelRequestState.value = result
            if (result is Resource.Success) {
                loadMyRequests()
                if (result.data.depositPaidOnline) {
                    loadBalance()
                }
            }
        }
    }

    fun resetSubmitState() { _submitState.value = null }
    fun resetBlockCardState() { _blockCardState.value = null }
    fun resetCancelRequestState() { _cancelRequestState.value = null }

    class Factory(
        private val repository: CardRequestRepository,
        private val cardRepository: CardRepository,
        private val walletRepository: WalletRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CardRequestViewModel(repository, cardRepository, walletRepository) as T
        }
    }
}
