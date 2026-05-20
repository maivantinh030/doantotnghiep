package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.CreateGameRequest
import com.park.data.model.GameDTO
import com.park.data.model.UpdateGameRequest
import com.park.data.repository.GameRepository
import com.park.data.repository.UploadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class GameManagementUiState(
    val isLoading: Boolean = false,
    val games: List<GameDTO> = emptyList(),
    val total: Int = 0,
    val currentPage: Int = 1,
    val searchQuery: String = "",
    val selectedGame: GameDTO? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isUploading: Boolean = false
)

class GameManagementViewModel : ViewModel() {

    private val repository = GameRepository()
    private val uploadRepository = UploadRepository()

    private val _uiState = MutableStateFlow(GameManagementUiState())
    val uiState: StateFlow<GameManagementUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    fun loadGames(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getGames(
                page = page,
                size = 20,
                search = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
            )
            result.fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        games = data.items,
                        total = data.total,
                        currentPage = page
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
            )
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadGames(1)
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true, selectedGame = null)
    }

    fun showEditDialog(game: GameDTO) {
        _uiState.value = _uiState.value.copy(showEditDialog = true, selectedGame = game)
    }

    fun dismissDialogs() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = false,
            showEditDialog = false,
            selectedGame = null
        )
    }

    fun createGame(request: CreateGameRequest) {
        viewModelScope.launch {
            repository.createGame(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Đã tạo trò chơi \"${it.name}\"",
                        showCreateDialog = false
                    )
                    loadGames(_uiState.value.currentPage)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )
        }
    }

    fun updateGame(gameId: String, request: UpdateGameRequest) {
        viewModelScope.launch {
            repository.updateGame(gameId, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Đã cập nhật trò chơi",
                        showEditDialog = false,
                        selectedGame = null
                    )
                    loadGames(_uiState.value.currentPage)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )
        }
    }

    fun deleteGame(gameId: String) {
        viewModelScope.launch {
            repository.deleteGame(gameId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Đã xóa trò chơi")
                    loadGames(_uiState.value.currentPage)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )
        }
    }

    fun uploadImage(file: File, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, errorMessage = null)
            uploadRepository.uploadImage(file).fold(
                onSuccess = { url ->
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        successMessage = "Đã upload ảnh game"
                    )
                    onSuccess(url)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        errorMessage = e.message ?: "Không upload được ảnh game"
                    )
                }
            )
        }
    }

    fun uploadImages(files: List<File>, onEachSuccess: (String) -> Unit) {
        if (files.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, errorMessage = null)
            var failedCount = 0
            files.forEach { file ->
                uploadRepository.uploadImage(file).fold(
                    onSuccess = { url -> onEachSuccess(url) },
                    onFailure = { failedCount += 1 }
                )
            }
            _uiState.value = if (failedCount > 0) {
                _uiState.value.copy(
                    isUploading = false,
                    errorMessage = "Có $failedCount ảnh upload thất bại"
                )
            } else {
                _uiState.value.copy(
                    isUploading = false,
                    successMessage = "Đã upload ${files.size} ảnh gallery"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
