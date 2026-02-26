package com.example.appcongvien.data.repository

import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.network.ApiService

class OrderRepository(private val apiService: ApiService) {

    suspend fun createOrder(request: CreateOrderRequest): Resource<OrderDTO> {
        return try {
            val response = apiService.createOrder(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tạo đơn hàng")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getOrders(page: Int = 1, size: Int = 10): Resource<PaginatedData<OrderDTO>> {
        return try {
            val response = apiService.getOrders(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải lịch sử đơn hàng")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getOrderDetail(orderId: String): Resource<OrderDTO> {
        return try {
            val response = apiService.getOrderDetail(orderId)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không tìm thấy đơn hàng")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun cancelOrder(orderId: String, reason: String? = null): Resource<OrderDTO> {
        return try {
            val response = apiService.cancelOrder(orderId, CancelOrderRequest(reason))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể hủy đơn hàng")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun getMyTickets(page: Int = 1, size: Int = 10): Resource<PaginatedData<TicketDTO>> {
        return try {
            val response = apiService.getMyTickets(page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Không thể tải vé")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
