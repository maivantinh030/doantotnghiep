package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.CreateGameRequest
import com.park.data.model.GameDTO
import com.park.data.model.UpdateGameRequest
import com.park.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
    val errorMessage: String? = null
)

class GameManagementViewModel : ViewModel() {

    private val repository = GameRepository()

    private val _uiState = MutableStateFlow(GameManagementUiState())
    val uiState: StateFlow<GameManagementUiState> = _uiState

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
            print(" Lấy được")
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

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}
