package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.data.model.AddGameRequest
import org.example.project.data.model.GameDto
import org.example.project.data.repository.GameRepository

data class GameUiState(
    val isLoading: Boolean = false,
    val games: List<GameDto> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class GameViewModel : ViewModel() {

    private val repository = GameRepository()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    init {
        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.getGames().fold(
                onSuccess = { games ->
                    _uiState.value = _uiState.value.copy(isLoading = false, games = games)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
            )
        }
    }

    fun addGame(name: String, description: String, price: String, imageData: ByteArray? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val imageBase64 = imageData?.let { java.util.Base64.getEncoder().encodeToString(it) }
            val request = AddGameRequest(
                gameName = name,
                gameDescription = description,
                ticketPrice = price,
                gameImage = imageBase64
            )
            repository.addGame(request).fold(
                onSuccess = { gameCode ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Đã thêm game '$name' với mã $gameCode"
                    )
                    loadGames()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
            )
        }
    }

    fun deleteGame(gameCode: Int, gameName: String) {
        viewModelScope.launch {
            repository.deleteGame(gameCode).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Đã xóa game $gameName")
                    loadGames()
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
