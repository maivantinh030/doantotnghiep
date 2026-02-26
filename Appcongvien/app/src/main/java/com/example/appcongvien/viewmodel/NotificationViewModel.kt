package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val notificationRepository: NotificationRepository) : ViewModel() {

    private val _notificationsState = MutableStateFlow<Resource<PaginatedData<NotificationDTO>>?>(null)
    val notificationsState: StateFlow<Resource<PaginatedData<NotificationDTO>>?> = _notificationsState

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    fun loadNotifications(page: Int = 1, size: Int = 20) {
        viewModelScope.launch {
            _notificationsState.value = Resource.Loading
            _notificationsState.value = notificationRepository.getNotifications(page, size)
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            val result = notificationRepository.getUnreadCount()
            if (result is Resource.Success) {
                _unreadCount.value = result.data
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            loadUnreadCount()
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            _unreadCount.value = 0
            loadNotifications()
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
            loadNotifications()
        }
    }

    class Factory(private val repository: NotificationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(repository) as T
        }
    }
}
