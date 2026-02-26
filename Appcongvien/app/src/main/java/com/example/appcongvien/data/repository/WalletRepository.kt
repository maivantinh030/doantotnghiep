package com.example.appcongvien.data.repository

import com.example.appcongvien.data.local.TokenManager
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.network.ApiService

class WalletRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun getBalance(): Resource<WalletBalanceDTO> {
        return try {
            val response = apiService.getWalletBalance()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                tokenManager.updateBalance(data.currentBalance)
                Resource.Success(data)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể lấy số dư")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun topUp(amount: String, method: String): Resource<TransactionDTO> {
        return try {
            val response = apiService.topUp(TopUpRequest(amount, method))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Nạp tiền thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getTransactions(
        page: Int = 1,
        size: Int = 10,
        type: String? = null
    ): Resource<PaginatedData<TransactionDTO>> {
        return try {
            val response = apiService.getTransactions(page, size, type)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải lịch sử giao dịch")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getPayments(page: Int = 1, size: Int = 10): Resource<PaginatedData<PaymentRecordDTO>> {
        return try {
            val response = apiService.getPayments(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải lịch sử thanh toán")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
