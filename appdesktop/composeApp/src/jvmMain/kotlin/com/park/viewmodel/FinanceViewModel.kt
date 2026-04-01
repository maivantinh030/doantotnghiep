package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.TransactionDTO
import com.park.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FinanceUiState(
    val isLoading: Boolean = false,
    val transactions: List<TransactionDTO> = emptyList(),
    val totalTransactions: Int = 0,
    val errorMessage: String? = null
)

class FinanceViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState

    init {
        loadTransactions()
    }

    fun loadTransactions(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getTransactions(page).fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        transactions = data.items,
                        totalTransactions = data.total
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
            )
        }
    }
}
