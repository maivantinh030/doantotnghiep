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

    private val _markAsReadState = MutableStateFlow<Resource<Unit>?>(null)
    val markAsReadState: StateFlow<Resource<Unit>?> = _markAsReadState

    private val _markAllAsReadState = MutableStateFlow<Resource<Unit>?>(null)
    val markAllAsReadState: StateFlow<Resource<Unit>?> = _markAllAsReadState

    private val _deleteNotificationState = MutableStateFlow<Resource<Unit>?>(null)
    val deleteNotificationState: StateFlow<Resource<Unit>?> = _deleteNotificationState

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
            _markAsReadState.value = Resource.Loading
            val result = notificationRepository.markAsRead(notificationId)
            _markAsReadState.value = result
            if (result is Resource.Success) {
                loadUnreadCount()
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _markAllAsReadState.value = Resource.Loading
            val result = notificationRepository.markAllAsRead()
            _markAllAsReadState.value = result
            if (result is Resource.Success) {
                _unreadCount.value = 0
                loadNotifications()
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            _deleteNotificationState.value = Resource.Loading
            val result = notificationRepository.deleteNotification(notificationId)
            _deleteNotificationState.value = result
            if (result is Resource.Success) {
                loadNotifications()
            }
        }
    }

    fun resetMarkAsReadState() { _markAsReadState.value = null }
    fun resetMarkAllAsReadState() { _markAllAsReadState.value = null }
    fun resetDeleteNotificationState() { _deleteNotificationState.value = null }

    class Factory(private val repository: NotificationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(repository) as T
        }
    }
}
