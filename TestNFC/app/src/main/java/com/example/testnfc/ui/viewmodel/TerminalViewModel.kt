package com.example.testnfc.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testnfc.data.model.GameItem
import com.example.testnfc.data.model.UseGameData
import com.example.testnfc.data.repository.TerminalRepository
import com.example.testnfc.nfc.TerminalNfcManager
import com.example.testnfc.nfc.TerminalNfcStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TerminalUiState(
    // Auth
    val isLoggedIn: Boolean = false,
    val adminName: String = "",
    val token: String = "",
    val loginLoading: Boolean = false,
    val loginError: String? = null,

    // Games
    val games: List<GameItem> = emptyList(),
    val gamesLoading: Boolean = false,
    val gamesError: String? = null,
    val selectedGame: GameItem? = null,

    // NFC / Play
    val playLoading: Boolean = false,
    val playResult: PlayResult? = null
)

data class PlayResult(
    val success: Boolean,
    val message: String,
    val data: UseGameData? = null
)

class TerminalViewModel : ViewModel() {

    private val repository = TerminalRepository()
    val nfcManager = TerminalNfcManager()

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    val nfcStatus: StateFlow<TerminalNfcStatus> = nfcManager.status
    val nfcMessage: StateFlow<String> = nfcManager.statusMessage

    // ─── Auth ───────────────────────────────────────────────────────────────

    fun login(phoneNumber: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loginLoading = true, loginError = null)
            val result = repository.login(phoneNumber, password)
            result.fold(
                onSuccess = { resp ->
                    if (resp.success && resp.data != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoggedIn = true,
                            token = resp.data.token,
                            adminName = resp.data.admin.fullName,
                            loginLoading = false,
                            loginError = null
                        )
                        loadGames(resp.data.token)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            loginLoading = false,
                            loginError = resp.message
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        loginLoading = false,
                        loginError = e.message
                    )
                }
            )
        }
    }

    fun logout() {
        _uiState.value = TerminalUiState()
    }

    // ─── Games ──────────────────────────────────────────────────────────────

    fun loadGames(token: String = _uiState.value.token) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(gamesLoading = true, gamesError = null)
            repository.getGames(token).fold(
                onSuccess = { games ->
                    _uiState.value = _uiState.value.copy(
                        games = games.filter { it.status == "ACTIVE" },
                        gamesLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        gamesLoading = false,
                        gamesError = e.message
                    )
                }
            )
        }
    }

    fun selectGame(game: GameItem) {
        _uiState.value = _uiState.value.copy(
            selectedGame = game,
            playResult = null
        )
    }

    fun clearSelectedGame() {
        _uiState.value = _uiState.value.copy(selectedGame = null, playResult = null)
    }

    // ─── NFC + Play ─────────────────────────────────────────────────────────

    fun enableNfc(activity: Activity) {
        val game = _uiState.value.selectedGame ?: return
        nfcManager.enableReaderMode(activity) { cardUid ->
            onCardScanned(cardUid, game.gameId)
        }
    }

    fun disableNfc(activity: Activity) {
        nfcManager.disableReaderMode(activity)
    }

    fun resetScan() {
        _uiState.value = _uiState.value.copy(playResult = null, playLoading = false)
        nfcManager.reset()
    }

    private fun onCardScanned(cardUid: String, gameId: String) {
        val token = _uiState.value.token
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(playLoading = true, playResult = null)
            repository.useGame(token, gameId, cardUid).fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        playLoading = false,
                        playResult = PlayResult(
                            success = true,
                            message = "Cho phép chơi! Còn ${data.remainingTurns} lượt.",
                            data = data
                        )
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        playLoading = false,
                        playResult = PlayResult(
                            success = false,
                            message = e.message ?: "Lỗi không xác định"
                        )
                    )
                }
            )
        }
    }
}
