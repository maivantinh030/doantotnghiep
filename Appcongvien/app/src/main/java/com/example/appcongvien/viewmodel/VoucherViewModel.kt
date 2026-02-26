package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.repository.VoucherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VoucherViewModel(private val voucherRepository: VoucherRepository) : ViewModel() {

    private val _vouchersState = MutableStateFlow<Resource<PaginatedData<VoucherDTO>>?>(null)
    val vouchersState: StateFlow<Resource<PaginatedData<VoucherDTO>>?> = _vouchersState

    private val _myVouchersState = MutableStateFlow<Resource<PaginatedData<UserVoucherDTO>>?>(null)
    val myVouchersState: StateFlow<Resource<PaginatedData<UserVoucherDTO>>?> = _myVouchersState

    private val _claimState = MutableStateFlow<Resource<UserVoucherDTO>?>(null)
    val claimState: StateFlow<Resource<UserVoucherDTO>?> = _claimState

    private val _searchState = MutableStateFlow<Resource<VoucherDTO>?>(null)
    val searchState: StateFlow<Resource<VoucherDTO>?> = _searchState

    fun loadVouchers(page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            _vouchersState.value = Resource.Loading
            _vouchersState.value = voucherRepository.getVouchers(page, size)
        }
    }

    fun loadMyVouchers(page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            _myVouchersState.value = Resource.Loading
            _myVouchersState.value = voucherRepository.getMyVouchers(page, size)
        }
    }

    fun claimVoucher(voucherId: String) {
        viewModelScope.launch {
            _claimState.value = Resource.Loading
            _claimState.value = voucherRepository.claimVoucher(voucherId)
        }
    }

    fun searchVoucher(code: String) {
        viewModelScope.launch {
            _searchState.value = Resource.Loading
            _searchState.value = voucherRepository.getVoucherByCode(code)
        }
    }

    fun resetClaimState() { _claimState.value = null }
    fun resetSearchState() { _searchState.value = null }

    class Factory(private val repository: VoucherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VoucherViewModel(repository) as T
        }
    }
}
