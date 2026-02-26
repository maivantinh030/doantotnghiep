package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WalletViewModel(private val walletRepository: WalletRepository) : ViewModel() {

    private val _balanceState = MutableStateFlow<Resource<WalletBalanceDTO>?>(null)
    val balanceState: StateFlow<Resource<WalletBalanceDTO>?> = _balanceState

    private val _topUpState = MutableStateFlow<Resource<TransactionDTO>?>(null)
    val topUpState: StateFlow<Resource<TransactionDTO>?> = _topUpState

    private val _transactionsState = MutableStateFlow<Resource<PaginatedData<TransactionDTO>>?>(null)
    val transactionsState: StateFlow<Resource<PaginatedData<TransactionDTO>>?> = _transactionsState

    private val _paymentsState = MutableStateFlow<Resource<PaginatedData<PaymentRecordDTO>>?>(null)
    val paymentsState: StateFlow<Resource<PaginatedData<PaymentRecordDTO>>?> = _paymentsState

    fun loadBalance() {
        viewModelScope.launch {
            _balanceState.value = Resource.Loading
            _balanceState.value = walletRepository.getBalance()
        }
    }

    fun topUp(amount: String, method: String) {
        viewModelScope.launch {
            _topUpState.value = Resource.Loading
            _topUpState.value = walletRepository.topUp(amount, method)
        }
    }

    fun loadTransactions(page: Int = 1, size: Int = 10, type: String? = null) {
        viewModelScope.launch {
            _transactionsState.value = Resource.Loading
            _transactionsState.value = walletRepository.getTransactions(page, size, type)
        }
    }

    fun loadPayments(page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            _paymentsState.value = Resource.Loading
            _paymentsState.value = walletRepository.getPayments(page, size)
        }
    }

    fun resetTopUpState() { _topUpState.value = null }

    class Factory(private val repository: WalletRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WalletViewModel(repository) as T
        }
    }
}
