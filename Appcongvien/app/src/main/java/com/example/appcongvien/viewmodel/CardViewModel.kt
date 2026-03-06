package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CardViewModel(private val cardRepository: CardRepository) : ViewModel() {

    private val _cardsState = MutableStateFlow<Resource<List<CardDTO>>?>(null)
    val cardsState: StateFlow<Resource<List<CardDTO>>?> = _cardsState

    private val _linkCardState = MutableStateFlow<Resource<CardDTO>?>(null)
    val linkCardState: StateFlow<Resource<CardDTO>?> = _linkCardState

    private val _blockCardState = MutableStateFlow<Resource<CardDTO>?>(null)
    val blockCardState: StateFlow<Resource<CardDTO>?> = _blockCardState

    private val _actionState = MutableStateFlow<Resource<Unit>?>(null)
    val actionState: StateFlow<Resource<Unit>?> = _actionState

    private val _virtualCardState = MutableStateFlow<Resource<CardDTO>?>(null)
    val virtualCardState: StateFlow<Resource<CardDTO>?> = _virtualCardState

    fun loadMyCards() {
        viewModelScope.launch {
            _cardsState.value = Resource.Loading
            _cardsState.value = cardRepository.getMyCards()
        }
    }

    fun linkCard(physicalCardUid: String, cardName: String? = null, pin: String? = null) {
        viewModelScope.launch {
            _linkCardState.value = Resource.Loading
            _linkCardState.value = cardRepository.linkCard(LinkCardRequest(physicalCardUid, cardName, pin))
        }
    }

    fun blockCard(cardId: String, reason: String? = null) {
        viewModelScope.launch {
            _blockCardState.value = Resource.Loading
            _blockCardState.value = cardRepository.blockCard(cardId, reason)
        }
    }

    fun unblockCard(cardId: String) {
        viewModelScope.launch {
            _blockCardState.value = Resource.Loading
            _blockCardState.value = cardRepository.unblockCard(cardId)
        }
    }

    fun unlinkCard(cardId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            _actionState.value = cardRepository.unlinkCard(cardId)
        }
    }

    fun createVirtualCard() {
        viewModelScope.launch {
            _virtualCardState.value = Resource.Loading
            _virtualCardState.value = cardRepository.createVirtualCard()
        }
    }

    fun generateVirtualCard(cardId: String) {
        viewModelScope.launch {
            _virtualCardState.value = Resource.Loading
            _virtualCardState.value = cardRepository.generateVirtualCard(cardId)
        }
    }

    fun removeVirtualCard(cardId: String) {
        viewModelScope.launch {
            _virtualCardState.value = Resource.Loading
            _virtualCardState.value = cardRepository.removeVirtualCard(cardId)
        }
    }

    fun resetLinkCardState() { _linkCardState.value = null }
    fun resetBlockCardState() { _blockCardState.value = null }
    fun resetActionState() { _actionState.value = null }
    fun resetVirtualCardState() { _virtualCardState.value = null }

    class Factory(private val repository: CardRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CardViewModel(repository) as T
        }
    }
}
