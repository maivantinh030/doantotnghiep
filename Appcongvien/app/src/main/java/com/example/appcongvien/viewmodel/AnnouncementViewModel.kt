package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.AnnouncementDTO
import com.example.appcongvien.data.model.Resource
import com.example.appcongvien.data.repository.AnnouncementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnnouncementViewModel(private val repository: AnnouncementRepository) : ViewModel() {

    private val _announcementsState = MutableStateFlow<Resource<List<AnnouncementDTO>>?>(null)
    val announcementsState: StateFlow<Resource<List<AnnouncementDTO>>?> = _announcementsState

    init {
        loadAnnouncements()
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            _announcementsState.value = Resource.Loading
            _announcementsState.value = repository.getAnnouncements()
        }
    }

    class Factory(private val repository: AnnouncementRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AnnouncementViewModel(repository) as T
        }
    }
}
