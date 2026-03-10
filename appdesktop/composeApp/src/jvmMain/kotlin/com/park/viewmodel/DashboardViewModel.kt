package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.CardLookupResultDTO
import com.park.data.model.DashboardStats
import com.park.data.model.OrderDTO
import com.park.data.model.RevenueChartData
import com.park.data.repository.CardRepository
import com.park.data.repository.OrderRepository
import com.park.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class RevenuePeriod(val label: String, val apiValue: String) {
    DAILY("Ngày", "daily"),
    WEEKLY("Tuần", "weekly"),
    MONTHLY("Tháng", "monthly")
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val stats: DashboardStats = DashboardStats(),
    val chartData: RevenueChartData = RevenueChartData(),
    val selectedPeriod: RevenuePeriod = RevenuePeriod.DAILY,
    val isChartLoading: Boolean = false,
    val recentOrders: List<OrderDTO> = emptyList(),
    val errorMessage: String? = null,
    val lastCardLookupResult: CardLookupResultDTO? = null,
    val lastCardLookupError: String? = null
)

class DashboardViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val orderRepo = OrderRepository()
    private val cardRepo = CardRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadStats()
        loadChart(RevenuePeriod.DAILY)
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            userRepo.getDashboardStats().onSuccess { stats ->
                _uiState.value = _uiState.value.copy(stats = stats)
            }

            orderRepo.getOrders(size = 5).onSuccess { data ->
                _uiState.value = _uiState.value.copy(recentOrders = data.items)
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectPeriod(period: RevenuePeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadChart(period)
    }

    private fun loadChart(period: RevenuePeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChartLoading = true)
            orderRepo.getRevenueChart(period.apiValue).onSuccess { data ->
                _uiState.value = _uiState.value.copy(chartData = data, isChartLoading = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isChartLoading = false)
            }
        }
    }

    /**
     * Hàm tiện thử nghiệm: dùng UID giả (hoặc sau này UID thật) để gọi backend lookup-by-uid.
     */
    fun testLookupCardByFakeUid(fakeUid: String = "04A1B2C3D4E5F6") {
        viewModelScope.launch {
            val result = cardRepo.lookupByUid(fakeUid)
            result
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        lastCardLookupResult = data,
                        lastCardLookupError = null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        lastCardLookupResult = null,
                        lastCardLookupError = e.message ?: "Lỗi tra cứu thẻ"
                    )
                }
        }
    }
}
