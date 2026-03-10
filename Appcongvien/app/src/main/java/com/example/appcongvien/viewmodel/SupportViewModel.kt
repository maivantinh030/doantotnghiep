package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.local.TokenManager
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.network.SupportWebSocketClient
import com.example.appcongvien.data.repository.SupportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SupportViewModel(
    private val supportRepository: SupportRepository,
    private val wsClient: SupportWebSocketClient,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _messagesState = MutableStateFlow<Resource<PaginatedData<SupportMessageDTO>>?>(null)
    val messagesState: StateFlow<Resource<PaginatedData<SupportMessageDTO>>?> = _messagesState

    private val _sendState = MutableStateFlow<Resource<SupportMessageDTO>?>(null)
    val sendState: StateFlow<Resource<SupportMessageDTO>?> = _sendState

    init {
        loadMessages()
        connectWebSocket()
    }

    private fun connectWebSocket() {
        val token = tokenManager.getToken() ?: return
        wsClient.connect(token)

        viewModelScope.launch {
            wsClient.newMessage.collect { newMsg ->
                val current = (_messagesState.value as? Resource.Success)?.data ?: return@collect
                if (current.items.any { it.messageId == newMsg.messageId }) return@collect
                _messagesState.value = Resource.Success(
                    current.copy(items = current.items + newMsg)
                )
            }
        }
    }

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
                val result = supportRepository.getMessages()
                if (result is Resource.Success) {
                    _messagesState.value = result
                }
            }
        }
    }

    fun resetSendState() { _sendState.value = null }

    override fun onCleared() {
        super.onCleared()
        wsClient.disconnect()
    }

    class Factory(
        private val repository: SupportRepository,
        private val wsClient: SupportWebSocketClient,
        private val tokenManager: TokenManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SupportViewModel(repository, wsClient, tokenManager) as T
        }
    }
}
