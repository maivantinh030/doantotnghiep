package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.repository.SupportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SupportViewModel(private val supportRepository: SupportRepository) : ViewModel() {

    private val _messagesState = MutableStateFlow<Resource<PaginatedData<SupportMessageDTO>>?>(null)
    val messagesState: StateFlow<Resource<PaginatedData<SupportMessageDTO>>?> = _messagesState

    private val _sendState = MutableStateFlow<Resource<SupportMessageDTO>?>(null)
    val sendState: StateFlow<Resource<SupportMessageDTO>?> = _sendState

    fun loadMessages(page: Int = 1, size: Int = 50) {
        viewModelScope.launch {
            _messagesState.value = Resource.Loading
            _messagesState.value = supportRepository.getMessages(page, size)
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            _sendState.value = Resource.Loading
            _sendState.value = supportRepository.sendMessage(content)
            if (_sendState.value is Resource.Success) {
                loadMessages()
            }
        }
    }

    fun resetSendState() { _sendState.value = null }

    class Factory(private val repository: SupportRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SupportViewModel(repository) as T
        }
    }
}
