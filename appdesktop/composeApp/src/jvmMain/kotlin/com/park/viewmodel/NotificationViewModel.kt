package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.AdminSentNotificationDTO
import com.park.data.model.SendNotificationRequest
import com.park.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val notifications: List<AdminSentNotificationDTO> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class NotificationViewModel : ViewModel() {

    private val repository = NotificationRepository()

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getNotifications().fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notifications = data.items
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

    fun sendNotification(title: String, message: String, targetType: String = "ALL") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, errorMessage = null)
            repository.sendNotification(
                SendNotificationRequest(title = title, message = message, targetType = targetType)
            ).fold(
                onSuccess = { response ->
                    val targetName = when(targetType) {
                        "ALL" -> "tất cả người dùng"
                        "PLATINUM" -> "hội viên Platinum"
                        "GOLD" -> "hội viên Gold"
                        "SILVER" -> "hội viên Silver"
                        "BRONZE" -> "hội viên Bronze"
                        else -> "người dùng"
                    }
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        successMessage = "Đã gửi thông báo đến ${response.sentCount} $targetName"
                    )
                    loadNotifications()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        errorMessage = e.message
                    )
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
