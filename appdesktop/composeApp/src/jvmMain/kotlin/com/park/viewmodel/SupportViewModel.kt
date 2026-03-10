package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.SupportMessageDTO
import com.park.data.network.ApiClient
import com.park.data.network.SupportWebSocketClient
import com.park.data.repository.SupportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SupportUiState(
    val isLoading: Boolean = false,
    val messages: List<SupportMessageDTO> = emptyList(),
    val selectedUserId: String? = null,
    val replyContent: String = "",
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class SupportViewModel : ViewModel() {

    private val repository = SupportRepository()
    private var wsClient: SupportWebSocketClient? = null

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState

    init {
        loadMessages()
        connectWebSocket()
    }

    private fun connectWebSocket() {
        val token = ApiClient.getToken() ?: return
        wsClient = SupportWebSocketClient(token).also { client ->
            client.connect(viewModelScope)

            viewModelScope.launch {
                client.newMessage.collect { newMsg ->
                    val current = _uiState.value.messages
                    if (current.any { it.messageId == newMsg.messageId }) return@collect
                    _uiState.value = _uiState.value.copy(messages = current + newMsg)
                }
            }
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getSupportMessages().fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        messages = data.items
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            )
        }
    }

    fun selectUser(userId: String?) {
        _uiState.value = _uiState.value.copy(selectedUserId = userId, replyContent = "")
    }

    fun setReplyContent(content: String) {
        _uiState.value = _uiState.value.copy(replyContent = content)
    }

    fun sendReply() {
        val userId = _uiState.value.selectedUserId ?: return
        val content = _uiState.value.replyContent.trim()
        if (content.isBlank()) return

        viewModelScope.launch {
            repository.replyToUser(userId, content).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        replyContent = "",
                        successMessage = "Đã gửi phản hồi"
                    )
                    loadMessages()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        wsClient?.disconnect()
    }
}
