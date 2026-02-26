package com.park.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.park.data.model.CreateVoucherRequest
import com.park.data.model.UpdateVoucherRequest
import com.park.data.model.VoucherDTO
import com.park.data.repository.VoucherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class VoucherManagementUiState(
    val isLoading: Boolean = false,
    val vouchers: List<VoucherDTO> = emptyList(),
    val total: Int = 0,
    val currentPage: Int = 1,
    val selectedVoucher: VoucherDTO? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class VoucherManagementViewModel : ViewModel() {

    private val repository = VoucherRepository()

    private val _uiState = MutableStateFlow(VoucherManagementUiState())
    val uiState: StateFlow<VoucherManagementUiState> = _uiState

    init {
        loadVouchers()
    }

    fun loadVouchers(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.getVouchers(page = page).fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        vouchers = data.items,
                        total = data.total,
                        currentPage = page
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
                }
            )
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true, selectedVoucher = null)
    }

    fun showEditDialog(voucher: VoucherDTO) {
        _uiState.value = _uiState.value.copy(showEditDialog = true, selectedVoucher = voucher)
    }

    fun dismissDialogs() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = false, showEditDialog = false, selectedVoucher = null
        )
    }

    fun createVoucher(request: CreateVoucherRequest) {
        viewModelScope.launch {
            repository.createVoucher(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Đã tạo voucher \"${it.code}\"",
                        showCreateDialog = false
                    )
                    loadVouchers()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )
        }
    }

    fun updateVoucher(voucherId: String, request: UpdateVoucherRequest) {
        viewModelScope.launch {
            repository.updateVoucher(voucherId, request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Đã cập nhật voucher",
                        showEditDialog = false
                    )
                    loadVouchers()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
            )
        }
    }

    fun deleteVoucher(voucherId: String) {
        viewModelScope.launch {
            repository.deleteVoucher(voucherId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(successMessage = "Đã xóa voucher")
                    loadVouchers()
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
