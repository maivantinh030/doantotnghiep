package com.example.appcongvien.data.repository

import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.network.ApiService

class VoucherRepository(private val apiService: ApiService) {

    suspend fun getVouchers(page: Int = 1, size: Int = 10): Resource<PaginatedData<VoucherDTO>> {
        return try {
            val response = apiService.getVouchers(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải danh sách voucher")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getVoucherByCode(code: String): Resource<VoucherDTO> {
        return try {
            val response = apiService.getVoucherByCode(code)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không tìm thấy voucher")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun claimVoucher(voucherId: String): Resource<UserVoucherDTO> {
        return try {
            val response = apiService.claimVoucher(voucherId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể nhận voucher")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getMyVouchers(page: Int = 1, size: Int = 10): Resource<PaginatedData<UserVoucherDTO>> {
        return try {
            val response = apiService.getMyVouchers(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải ví voucher")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
