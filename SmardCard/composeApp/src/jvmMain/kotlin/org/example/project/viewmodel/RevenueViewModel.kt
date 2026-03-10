package org.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.data.model.GameRevenue
import org.example.project.data.model.RevenuePoint
import org.example.project.data.repository.TransactionRepository

data class RevenueUiState(
    val isLoading: Boolean = false,
    val dayData: List<RevenuePoint> = emptyList(),
    val monthData: List<RevenuePoint> = emptyList(),
    val gameData: List<GameRevenue> = emptyList(),
    val errorMessage: String? = null
)

class RevenueViewModel : ViewModel() {

    private val repository = TransactionRepository()

    private val _uiState = MutableStateFlow(RevenueUiState())
    val uiState: StateFlow<RevenueUiState> = _uiState

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            repository.getRevenueByDay().onSuccess { data ->
                _uiState.value = _uiState.value.copy(dayData = data)
            }

            repository.getRevenueByMonth().onSuccess { data ->
                _uiState.value = _uiState.value.copy(monthData = data)
            }

            repository.getRevenueByGame().fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(gameData = data)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
