package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.OrderDTO
import com.park.data.model.TransactionDTO
import com.park.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FinanceUiState(
    val isLoading: Boolean = false,
    val orders: List<OrderDTO> = emptyList(),
    val transactions: List<TransactionDTO> = emptyList(),
    val totalOrders: Int = 0,
    val totalTransactions: Int = 0,
    val currentTab: FinanceTab = FinanceTab.ORDERS,
    val errorMessage: String? = null
)

enum class FinanceTab { ORDERS, TRANSACTIONS }

class FinanceViewModel : ViewModel() {

    private val repository = OrderRepository()

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState

    init {
        loadOrders()
    }

    fun selectTab(tab: FinanceTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
        when (tab) {
            FinanceTab.ORDERS -> if (_uiState.value.orders.isEmpty()) loadOrders()
            FinanceTab.TRANSACTIONS -> if (_uiState.value.transactions.isEmpty()) loadTransactions()
        }
    }

    fun loadOrders(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getOrders(page).fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = data.items,
                        totalOrders = data.total
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
            )
        }
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
