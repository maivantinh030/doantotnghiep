package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.DashboardStats
import com.park.data.repository.GameRepository
import com.park.data.repository.OrderRepository
import com.park.data.repository.UserRepository
import com.park.data.repository.VoucherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val stats: DashboardStats = DashboardStats(),
    val errorMessage: String? = null
)

class DashboardViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val gameRepo = GameRepository()
    private val voucherRepo = VoucherRepository()
    private val orderRepo = OrderRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            var stats = DashboardStats()

            userRepo.getUsers().onSuccess { data ->
                stats = stats.copy(totalUsers = data.total)
            }
            gameRepo.getGames().onSuccess { data ->
                stats = stats.copy(totalGames = data.total)
            }
            voucherRepo.getVouchers().onSuccess { data ->
                stats = stats.copy(activeVouchers = data.total)
            }
            orderRepo.getOrders().onSuccess { data ->
                stats = stats.copy(totalOrders = data.total)
            }

            _uiState.value = _uiState.value.copy(isLoading = false, stats = stats)
        }
    }
}
