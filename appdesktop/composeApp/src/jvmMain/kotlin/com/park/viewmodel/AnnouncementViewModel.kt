package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.AnnouncementDTO
import com.park.data.model.CreateAnnouncementRequest
import com.park.data.model.GameDTO
import com.park.data.model.UpdateAnnouncementRequest
import com.park.data.model.VoucherDTO
import com.park.data.repository.AnnouncementRepository
import com.park.data.repository.GameRepository
import com.park.data.repository.VoucherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AnnouncementUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val announcements: List<AnnouncementDTO> = emptyList(),
    val games: List<GameDTO> = emptyList(),
    val vouchers: List<VoucherDTO> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class AnnouncementViewModel : ViewModel() {

    private val repository = AnnouncementRepository()
    private val gameRepository = GameRepository()
    private val voucherRepository = VoucherRepository()

    private val _uiState = MutableStateFlow(AnnouncementUiState())
    val uiState: StateFlow<AnnouncementUiState> = _uiState

    init {
        loadAnnouncements()
        loadGames()
        loadVouchers()
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAnnouncements().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, announcements = list)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
            )
        }
    }

    private fun loadGames() {
        viewModelScope.launch {
            gameRepository.getGames(size = 100).fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(games = data.items)
                },
                onFailure = {}
            )
        }
    }

    private fun loadVouchers() {
        viewModelScope.launch {
            voucherRepository.getVouchers(size = 100).fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(vouchers = data.items)
                },
                onFailure = {}
            )
        }
    }

    fun createAnnouncement(request: CreateAnnouncementRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            repository.createAnnouncement(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, successMessage = "Đã tạo banner thành công")
                    loadAnnouncements()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message)
                }
            )
        }
    }

    fun updateAnnouncement(id: String, request: UpdateAnnouncementRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            repository.updateAnnouncement(id, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, successMessage = "Đã cập nhật banner")
                    loadAnnouncements()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message)
                }
            )
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Đã xóa banner")
                    loadAnnouncements()
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
}
