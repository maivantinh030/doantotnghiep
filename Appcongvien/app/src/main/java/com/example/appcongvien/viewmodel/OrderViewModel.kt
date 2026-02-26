package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    private val _createOrderState = MutableStateFlow<Resource<OrderDTO>?>(null)
    val createOrderState: StateFlow<Resource<OrderDTO>?> = _createOrderState

    private val _ordersState = MutableStateFlow<Resource<PaginatedData<OrderDTO>>?>(null)
    val ordersState: StateFlow<Resource<PaginatedData<OrderDTO>>?> = _ordersState

    private val _orderDetailState = MutableStateFlow<Resource<OrderDTO>?>(null)
    val orderDetailState: StateFlow<Resource<OrderDTO>?> = _orderDetailState

    private val _ticketsState = MutableStateFlow<Resource<PaginatedData<TicketDTO>>?>(null)
    val ticketsState: StateFlow<Resource<PaginatedData<TicketDTO>>?> = _ticketsState

    fun createOrder(
        items: List<OrderItemRequest>,
        voucherCode: String? = null,
        paymentMethod: String = "BALANCE",
        note: String? = null
    ) {
        viewModelScope.launch {
            _createOrderState.value = Resource.Loading
            _createOrderState.value = orderRepository.createOrder(
                CreateOrderRequest(items, voucherCode, paymentMethod, note)
            )
        }
    }

    fun loadOrders(page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            _ordersState.value = Resource.Loading
            _ordersState.value = orderRepository.getOrders(page, size)
        }
    }

    fun loadOrderDetail(orderId: String) {
        viewModelScope.launch {
            _orderDetailState.value = Resource.Loading
            _orderDetailState.value = orderRepository.getOrderDetail(orderId)
        }
    }

    fun cancelOrder(orderId: String, reason: String? = null) {
        viewModelScope.launch {
            _orderDetailState.value = Resource.Loading
            _orderDetailState.value = orderRepository.cancelOrder(orderId, reason)
        }
    }

    fun loadMyTickets(page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            _ticketsState.value = Resource.Loading
            _ticketsState.value = orderRepository.getMyTickets(page, size)
        }
    }

    fun resetCreateOrderState() { _createOrderState.value = null }

    class Factory(private val repository: OrderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrderViewModel(repository) as T
        }
    }
}
