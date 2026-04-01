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

    private val _blockCardState = MutableStateFlow<Resource<CardDTO>?>(null)
    val blockCardState: StateFlow<Resource<CardDTO>?> = _blockCardState

    fun loadMyCards() {
        viewModelScope.launch {
            _cardsState.value = Resource.Loading
            _cardsState.value = cardRepository.getMyCards()
        }
    }

    fun blockCard(cardId: String, reason: String? = null) {
        viewModelScope.launch {
            _blockCardState.value = Resource.Loading
            _blockCardState.value = cardRepository.blockCard(cardId, reason)
        }
    }

    fun resetBlockCardState() { _blockCardState.value = null }

    class Factory(private val repository: CardRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CardViewModel(repository) as T
        }
    }
}
